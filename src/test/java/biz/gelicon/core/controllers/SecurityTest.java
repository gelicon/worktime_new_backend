package biz.gelicon.core.controllers;

import biz.gelicon.core.dto.NewProgUserPasswordDTO;
import biz.gelicon.core.dto.PasswordAndUserDTO;
import biz.gelicon.core.dto.TokenDTO;
import biz.gelicon.core.security.UserCredential;
import biz.gelicon.core.utils.GridDataOption;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SecurityTest  extends IntergatedTest {

    @Test
    public void gettoken() throws Exception {
        UserCredential user = new UserCredential("SYSDBA", "masterkey");

        this.mockMvc.perform(post("/security/gettoken")
                .content(new ObjectMapper().writeValueAsString(user))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"token\":")));

        // {"errorCode":128,"timeStamp":1646912472637,"errorMessage":"Неверное имя пользователя или пароль
        UserCredential userNew = new UserCredential("ADMIN", "admin123");
        MvcResult result = this.mockMvc.perform(post("/security/gettoken")
                .content(new ObjectMapper().writeValueAsString(userNew))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                //.andExpect(content().string(containsString("\"errorCode\":127")));
                .andReturn();
        String content = result.getResponse().getContentAsString();
        Assertions.assertTrue(content.contains("\"errorCode\":128"));

        // с устаревшим токеном
        UserCredential userOld = new UserCredential("WORKER", "worker");
        result = this.mockMvc.perform(post("/security/gettoken")
                .content(new ObjectMapper().writeValueAsString(userOld))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                //.andExpect(content().string(not(containsString("\"token\":\"62e4e435-85da-48a4-adb0-d91ff1d26624\""))));
                .andReturn();
        content = result.getResponse().getContentAsString();
        Assertions.assertFalse(
                content.contains("\"token\":\"62e4e435-85da-48a4-adb0-d91ff1d26624\""));

    }

    @Test
    @Transactional
    @Rollback
    public void renew() throws Exception {
        // пробуем с устаревшим токеном
        // {"errorCode":140,"timeStamp":1646914537915,"errorMessage":"Срок действия токена истек",
        TokenDTO oldTok = new TokenDTO("62e4e435-85da-48a4-adb0-d91ff1d26624");
        MvcResult result = this.mockMvc.perform(post("/security/renew")
                .content(new ObjectMapper().writeValueAsString(oldTok))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        Assertions.assertTrue(content.contains("\"errorCode\":140"));

        // пробуем с текущим токеном
        TokenDTO newTok = new TokenDTO("e9b3c034-fdd5-456f-825b-4c632f2053ac");
        this.mockMvc.perform(post("/security/renew")
                .content(new ObjectMapper().writeValueAsString(newTok))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"token\":")));

    }

    @Test
    public void incorrectAccess() throws Exception {
        // неверный токен
        GridDataOption options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("edizmNotation", Sort.Direction.ASC)
                .build();

        this.mockMvc.perform(post(buildUrl("/apps/edizm/edizm/getlist"))
                .header("Authorization","Bearer unknown-token")
                .content(new ObjectMapper().writeValueAsString(options))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isUnauthorized());

        // с устаревшим токеном
        this.mockMvc.perform(post(buildUrl("/apps/edizm/edizm/getlist"))
                .header("Authorization","Bearer 62e4e435-85da-48a4-adb0-d91ff1d26624")
                .content(new ObjectMapper().writeValueAsString(options))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isUnauthorized());

        // с токеном заблокированного пользователя
        this.mockMvc.perform(post(buildUrl("/apps/edizm/edizm/getlist"))
                .header("Authorization","Bearer c85bcbdf-d4f8-434b-ab43-3308099ad561")
                .content(new ObjectMapper().writeValueAsString(options))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    @Rollback
    public void changeTempPassword() throws Exception {
        // устаналиваем временный пароль у 4 WORKER
        NewProgUserPasswordDTO pswd = new NewProgUserPasswordDTO(4, "newpassword",1);
        this.mockMvc.perform(post(buildUrl("/proguser/setpswd","admin","credential"))
                .header("Authorization","Bearer e9b3c034-fdd5-456f-825b-4c632f2053ac")
                .content(new ObjectMapper().writeValueAsString(pswd))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorCode\":"))));

        // пытаемся зайти со старым паролем
        UserCredential user = new UserCredential("WORKER", "worker");
        this.mockMvc.perform(post("/security/gettoken")
                .content(new ObjectMapper().writeValueAsString(user))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"errorCode\":128")));

        // пытаемся зайти с этим новым временным паролем
        user = new UserCredential("WORKER", "newpassword");
        this.mockMvc.perform(post("/security/gettoken")
                .content(new ObjectMapper().writeValueAsString(user))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"errorCode\":131")));

        // меняем временный на постоянный
        PasswordAndUserDTO password = new PasswordAndUserDTO("WORKER","newpassword","worker");
        this.mockMvc.perform(post("/security/changepswd")
                .header("Authorization","Bearer ") //токен зануляем
                .content(new ObjectMapper().writeValueAsString(password))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorCode\":"))));


        // пытаемся зайти по временному
        user = new UserCredential("WORKER", "newpassword");
        this.mockMvc.perform(post("/security/gettoken")
                .content(new ObjectMapper().writeValueAsString(user))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"errorCode\":128")));
    }

}
