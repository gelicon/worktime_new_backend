package biz.gelicon.core.repository;

import biz.gelicon.core.config.Config;
import biz.gelicon.core.model.ControlObject;
import biz.gelicon.core.security.Permission;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ControlObjectRepository implements TableRepository<ControlObject> {

    private static final Logger logger = LoggerFactory.getLogger(ControlObjectRepository.class);
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Каскадное удаление со всеми ссылками
     * Удаляет из таблиц controlobjectrole
     * //TODO подумать о deleteCascadeAll
     * @param controlObjectId
     */
    public void deleteCascade(Integer controlObjectId){
        // Таблица controlObject
        String sqlText = ""
                + " DELETE FROM controlobjectrole "
                + " WHERE  controlobject_id = :controlobject_id ";
        Map<String, Object> params = new HashMap<>();
        params.put("controlobject_id", controlObjectId);
        executeSQL(sqlText, params);
        delete(controlObjectId);
    }


    /**
     * Подправляет значение генератора на максимальное + 1
     */
    public void correctSequence() {
        DatabaseUtils.setSequence("controlobject_id_gen", getMaxId() + 1);
    }

    public boolean isLinkedWithRole(Integer accessRoleId, Integer controlObjectId,
            Permission perm) {
        String sqlText = "SELECT COUNT(*) " +
                "FROM controlobjectrole " +
                "WHERE accessrole_id=:accessrole_id AND " +
                "controlobject_id=:controlobject_id AND " +
                "sqlaction_id=:sqlaction_id";
        Map<String, Object> params = new HashMap<>();
        params.put("controlobject_id", controlObjectId);
        params.put("accessrole_id", accessRoleId);
        params.put("sqlaction_id", perm.ordinal());
        OrmUtils.logSQL(sqlText);
        Integer count = jdbcTemplate.queryForObject(sqlText, params, Integer.class);
        return count.intValue() > 0;
    }

    public int allow(Integer accessRoleId, Integer controlObjectId, Permission perm) {
        if (isLinkedWithRole(accessRoleId, controlObjectId, perm)) {
            return 0;
        }
        String sqlText = buildInsertClause("controlobjectrole",
                new String[]{"controlobjectrole_id", "controlobject_id", "accessrole_id",
                        "sqlaction_id"},
                new String[]{"controlobjectrole_id", "controlobject_id", "accessrole_id",
                        "sqlaction_id"});
        int id = DatabaseUtils.sequenceNextValue("controlobjectrole_id_gen");
        Map<String, Object> params = new HashMap<>();
        params.put("controlobjectrole_id", id);
        params.put("controlobject_id", controlObjectId);
        params.put("accessrole_id", accessRoleId);
        params.put("sqlaction_id", perm.ordinal());
        executeSQL(sqlText, params);
        return id;
    }

    public void deny(Integer accessRoleId, Integer controlObjectId, Permission perm) {
        String sqlText = "DELETE FROM controlobjectrole " +
                "WHERE accessrole_id=:accessrole_id AND controlobject_id=:controlobject_id  AND sqlaction_id=:sqlaction_id";
        Map<String, Object> params = new HashMap<>();
        params.put("controlobject_id", controlObjectId);
        params.put("accessrole_id", accessRoleId);
        params.put("sqlaction_id", perm.ordinal());
        executeSQL(sqlText, params);
    }

    @Override
    public void create() {
        Resource resource = new ClassPathResource("sql/400225-controlobject.sql");
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(resource);
        databasePopulator.setSqlScriptEncoding("UTF-8");
        databasePopulator.execute(jdbcTemplate.getJdbcTemplate().getDataSource());
        logger.info("controlobject created");
    }

    @Override
    public int load() {
        // todo
        /*
        String v1 = "/v" + Config.CURRENT_VERSION + "/";
        ControlObject[] data = new ControlObject[]{
                new ControlObject(1, "Просмотр списка единиц измерения",
                        v1 + "apps/refbooks/edizm/edizm/getlist"),
                new ControlObject(2,
                        "Получение единицы измерения с целью редактирования/добавления",
                        v1 + "apps/refbooks/edizm/edizm/get"),
                new ControlObject(3, "Сохранение единицы измерения",
                        v1 + "apps/refbooks/edizm/edizm/save"),
                new ControlObject(4, "Удаление единицы измерения",
                        v1 + "apps/refbooks/edizm/edizm/delete"),
                new ControlObject(5, "Получение единицы измерения с целью редактирования",
                        v1 + "apps/refbooks/edizm/edizm/get#edit"),
                new ControlObject(6, "Получение единицы измерения с целью добавления",
                        v1 + "apps/refbooks/edizm/edizm/get#add"),
                new ControlObject(7, "Сохранение новой единицы измерения",
                        v1 + "apps/refbooks/edizm/edizm/save#ins"),
                new ControlObject(8, "Обновление единицы измерения",
                        v1 + "apps/refbooks/edizm/edizm/save#upd"),
        };
        insert(Arrays.asList(data));
        logger.info(String.format("%d controlobject loaded", data.length));
        DatabaseUtils.setSequence("controlobject_id_gen", data.length + 1);
        return data.length;

         */
        return 0;
    }



}

