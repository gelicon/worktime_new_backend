package biz.gelicon.core.controllers;

import biz.gelicon.core.utils.GridDataOption;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class SessionControllerTest extends IntergatedTest {
    private static final String CONTOURE = "admin";
    private static final String MODULE = "audit";

    @BeforeAll
    public static void setup() {
        token = "e9b3c034-fdd5-456f-825b-4c632f2053ac"; //root
    }

    @Test
    public void selectTest() throws Exception {
        // базовая проверка
        GridDataOption options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("proguserAuthDateCreate", Sort.Direction.DESC)
                .build();

        this.mockMvc.perform(post(buildUrl("session/getlist",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(options))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"proguserAuthToken\":")));

        // проверка быстрого фильтра eq
        options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("proguserAuthDateCreate", Sort.Direction.DESC)
                .addFilter("quick.proguserAuthToken.eq","e9b3c034-fdd5-456f-825b-4c632f2053ac")
                .build();


        this.mockMvc.perform(post(buildUrl("session/getlist",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(options))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"rowCount\":1")));


        // проверка быстрого фильтра like
        options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("proguserAuthDateCreate", Sort.Direction.DESC)
                .addFilter("quick.proguserAuthToken.like","2222222")
                .build();


        this.mockMvc.perform(post(buildUrl("session/getlist",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(options))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"rowCount\":1")));

        // проверка именованного фильтра
        options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("proguserAuthDateCreate", Sort.Direction.DESC)
                .addFilter("status","1")
                .build();


        this.mockMvc.perform(post(buildUrl("session/getlist",CONTOURE,MODULE))
                .content(new ObjectMapper().writeValueAsString(options))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"proguserAuthToken\":")));

    }

    /*
    @Test
    public void searchFullText() throws Exception {
        GridDataOption options = new GridDataOption.Builder()
                .pagination(1, 25)
                .search("рам")
                .build();


        this.mockMvc.perform(post(buildUrl("/apps/refbooks/edizm/edizm/getlist"))
                .content(new ObjectMapper().writeValueAsString(options))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"rowCount\":2")));

    }

     */



}
