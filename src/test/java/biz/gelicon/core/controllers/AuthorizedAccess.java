package biz.gelicon.core.controllers;

import biz.gelicon.core.dto.ApplicationDTO;
import biz.gelicon.core.repository.AccessRoleRepository;
import biz.gelicon.core.security.ACL;
import biz.gelicon.core.security.Permission;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.utils.ReflectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthorizedAccess extends IntergatedTest {

    @BeforeAll
    public static void setup() {
        token = "22222222-85da-48a4-2222-d91ff1d26624"; //test1
    }

    @Autowired
    AccessRoleRepository accessRoleRepository;

    @Autowired
    ACL acl;

    @Test
    public void accessTest1() throws Exception {
        // базовая проверка
        GridDataOption options = new GridDataOption.Builder()
                .pagination(1, 25)
                .addSort("edizmNotation", Sort.Direction.ASC)
                .build();

        this.mockMvc.perform(post(buildUrl("/apps/refbooks/edizm/edizm/getlist"))
                .content(new ObjectMapper().writeValueAsString(options))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"rowCount\":5")));

        //удаление записи
        this.mockMvc.perform(post(buildUrl("/apps/refbooks/edizm/edizm/delete"))
                .content("[1]")         // тут не важно какой id - доступа не должно быть
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"errorCode\":130")));


    }

    @Test
    @Transactional
    @Rollback
    public void accessGet() throws Exception {
        // у Роли 1 отключим право на get и get#edit и get#add
        accessRoleRepository.unbindControlObject(1,2);
        accessRoleRepository.unbindControlObject(1,5);
        accessRoleRepository.unbindControlObject(1,6);
        acl.buildAccessTable();

        // проверка получения записи для редактирования
        this.mockMvc.perform(post(buildUrl("/apps/refbooks/edizm/edizm/get"))
                .content("1")
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"errorCode\":130")))
                .andReturn();

        // у Роли 1 вернем право только на get#edit
        accessRoleRepository.bindWithControlObject(1,5, Permission.EXECUTE);
        acl.buildAccessTable();
        // проверка получения записи для редактирования
        this.mockMvc.perform(post(buildUrl("/apps/refbooks/edizm/edizm/get"))
                .content("1")
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorCode\":"))))
                .andReturn();
        // а для добавления будет Access Denied
        this.mockMvc.perform(post(buildUrl("/apps/refbooks/edizm/edizm/get"))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"errorCode\":130")))
                .andReturn();
    }

    @Test
    @Transactional
    @Rollback
    public void accessSave() throws Exception {

        // проверка получения записи для редактирования
        MvcResult result = this.mockMvc.perform(post(buildUrl("/apps/refbooks/edizm/edizm/get"))
                .content("1")
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorMessage\":"))))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ApplicationDTO dto = new ObjectMapper().readValue(content,ApplicationDTO.class);

        // у Роли 1 отключим право на save и save#ins и save#upd
        accessRoleRepository.unbindControlObject(1,3);
        accessRoleRepository.unbindControlObject(1,7);
        accessRoleRepository.unbindControlObject(1,8);
        acl.buildAccessTable();

        this.mockMvc.perform(post(buildUrl("/apps/refbooks/edizm/edizm/save"))
                .content(new ObjectMapper().writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"errorCode\":130")));

        // у Роли 1 вернем право только на save#upd
        accessRoleRepository.bindWithControlObject(1,8, Permission.EXECUTE);
        acl.buildAccessTable();
        // проверка сохраниния
        this.mockMvc.perform(post(buildUrl("/apps/refbooks/edizm/edizm/save"))
                .content(new ObjectMapper().writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorCode\":"))));
        // а для save#ins будет Access Denied
        dto.setApplicationId(null);
        this.mockMvc.perform(post(buildUrl("/apps/refbooks/edizm/edizm/save"))
                .content(new ObjectMapper().writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"errorCode\":130")));
    }


    @Test
    public void methodExtension() throws NoSuchMethodException {

        Method mGet = ApplicationController.class.getDeclaredMethod("get", Integer.class);
        Assert.assertTrue(ReflectUtils.isGetMethod(mGet));

        Method mSave = ApplicationController.class.getDeclaredMethod("save", ApplicationDTO.class);
        Assert.assertTrue(ReflectUtils.isSaveMethod(mSave));

        Method mList = ApplicationController.class.getDeclaredMethod("getlist", ApplicationController.GridDataOptionApplication.class);
        Assert.assertTrue(!ReflectUtils.isGetMethod(mList) && !ReflectUtils.isSaveMethod(mList) );
    }

}
