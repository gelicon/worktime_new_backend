package biz.gelicon.core.controllers;

import biz.gelicon.core.dto.AllowOrDenyApplication;
import biz.gelicon.core.utils.ConvertUtils;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.view.ApplicationView;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class ApplicationRoleControllerTest extends IntergatedTest {

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
                .addSort("applicationId", Sort.Direction.ASC)
                .addFilter("accessRoleId", 1)
                .build();

        this.mockMvc.perform(post(buildUrl("applicationrole/getlist", CONTOURE, MODULE))
                        .content(new ObjectMapper().writeValueAsString(options))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()) // выводить результат в консоль
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"rowCount\":")));

        // проверка быстрого фильтра eq
        options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("applicationId", Sort.Direction.ASC)
                .addFilter("quick.applicationName.eq", "Роли доступа")
                .addFilter("accessRoleId", 1)
                .build();

        this.mockMvc.perform(post(buildUrl("applicationrole/getlist", CONTOURE, MODULE))
                        .content(new ObjectMapper().writeValueAsString(options))
                        .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"rowCount\":1")));

        // проверка быстрого фильтра like
        options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("applicationId", Sort.Direction.DESC)
                .addFilter("quick.applicationName.like", "един")
                .addFilter("accessRoleId", 1)
                .build();

        this.mockMvc.perform(post(buildUrl("applicationrole/getlist", CONTOURE, MODULE))
                        .content(new ObjectMapper().writeValueAsString(options))
                        .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"rowCount\":1")));

    }

    @Test
    public void searchFullText() throws Exception {
        GridDataOption options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addFilter("accessRoleId", 1)
                .search("admin.credential")
                .build();

        this.mockMvc.perform(post(buildUrl("applicationrole/getlist", CONTOURE, MODULE))
                        .content(new ObjectMapper().writeValueAsString(options))
                        .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @Transactional
    @Rollback
    public void allowOrDeny() throws Exception {
        AllowOrDenyApplication param = new AllowOrDenyApplication();
        param.setAccessRoleId(3); // 3	EDIZM	Единицы измерения
        param.setApplicationIds(new Integer[]{2, 3, 4});

        // разрешаем
        this.mockMvc.perform(post(buildUrl("applicationrole/allow", CONTOURE, MODULE))
                        .content(new ObjectMapper().writeValueAsString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorMessage\":"))));

        // отбираем
        this.mockMvc.perform(post(buildUrl("applicationrole/deny", CONTOURE, MODULE))
                        .content(new ObjectMapper().writeValueAsString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorMessage\":"))));

    }

    @Test
    public void accesslist() throws Exception {
        MvcResult result = this.mockMvc.perform(post(buildUrl("applicationrole/accesslist", CONTOURE, MODULE))
                        .header("Authorization", "Bearer 15a5a967-7a71-46f4-9af9-e3878b7fffac") // ADMIN
                        .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                // Запускаем обычно из под SYSDBA, которому все доступно
                // поэтому просто чтобы не свалилось
                //.andExpect(content().string(containsString("\"rowCount\":4")));
                .andReturn();
        String content = result.getResponse().getContentAsString();
        // подправим
        content = ConvertUtils.correctMvcResult(content);

        List<ApplicationView> actual
                = mapper.readValue(content, new TypeReference<>() {});

        // Список должен быть не пустым
        Assertions.assertTrue(actual.size() > 0);

        // пользователь с меньшим доступом
        // должно вернуть меньше записей
        result = this.mockMvc.perform(post(buildUrl("applicationrole/accesslist", CONTOURE, MODULE))
                        .header("Authorization", "Bearer bf528245-ce41-4ab4-9595-910191c0b1b1") // WORKER
                        .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                //.andExpect(content().string(containsString("\"rowCount\":")));
                .andReturn();
        content = result.getResponse().getContentAsString();
        // подправим
        content = ConvertUtils.correctMvcResult(content);

        List<ApplicationView> actual1
                = mapper.readValue(content, new TypeReference<>() {});

        // Список должен быть меньшим
        Assertions.assertTrue(actual1.size() < actual.size());
    }


}

