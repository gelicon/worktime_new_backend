package biz.gelicon.core.repository;

import biz.gelicon.core.model.ApplicationRole;
import biz.gelicon.core.service.ApplicationService;
import biz.gelicon.core.utils.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class ApplicationRoleRepository implements TableRepository<ApplicationRole> {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationRoleRepository.class);
    public final static int INITIAL_LOAD_COUNT = 6; // Для теста - сколько грузим в начале столько и ставим
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired
    private ApplicationService applicationService;


    @Override
    public void create() {
        Resource resource = new ClassPathResource("sql/400310-applicationrole.sql");
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(resource);
        databasePopulator.setSqlScriptEncoding("UTF-8");
        databasePopulator.execute(Objects.requireNonNull(jdbcTemplate.getDataSource()));
        logger.info(this.getTableMetaData().getTableName() + " created");
    }

    @Override
    public void dropForTest() {
        String[] dropTableBefore = new String[]{
        };
        TableRepository.super.drop(dropTableBefore);

        TableRepository.super.dropForTest();

        String[] dropTableAfter = new String[]{
        };
        TableRepository.super.drop(dropTableAfter);
    }

    @Override
    public int load() {
        ApplicationRole[] data = new ApplicationRole[]{
                new ApplicationRole(null, 2, 1), // ADMIN - Группы пользователей
                new ApplicationRole(null, 2, 2), // ADMIN - Приложения
                new ApplicationRole(null, 2, 3), // ADMIN - Роли доступа
                new ApplicationRole(null, 2, 4), // ADMIN - Пользователи
                new ApplicationRole(null, 2, 5), // ADMIN - Доступ роли на приложение
                new ApplicationRole(null, 3, 100), // EDIZM - Справочник единиц измерения
        };
        insert(Arrays.asList(data));
        String tableName = this.getTableMetaData().getTableName();
        DatabaseUtils.setSequence(tableName + "_id_gen", data.length + 1);
        logger.info(data.length + " " + tableName + " loaded");
        // Роль 2-ADMIN связана со всеми приложениями администрирования
        // Добавим все что загружено
        List<ApplicationRole> applicationRoleList = applicationService.getAllList();
        applicationRoleList.forEach(ar -> {
            allow(2, ar.getApplicationId());
        });
        return data.length;
    }

    /**
     * Связать роль с приложением
     *
     * @param accessRoleId - роль
     * @param appId        - приложение
     */
    public void allow(Integer accessRoleId, Integer appId) {
        if (isLinkedWithRole(accessRoleId, appId)) {
            return;
        }
        // SYSDBA незачем давать
        if (accessRoleId == AccessRoleRepository.SYSDBA_ACCESSROLE_ID) {
            return;
        }
        String sqlText = buildInsertClause("applicationrole",
                new String[]{"applicationrole_id", "application_id", "accessrole_id"},
                new String[]{"applicationrole_id", "application_id", "accessrole_id"});
        int id = DatabaseUtils.sequenceNextValue("applicationrole_id_gen");
        Map<String, Object> params = new HashMap<>();
        params.put("applicationrole_id", id);
        params.put("application_id", appId);
        params.put("accessrole_id", accessRoleId);
        executeSQL(sqlText, params);
    }

    /**
     * Отвязывает роль от приложения
     *
     * @param accessRoleId - роль
     * @param appId        - приложение
     */
    public void deny(Integer accessRoleId, Integer appId) {
        String sqlText = ""
                + " DELETE FROM applicationrole "
                + " WHERE accessrole_id = :accessrole_id "
                + "   AND application_id = :application_id ";
        Map<String, Object> params = new HashMap<>();
        params.put("application_id", appId);
        params.put("accessrole_id", accessRoleId);
        executeSQL(sqlText, params);
    }

    /**
     * Проверяет соединена ли роль с приложением
     */
    private boolean isLinkedWithRole(Integer accessRoleId, Integer appId) {
        Integer count = jdbcTemplate.queryForObject(""
                        + " SELECT COUNT(*) "
                        + " FROM   applicationrole "
                        + " WHERE  accessrole_id = " + accessRoleId
                        + "   AND  application_id = " + appId,
                Integer.class);
        return count != null && count == 1;
    }
}

