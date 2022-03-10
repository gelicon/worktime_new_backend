package biz.gelicon.core.controllers;

import biz.gelicon.core.dto.NewProgUserPasswordDTO;
import biz.gelicon.core.dto.PasswordDTO;
import biz.gelicon.core.model.Proguser;
import biz.gelicon.core.model.Progusergroup;
import biz.gelicon.core.utils.ConvertUtils;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.view.ControlObjectView;
import biz.gelicon.core.view.ProguserView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class ProguserControllerTest extends IntergatedTest {
    private static final String CONTOURE = "admin";
    private static final String MODULE = "credential";

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setup() {
        token = "e9b3c034-fdd5-456f-825b-4c632f2053ac"; //SYSDBA
    }

    @Test
    public void selectTest() throws Exception {
        // базовая проверка
        GridDataOption options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("proguserId", Sort.Direction.ASC)
                .build();

        MvcResult result = this.mockMvc.perform(post(buildUrl("proguser/getlist",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(options))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                //.andExpect(content().string(containsString("\"rowCount\":4")));
                .andReturn();
        String content = result.getResponse().getContentAsString();
        // подправим
        content = ConvertUtils.correctMvcResult(content);

        List<ProguserView> actual
                = mapper.readValue(content, new TypeReference<>() {});

        // Список должен быть не пустым
        Assertions.assertTrue(actual.size() > 1);

        // проверка быстрого фильтра eq
        options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("proguserId", Sort.Direction.ASC)
                .addFilter("quick.proguserName.eq","SYSDBA")
                .build();


        String s = new ObjectMapper().writeValueAsString(options);

        this.mockMvc.perform(post(buildUrl("proguser/getlist",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(options))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"rowCount\":1")));

        // проверка быстрого фильтра like
        options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("proguserId", Sort.Direction.DESC)
                .addFilter("quick.proguserFullName.like","дмин")
                .build();


        result = this.mockMvc.perform(post(buildUrl("proguser/getlist",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(options))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                //.andExpect(content().string(containsString("\"rowCount\":3")));
                .andReturn();
        content = result.getResponse().getContentAsString();
        // подправим
        content = ConvertUtils.correctMvcResult(content);

        List<ProguserView> actual1
                = mapper.readValue(content, new TypeReference<>() {});

        // Список должен меньшще
        Assertions.assertTrue(actual1.size() < actual.size());

        // проверка именованного фильтра
        options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("proguserId", Sort.Direction.DESC)
                .addFilter("proguserGroupId", Progusergroup.EVERYONE)
                .build();

    }

    @Test
    // Поиск
    public void searchFullText() throws Exception {
        GridDataOption options = new GridDataOption.Builder()
                .pagination(1, 25)
                .search("истра")
                .build();


        this.mockMvc.perform(post(buildUrl("proguser/getlist",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(options))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"rowCount\":2")));

    }

    @Test
    @Transactional
    @Rollback
    public void updateTest() throws Exception {

        // проверка получения записи для редактирования
        MvcResult result = this.mockMvc.perform(post(buildUrl("proguser/get",CONTOURE,MODULE))
                .content("2")
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorMessage\":"))))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        Proguser dto = new ObjectMapper().readValue(content,Proguser.class);

        // проверка сохранения изменений
        String checkValue = "---";
        String fldName = "proguserName";
        dto.setProguserName(checkValue);

        this.mockMvc.perform(post(buildUrl("proguser/save",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(String.format("\"%s\":\"%s\"",fldName,checkValue))));

        // проверка ошибки Запись не найдена
        this.mockMvc.perform(post(buildUrl("proguser/get",CONTOURE,MODULE))
                .content("-1")
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"errorMessage\":")))
                .andReturn();

    }

    @Test
    @Transactional
    @Rollback
    public void insertAndDeleteTest() throws Exception {
        // проверка получения записи для вставки
        MvcResult result = this.mockMvc.perform(post(buildUrl("proguser/get",CONTOURE,MODULE))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorMessage\":"))))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        Proguser dto = new ObjectMapper().readValue(content,Proguser.class);

        // проверка сохранения c ошибками
        this.mockMvc.perform(post(buildUrl("proguser/save",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"errorMessage\":")));

        // проверка сохранения без ошибкок

        String checkValue = "xyxyxyx";
        dto.setProguserFullName("этояэтояэтоя");
        dto.setStatusId(Proguser.USER_IS_ACTIVE);
        String fldName = "proguserName";
        dto.setProguserName(checkValue);


        result = this.mockMvc.perform(post(buildUrl("proguser/save",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(String.format("\"%s\":\"%s\"",fldName,checkValue))))
                .andReturn();
        content = result.getResponse().getContentAsString();
        ProguserView view = new ObjectMapper().readValue(content,ProguserView.class);

        //удаление записи
        this.mockMvc.perform(post(buildUrl("proguser/delete",CONTOURE,MODULE))
                .content("["+view.getProguserId().toString()+"]")
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorMessage\":"))));

    }


    @Test
    @Transactional
    @Rollback
    public void changePassword() throws Exception {
        PasswordDTO pswd = new PasswordDTO("masterkey", "newpassword");

        this.mockMvc.perform(post(buildUrl("/proguser/changepswd",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(pswd))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorCode\":"))));
    }

    @Test
    @Transactional
    @Rollback
    public void setPassword() throws Exception {
        // 4 WORKER	Работник
        // временный пароль
        NewProgUserPasswordDTO pswd = new NewProgUserPasswordDTO(4, "newpassword",1);

        this.mockMvc.perform(post(buildUrl("/proguser/setpswd",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(pswd))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorCode\":"))));
    }



}

