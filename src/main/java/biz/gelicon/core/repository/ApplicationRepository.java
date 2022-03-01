package biz.gelicon.core.repository;

import biz.gelicon.core.model.Application;
import biz.gelicon.core.utils.DatabaseUtils;
import biz.gelicon.core.utils.OrmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ApplicationRepository implements TableRepository<Application> {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationRepository.class);
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Возвращает Application-ы из таблицы application для application_exe
     */
    public List<Integer> getApplicationIdByUnitName(String applicationExe) {
        if (applicationExe == null || applicationExe.isEmpty()) {
            return new ArrayList<>(); // Чтобы не проверять после вызова
        }
        // todo определиться как искать. сейчас по application_exe
        return findQuery(""
                        + " SELECT * "
                        + " FROM   application "
                        + " WHERE  UPPER(application_exe) = :application_exe ",
                "application_exe", applicationExe.toUpperCase())
                .stream()
                .map(a -> a.applicationId)
                .collect(Collectors.toCollection(
                        ArrayList::new));
    }

    /**
     * Проверяет есть ли связь роли и приложения
     */
    private boolean isLinkedWithRole(Integer accessRoleId, Integer appId) {
        String sqlText = ""
                + " SELECT COUNT(*) "
                + " FROM   applicationrole "
                + " WHERE  accessrole_id = :accessrole_id "
                + "   AND  application_id = :application_id";
        Map<String, Object> params = new HashMap<>();
        params.put("application_id", appId);
        params.put("accessrole_id", accessRoleId);
        OrmUtils.logSQL(sqlText);
        Integer count = jdbcTemplate.queryForObject(sqlText, params, Integer.class);
        return count > 0;
    }

    /**
     * Добавляет запись в applicationrole связывая раль с приложением
     */
    public int allow(Integer accessRoleId, Integer appId) {
        if (isLinkedWithRole(accessRoleId, appId)) {
            return 0;
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
        return id;
    }

    /**
     * Удялает запись из applicationrole Удаляет связь роли с приложением
     */
    public void deny(Integer accessRoleId, Integer appId) {
        String sqlText = ""
                + " DELETE FROM applicationrole "
                + " WHERE accessrole_id = :accessrole_id "
                + "   AND application_id = :application_id";
        Map<String, Object> params = new HashMap<>();
        params.put("application_id", appId);
        params.put("accessrole_id", accessRoleId);
        executeSQL(sqlText, params);
    }

    @Override
    public void create() {
        Resource resource = new ClassPathResource("sql/400300-application.sql");
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(resource);
        databasePopulator.setSqlScriptEncoding("UTF-8");
        databasePopulator.execute(jdbcTemplate.getJdbcTemplate().getDataSource());
        logger.info("application created");
    }

    @Override
    public int load() {
        Application[] data = new Application[]{
                new Application(1, Application.TYPE_GELICON_CORE_APP,
                        "admin.credential.progusergroup", "Группы пользователей", "credential", null),
                new Application(2, Application.TYPE_GELICON_CORE_APP,
                        "admin.credential.application", "Приложения", "credential", null),
                new Application(3, Application.TYPE_GELICON_CORE_APP,
                        "admin.credential.accessrole", "Роли доступа", "credential", null),
                new Application(4, Application.TYPE_GELICON_CORE_APP,
                        "admin.credential.proguser", "Пользователи", "credential", null),
                new Application(5, Application.TYPE_GELICON_CORE_APP,
                        "admin.credential.applicationrole", "Доступ роли на приложение",
                        "credential", null),
                new Application(6, Application.TYPE_GELICON_CORE_APP,
                        "admin.credential.controlobjectrole",
                        "Доступ роли на контролируемый объект", "credential", null),
                new Application(7, Application.TYPE_GELICON_CORE_APP,
                        "admin.credential.proguserrole", "Доступ пользователю на роль",
                        "credential", null),
                new Application(8, Application.TYPE_GELICON_CORE_APP,
                        "admin.audit.session", "Сессии", "audit", null),
                new Application(100, Application.TYPE_GELICON_CORE_APP,
                        "refbooks.edizm.edizm", "Справочник единиц измерения", "edizm", "edizm"),
        };
        insert(Arrays.asList(data));
        int i = Collections.max(
                Arrays.stream(data).map(p -> p.applicationId).collect(Collectors.toList())
        );
        DatabaseUtils.setSequence("application_id_gen", i);

        logger.info(String.format("%d application loaded", data.length));
        return data.length;
    }

}

