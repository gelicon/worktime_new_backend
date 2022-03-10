package biz.gelicon.core.controllers;

import biz.gelicon.core.dto.AllowOrDenyControlObject;
import biz.gelicon.core.dto.ControlObjectDTO;
import biz.gelicon.core.utils.ConvertUtils;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.view.ControlObjectView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ControlObjectControllerTest extends IntergatedTest {

    private static final String CONTOURE = "admin";
    private static final String MODULE = "credential";
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setup() {
        token = "e9b3c034-fdd5-456f-825b-4c632f2053ac"; //SYSDBA
    }

    @Test
    public void badSelectTest() throws Exception {
        // Проверка обязательности именованного фильтра
        GridDataOption options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("controlObjectId", Sort.Direction.ASC)
                .build();

        this.mockMvc.perform(post(buildUrl("controlobject/getlist",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(options))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("errorMessage\":\"Требуется именованный фильтр")));
    }

    @Test
    public void selectTest() throws Exception {
        // базовая проверка
        GridDataOption options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("controlObjectId", Sort.Direction.ASC)
                .addFilter("accessRoleId",1)
                .build();

        MvcResult result = this.mockMvc.perform(post(buildUrl("controlobject/getlist",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(options))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                //.andExpect(content().string(containsString("\"rowCount\":8")));
                .andReturn();
        String content = result.getResponse().getContentAsString();
        // подправим
        content = ConvertUtils.correctMvcResult(content);

        List<ControlObjectView> actual
                = mapper.readValue(content, new TypeReference<>() {});

        // Список должен быть не пустым
        Assertions.assertTrue(actual.size() > 10);

        // проверка быстрого фильтра eq
        options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("controlObjectId", Sort.Direction.ASC)
                .addFilter("quick.controlObjectName.like","Получение")
                .build();

        result = this.mockMvc.perform(post(buildUrl("controlobject/getlist",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(options))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                //.andExpect(content().string(containsString("\"rowCount\":2")));
                .andReturn();
        content = result.getResponse().getContentAsString();
        // подправим
        content = ConvertUtils.correctMvcResult(content);
        List<ControlObjectView> actual1
                = mapper.readValue(content, new TypeReference<>() {});

        // Должно быть меньше
        Assertions.assertTrue(actual1.size() < actual.size());

    }

    @Test
    public void searchFullText() throws Exception {
        GridDataOption options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addFilter("accessRoleId",0)
                .search("delete")
                .build();


        MvcResult result = this.mockMvc.perform(post(buildUrl("controlobject/getlist",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(options))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                //.andExpect(content().string(containsString("\"rowCount\":1")));
                .andReturn();
        String content = result.getResponse().getContentAsString();
        // подправим
        content = ConvertUtils.correctMvcResult(content);

        List<ControlObjectView> actual
                = mapper.readValue(content, new TypeReference<>() {});

        // Список должен быть не пустым
        Assertions.assertTrue(actual.size() > 0);

    }

    @Test
    @Transactional
    @Rollback
    public void updateTest() throws Exception {

        // проверка получения записи для редактирования
        MvcResult result = this.mockMvc.perform(post(buildUrl("controlobject/get",CONTOURE,MODULE))
                .content("1")
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorMessage\":"))))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ControlObjectDTO dto = new ObjectMapper().readValue(content,ControlObjectDTO.class);

        // проверка сохранения изменений
        String checkValue = "---";
        String fldName = "controlObjectName";
        dto.setControlObjectName(checkValue);

        this.mockMvc.perform(post(buildUrl("controlobject/save",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(String.format("\"%s\":\"%s\"",fldName,checkValue))));

        // проверка ошибки Запись не найдена
        this.mockMvc.perform(post(buildUrl("controlobject/get",CONTOURE,MODULE))
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
        MvcResult result = this.mockMvc.perform(post(buildUrl("controlobject/get",CONTOURE,MODULE))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorMessage\":"))))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ControlObjectDTO dto = new ObjectMapper().readValue(content,ControlObjectDTO.class);

        // проверка сохранения c ошибками
        this.mockMvc.perform(post(buildUrl("controlobject/save",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"errorMessage\":")));

        // проверка сохранения без ошибкок

        String checkValue = "---";
        dto.setControlObjectUrl("/abc/foo/baz");
        String fldName = "controlObjectName";
        dto.setControlObjectName(checkValue);


        result = this.mockMvc.perform(post(buildUrl("controlobject/save",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(String.format("\"%s\":\"%s\"",fldName,checkValue))))
                .andReturn();
        content = result.getResponse().getContentAsString();
        ControlObjectView view = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(content,ControlObjectView.class);

        //удаление записи
        this.mockMvc.perform(post(buildUrl("controlobject/delete",CONTOURE,MODULE))
                .content("["+view.getControlObjectId().toString()+"]")
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorMessage\":"))));

    }

    @Test
    @Transactional
    @Rollback
    public void allowOrDeny()  throws Exception {
        AllowOrDenyControlObject param = new AllowOrDenyControlObject();
        param.setAccessRoleId(3); // 3	EDIZM	Единицы измерения
        // 50	Группы пользователей: Удаление
        // 52	Группы пользователей: Получение объекта по идентификатору
        param.setControlObjectIds(new Integer[]{50,52});

        // разрешаем
        this.mockMvc.perform(post(buildUrl("controlobject/allow",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(param))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorMessage\":"))));

        // отбираем
        this.mockMvc.perform(post(buildUrl("controlobject/deny",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(param))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorMessage\":"))));

    }

}

