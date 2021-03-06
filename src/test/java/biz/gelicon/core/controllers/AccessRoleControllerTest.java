package biz.gelicon.core.controllers;

import biz.gelicon.core.dto.AccessRoleDTO;
import biz.gelicon.core.utils.ConvertUtils;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.view.AccessRoleView;
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


public class AccessRoleControllerTest extends IntergatedTest {

    private static final String CONTOURE = "admin";
    private static final String MODULE = "credential";

    @BeforeAll
    public static void setup() {
        token = "e9b3c034-fdd5-456f-825b-4c632f2053ac"; //SYSDBA
    }

    @Test
    public void selectTest() throws Exception {
        // базовая проверка
        ObjectMapper objectMapper = new ObjectMapper();
        GridDataOption options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("accessRoleId", Sort.Direction.ASC)
                .build();
        MvcResult result = this.mockMvc.perform(
                        post(buildUrl("accessrole/getlist", CONTOURE, MODULE))
                                .content(new ObjectMapper().writeValueAsString(options))
                                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        //  Чудеса, добавляет в начало {"result": и удаляет закрывающую квадратную скобку в конце
        // подправим
        content = ConvertUtils.correctMvcResult(content);
        List<AccessRoleView> accessRoleViewList =
                objectMapper.readValue(content, new TypeReference<>() {
                });
        Assertions.assertEquals(3, accessRoleViewList.size());
        //Assert.assertArrayEquals(measureListExpected.toArray(), measureList.toArray());

        // проверка быстрого фильтра eq
        options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("accessRoleId", Sort.Direction.ASC)
                .addFilter("quick.accessRoleName.eq", "SYSDBA")
                .build();

        this.mockMvc.perform(post(buildUrl("accessrole/getlist", CONTOURE, MODULE))
                        .content(new ObjectMapper().writeValueAsString(options))
                        .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"rowCount\":1")));

        // проверка быстрого фильтра like
        options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("accessRoleId", Sort.Direction.DESC)
                .addFilter("quick.accessRoleName.like", "YSDB")
                .build();

        this.mockMvc.perform(post(buildUrl("accessrole/getlist", CONTOURE, MODULE))
                        .content(new ObjectMapper().writeValueAsString(options))
                        .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"rowCount\":1")));

        // проверка именованного фильтра
        options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("accessRoleId", Sort.Direction.DESC)
                .addFilter("onlyVisible", 1)
                .build();

        this.mockMvc.perform(post(buildUrl("accessrole/getlist", CONTOURE, MODULE))
                        .content(new ObjectMapper().writeValueAsString(options))
                        .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"rowCount\":3")));
        logger.info("selectTest() - Ok");
    }

    @Test
    public void searchFullText() throws Exception {
        GridDataOption options = new GridDataOption.Builder()
                .pagination(1, 25)
                .search("ин")
                .build();

        this.mockMvc.perform(post(buildUrl("accessrole/getlist", CONTOURE, MODULE))
                        .content(new ObjectMapper().writeValueAsString(options))
                        .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"rowCount\":2")));
        logger.info("searchFullText() - Ok");

    }

    @Test
    @Transactional
    @Rollback
    public void updateTest() throws Exception {
        // проверка получения записи для редактирования
        MvcResult result = this.mockMvc.perform(post(buildUrl("accessrole/get", CONTOURE, MODULE))
                        .content("2")
                        .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorMessage\":"))))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        AccessRoleDTO dto = new ObjectMapper().readValue(content, AccessRoleDTO.class);

        // проверка сохранения изменений
        String checkValue = "---";
        String fldName = "accessRoleName";
        dto.setAccessRoleName(checkValue);

        this.mockMvc.perform(post(buildUrl("accessrole/save", CONTOURE, MODULE))
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString(String.format("\"%s\":\"%s\"", fldName, checkValue))));

        // проверка ошибки Запись не найдена
        this.mockMvc.perform(post(buildUrl("accessrole/get", CONTOURE, MODULE))
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
        MvcResult result = this.mockMvc.perform(post(buildUrl("accessrole/get", CONTOURE, MODULE))
                        .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorMessage\":"))))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        AccessRoleDTO dto = new ObjectMapper().readValue(content, AccessRoleDTO.class);

        // проверка сохранения c ошибками
        this.mockMvc.perform(post(buildUrl("accessrole/save", CONTOURE, MODULE))
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"errorMessage\":")));

        // проверка сохранения без ошибкок

        String checkValue = "---";
        dto.setAccessRoleNote("foo bar baz");
        dto.setAccessRoleVisible(1);
        String fldName = "accessRoleName";
        dto.setAccessRoleName(checkValue);

        result = this.mockMvc.perform(post(buildUrl("accessrole/save", CONTOURE, MODULE))
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString(String.format("\"%s\":\"%s\"", fldName, checkValue))))
                .andReturn();
        content = result.getResponse().getContentAsString();
        AccessRoleView view = new ObjectMapper().readValue(content, AccessRoleView.class);

        //удаление записи
        this.mockMvc.perform(post(buildUrl("accessrole/delete", CONTOURE, MODULE))
                        .content("[" + view.getAccessRoleId().toString() + "]")
                        .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorMessage\":"))));

    }


}

