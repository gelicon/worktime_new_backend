package biz.gelicon.core.controllers;

import biz.gelicon.core.dto.AllowOrDeny;
import biz.gelicon.core.dto.AllowOrDenyApplication;
import biz.gelicon.core.utils.GridDataOption;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class ApplicationRoleControllerTest extends IntergatedTest {
    private static final String CONTOURE = "admin";
    private static final String MODULE = "credential";

    @BeforeAll
    public static void setup() {
        token = "e9b3c034-fdd5-456f-825b-4c632f2053ac"; //root
    }

    @Test
    public void selectTest() throws Exception {
        // базовая проверка
        GridDataOption options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("applicationId", Sort.Direction.ASC)
                .addFilter("accessRoleId",1)
                .build();

        this.mockMvc.perform(post(buildUrl("applicationrole/getlist",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(options))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"rowCount\":4")));

        // проверка быстрого фильтра eq
        options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("applicationId", Sort.Direction.ASC)
                .addFilter("quick.applicationName.eq","Роли")
                .addFilter("accessRoleId",1)
                .build();


        this.mockMvc.perform(post(buildUrl("applicationrole/getlist",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(options))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"rowCount\":1")));

        // проверка быстрого фильтра like
        options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("applicationId", Sort.Direction.DESC)
                .addFilter("quick.applicationName.like","един")
                .addFilter("accessRoleId",1)
                .build();


        this.mockMvc.perform(post(buildUrl("applicationrole/getlist",CONTOURE,MODULE))
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
                .addFilter("accessRoleId",1)
                .search("s4.m0")
                .build();


        this.mockMvc.perform(post(buildUrl("applicationrole/getlist",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(options))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"rowCount\":3")));

    }

    @Test
    @Transactional
    @Rollback
    public void allowOrDeny()  throws Exception {
        AllowOrDenyApplication param = new AllowOrDenyApplication();
        param.setAccessRoleId(3);
        param.setApplicationIds(new Integer[]{2,3,4});

        // разрешаем
        this.mockMvc.perform(post(buildUrl("applicationrole/allow",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(param))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorMessage\":"))));

        // отбираем
        this.mockMvc.perform(post(buildUrl("applicationrole/deny",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(param))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorMessage\":"))));

    }

    @Test
    public void accesslist() throws Exception {
        this.mockMvc.perform(post(buildUrl("applicationrole/accesslist",CONTOURE,MODULE))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"rowCount\":4")));

        // пользователь без доступа
        this.mockMvc.perform(post(buildUrl("applicationrole/accesslist",CONTOURE,MODULE))
                .header("Authorization","Bearer 22222222-85da-48a4-2222-d91ff1d26624")
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"rowCount\":0")));
    }


}

