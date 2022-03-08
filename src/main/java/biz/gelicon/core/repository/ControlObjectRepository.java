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
import java.util.Map;
import java.util.Objects;

@Repository
public class ControlObjectRepository implements TableRepository<ControlObject> {

    private static final Logger logger = LoggerFactory.getLogger(ControlObjectRepository.class);
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Каскадное удаление со всеми ссылками Удаляет из таблиц controlobjectrole //TODO подумать о
     * deleteCascadeAll
     *
     * @param controlObjectId контролируемый объект
     */
    public void deleteCascade(Integer controlObjectId) {
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
        if (count == null) return false;
        return count > 0;
    }

    public void allow(Integer accessRoleId, Integer controlObjectId, Permission perm) {
        if (isLinkedWithRole(accessRoleId, controlObjectId, perm)) {
            return;
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
        databasePopulator.execute(
                Objects.requireNonNull(jdbcTemplate.getJdbcTemplate().getDataSource()));
        logger.info("controlobject created");
    }

    @Override
    public int load() {
        // Загрузим для теста
        // Остальные загружаются при старте из аннотаций
        String v1 = "/v" + Config.CURRENT_VERSION + "/";
        ControlObject[] data = new ControlObject[]{
                // Единицы измерения
                new ControlObject(1, "Единицы измерения: Получение списка объектов",
                        v1 + "apps/refbooks/edizm/edizm/getlist"),
                new ControlObject(2,
                        "Единицы измерения: Получение объекта по идентификатору",
                        v1 + "apps/refbooks/edizm/edizm/get"),
                new ControlObject(3, "Единицы измерения: Сохранение - вставка",
                        v1 + "apps/refbooks/edizm/edizm/save#ins"),
                new ControlObject(4, "Единицы измерения: Сохранение - изменение",
                        v1 + "apps/refbooks/edizm/edizm/save#upd"),
                new ControlObject(5, "Единицы измерения: Удаление",
                        v1 + "apps/refbooks/edizm/edizm/delete"),
                // Группы пользователей
                new ControlObject(50, "Группы пользователей: Удаление",
                        v1 + "apps/admin/credential/progusergroup/delete"),
                new ControlObject(51, "Группы пользователей: Получение списка объектов",
                        v1 + "apps/admin/credential/progusergroup/getlist"),
                new ControlObject(52, "Группы пользователей: Получение объекта по идентификатору",
                        v1 + "apps/admin/credential/progusergroup/get"),
                new ControlObject(53, "Группы пользователей: Сохранение - вставка",
                        v1 + "apps/admin/credential/progusergroup/save#ins"),
                new ControlObject(54, "Группы пользователей: Сохранение - изменение",
                        v1 + "apps/admin/credential/progusergroup/save#upd"),
                // Роли
                new ControlObject(70, "Роли: Получение списка объектов",
                        v1 + "apps/admin/credential/accessrole/getlist"),
                new ControlObject(71, "Роли: Получение объекта по идентификатору",
                        v1 + "apps/admin/credential/accessrole/get"),
                new ControlObject(72, "Роли: Удаление",
                        v1 + "apps/admin/credential/accessrole/delete"),
                new ControlObject(73, "Роли: Сохранение - изменение",
                        v1 + "apps/admin/credential/accessrole/save#upd"),
                new ControlObject(74, "Роли: Сохранение - вставка",
                        v1 + "apps/admin/credential/accessrole/save#ins"),
                // Пользователи
                new ControlObject(100, "Пользователи: Установка пароля у пользователя",
                        v1 + "apps/admin/credential/proguser/setpswd"),
                new ControlObject(101, "Пользователи: Сохранить список ролей пользователя, удалить ненужные - изменение",
                        v1 + "apps/admin/credential/proguser/roles/save#upd"),
                new ControlObject(102, "Пользователи: Сохранить список ролей пользователя, удалить ненужные - вставка",
                        v1 + "apps/admin/credential/proguser/roles/save#ins"),
                new ControlObject(103, "Пользователи: Смена своего пароля. Необходимо знать старый пароль",
                        v1 + "apps/admin/credential/proguser/changepswd"),
                new ControlObject(104, "Пользователи: Список ролей пользователя для редактора ролей",
                        v1 + "apps/admin/credential/proguser/roles/get"),
                new ControlObject(105, "Пользователи: Получение списка объектов",
                        v1 + "apps/admin/credential/proguser/getlist"),
                new ControlObject(106, "Пользователи: Получение объекта по идентификатору",
                        v1 + "apps/admin/credential/proguser/get"),
                new ControlObject(107, "Пользователи: Удаление",
                        v1 + "apps/admin/credential/proguser/delete"),
                new ControlObject(108, "Пользователи: Сохранение - вставка",
                        v1 + "apps/admin/credential/proguser/save#ins"),
                new ControlObject(109, "Пользователи: Сохранение - изменение",
                        v1 + "apps/admin/credential/proguser/save#upd"),
                new ControlObject(110, "Пользователи: Список пользователей по поисковой сроке",
                        v1 + "apps/admin/credential/proguser/find"),
                // Контролируемый объект
                new ControlObject(201, "Контролируемый объект: Отнять доступ на контролируемые объекты у роли",
                        v1 + "apps/admin/credential/controlobject/deny"),
                new ControlObject(202, "Контролируемый объект: Дать доступ на контролируемые объекты для роли",
                        v1 + "apps/admin/credential/controlobject/allow"),
                new ControlObject(203, "Контролируемый объект: Получение списка объектов",
                        v1 + "apps/admin/credential/controlobject/getlist"),
                new ControlObject(204, "Контролируемый объект: Получение объекта по идентификатору",
                        v1 + "apps/admin/credential/controlobject/get"),
                new ControlObject(205, "Контролируемый объект: Удаление",
                        v1 + "apps/admin/credential/controlobject/delete"),
                new ControlObject(206, "Контролируемый объект: Сохранение - изменение",
                        v1 + "apps/admin/credential/controlobject/save#upd"),
                new ControlObject(207, "Контролируемый объект: Сохранение - вставка",
                        v1 + "apps/admin/credential/controlobject/save#ins"),
                // Модули
                new ControlObject(250, "Модуль: Получение списка объектов",
                        v1 + "apps/admin/credential/application/getlist"),
                // Роль в модуле
                new ControlObject(301, "Роль в модуле: Отнять доступ на объект \"Доступ к модулям\" у роли",
                        v1 + "apps/admin/credential/applicationrole/deny"),
                new ControlObject(302, "Роль в модуле: Дать доступ на объект \"Доступ к модулям\" для роли",
                        v1 + "apps/admin/credential/applicationrole/allow"),
                new ControlObject(303, "Роль в модуле: Получение списка объектов",
                        v1 + "apps/admin/credential/applicationrole/getlist"),
                new ControlObject(304, "Роль в модуле: Список объектов \"Модуль\", доступных текущему пользователю",
                        v1 + "apps/admin/credential/applicationrole/accesslist"),
                // Доступ для роли на объекты
                new ControlObject(401, "Доступ для роли на объекты: Получение списка объектов",
                        v1 + "apps/admin/credential/controlobjectrole/getlist"),
                new ControlObject(402, "Доступ для роли на объекты: Получение объекта по идентификатору",
                        v1 + "apps/admin/credential/controlobjectrole/get"),
                new ControlObject(403, "Доступ для роли на объекты: Удаление",
                        v1 + "apps/admin/credential/controlobjectrole/delete"),
                new ControlObject(404, "Доступ для роли на объекты: Сохранение - изменение",
                        v1 + "apps/admin/credential/controlobjectrole/save#upd"),
                new ControlObject(405, "Доступ для роли на объекты: Сохранение - вставка",
                        v1 + "apps/admin/credential/controlobjectrole/save#ins"),
                // Сессии
                new ControlObject(501, "Сессии: Получение списка объектов",
                        v1 + "apps/admin/audit/session/getlist"),
                new ControlObject(502, "Сессии: Закрытие сессий",
                        v1 + "apps/admin/audit/session/close")
        };
        insert(Arrays.asList(data));
        logger.info(String.format("%d controlobject loaded", data.length));
        DatabaseUtils.setSequence("controlobject_id_gen", 1000);
        return data.length;
    }


}

