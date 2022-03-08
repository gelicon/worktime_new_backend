package biz.gelicon.core.controllers;

import biz.gelicon.core.dto.ApplicationDTO;
import biz.gelicon.core.dto.EdizmDTO;
import biz.gelicon.core.repository.AccessRoleRepository;
import biz.gelicon.core.security.ACL;
import biz.gelicon.core.security.Permission;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.utils.ReflectUtils;
import biz.gelicon.core.view.EdizmView;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthorizedAccessTest extends IntergatedTest {

    @BeforeAll
    public static void setup() {
        token = "15a5a967-7a71-46f4-9af9-e3878b7fffac"; //ADMIN admin
    }

    @Autowired
    AccessRoleRepository accessRoleRepository;

    @Autowired
    ACL acl;

    @Test
    @Transactional
    @Rollback
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
        this.mockMvc.perform(post(buildUrl("/apps/admin/credential/progusergroup/delete"))
                .content("[1]")         // тут не важно какой id - доступа не должно быть
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"errorCode\":130")));
        // "errorCode":130,"timeStamp":1646632981995,"errorMessage":"Access is denied","exceptionClassName":"org.springframework.security.access.AccessDeniedException","fieldErrors":null,"cause":null}
    }

    @Test
    @Transactional
    @Rollback
    public void accessGet() throws Exception {
        // у Роли 3	EDIZM отключим право на get 2 Единицы измерения: Получение объекта по идентификатору
        accessRoleRepository.unbindControlObject(3,2);
        // Перестроим доступы
        acl.buildAccessTable();

        // проверка получения записи для редактирования
        // доступа быть не должно
        this.mockMvc.perform(post(buildUrl("/apps/refbooks/edizm/edizm/get"))
                .content("1")
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"errorCode\":130")))
                .andReturn();

        // у Роли 3	EDIZM вернем право
        accessRoleRepository.bindWithControlObject(3,2, Permission.EXECUTE);
        acl.buildAccessTable();
        // проверка получения записи для редактирования
        this.mockMvc.perform(post(buildUrl("/apps/refbooks/edizm/edizm/get"))
                .content("1")
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorCode\":"))))
                .andReturn();
    }

    @Test
    public void methodExtension() throws NoSuchMethodException {

        Method mGet = EdizmController.class.getDeclaredMethod("get", Integer.class);
        Assert.assertTrue(ReflectUtils.isGetMethod(mGet));

        Method mSave = EdizmController.class.getDeclaredMethod("save", EdizmDTO.class);
        Assert.assertTrue(ReflectUtils.isSaveMethod(mSave));

        Method mList = EdizmController.class.getDeclaredMethod("getlist", EdizmController.GridDataOptionEdizm.class);
        Assert.assertTrue(!ReflectUtils.isGetMethod(mList) && !ReflectUtils.isSaveMethod(mList) );
    }

}
