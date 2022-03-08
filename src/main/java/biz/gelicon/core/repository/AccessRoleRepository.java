package biz.gelicon.core.repository;

import biz.gelicon.core.model.AccessRole;
import biz.gelicon.core.model.ControlObject;
import biz.gelicon.core.model.Proguser;
import biz.gelicon.core.security.Permission;
import biz.gelicon.core.service.ControlObjectService;
import biz.gelicon.core.utils.DatabaseUtils;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.view.ControlObjectView;
import biz.gelicon.core.view.ObjectRoleView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class AccessRoleRepository implements TableRepository<AccessRole> {

    private static final Logger logger = LoggerFactory.getLogger(AccessRoleRepository.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ControlObjectService controlObjectService;

    // Ид для роли SYSDBA
    public static final int SYSDBA_ACCESSROLE_ID = 1;


    public List<AccessRole> findByUser(Integer progUserId) {
        List<AccessRole> accessRoleList;
        if (progUserId == Proguser.SYSDBA_PROGUSER_ID) {
            // dav SYSDBA имеет доступ на все роли
            accessRoleList = findQuery(""
                    + " SELECT DISTINCT"
                    + "        AR.* "
                    + " FROM   accessrole AR "
                    + " WHERE  AR.accessrole_visible != 0 ");
        } else {
            accessRoleList = findQuery(""
                            + " SELECT DISTINCT"
                            + "        AR.* "
                            + " FROM   accessrole AR "
                            + "        INNER JOIN proguserrole PGR "
                            + "          ON PGR.accessrole_id = AR.accessrole_id "
                            + " WHERE  AR.accessrole_visible != 0 "
                            + "   AND  (PGR.proguser_id = :proguser_id)",
                    "proguser_id", progUserId);
        }
        return accessRoleList;
    }

    public List<ObjectRoleView> findAllObjectRoles() {
        String sqlText = "" +
                "SELECT DISTINCT " +
                "cor.accessrole_id, " +
                "cor.controlobject_id, " +
                "co.controlobject_url, " +
                "cor.sqlaction_id " +
                "FROM   controlobjectrole cor " +
                "INNER JOIN accessrole ar ON ar.accessrole_id=cor.accessrole_id " +
                "INNER JOIN controlobject co ON co.controlobject_id=cor.controlobject_id " +
                "WHERE ar.accessrole_visible!=0";
        return findQuery(ObjectRoleView.class, sqlText);
    }

    /**
     * Связывает пользователя с ролью
     */
    public void bindWithProgUser(Integer accessRoleId, Integer progUserId) {
        // Роль SYSDBA можно связать только с SYSDBA и наоборот
        if ((accessRoleId == SYSDBA_ACCESSROLE_ID && progUserId != Proguser.SYSDBA_PROGUSER_ID)
                || (progUserId == Proguser.SYSDBA_PROGUSER_ID
                && accessRoleId != SYSDBA_ACCESSROLE_ID)) {
            return;
        }
        String sqlText =
                "INSERT INTO proguserrole (proguserrole_id, proguser_id,accessrole_id) " +
                        "VALUES (:id,:proguser_id,:accessrole_id)";
        Map<String, Object> map = new HashMap<>();
        map.put("id", DatabaseUtils.sequenceNextValue("proguserrole_id_gen"));
        map.put("proguser_id", progUserId);
        map.put("accessrole_id", accessRoleId);
        executeSQL(sqlText, map);
    }

    /**
     * Отвязывает пользователя с ролью
     *
     * @param accessRoleId роль
     * @param progUserId юзер
     */
    public void unbindProgUser(Integer accessRoleId, Integer progUserId) {
        // Роль SYSDBA нельзя отвязать от SYSDBA
        if (accessRoleId == SYSDBA_ACCESSROLE_ID && progUserId == Proguser.SYSDBA_PROGUSER_ID) {
            return;
        }
        String sqlText = ""
                + " DELETE FROM proguserrole "
                + " WHERE accessrole_id = :accessrole_id "
                + "   AND proguser_id = :proguser_id ";
        Map<String, Object> map = new HashMap<>();
        map.put("proguser_id", progUserId);
        map.put("accessrole_id", accessRoleId);
        executeSQL(sqlText, map);
    }

    public void unbindProgUser(Integer progUserId) {
        String sqlText =
                "DELETE FROM proguserrole " +
                        "WHERE proguser_id=:proguser_id ";
        Map<String, Object> map = new HashMap<>();
        map.put("proguser_id", progUserId);
        executeSQL(sqlText, map);
    }


    /**
     * Связывает контролируемый объект с ролью
     */
    public void bindWithControlObject(Integer accessRoleId, Integer controlObjectId,
            Permission permission) {
        String sqlText = " "
                + " INSERT INTO controlobjectrole ("
                + "   controlobjectrole_id,"
                + "   controlobject_id,"
                + "   accessrole_id,"
                + "   sqlaction_id) "
                + " VALUES ("
                + "   :id,"
                + "   :controlobject_id,"
                + "   :accessrole_id,"
                + "   :sqlaction_id"
                + ")";
        Map<String, Object> map = new HashMap<>();
        map.put("id", DatabaseUtils.sequenceNextValue("controlobjectrole_id_gen"));
        map.put("controlobject_id", controlObjectId);
        map.put("accessrole_id", accessRoleId);
        map.put("sqlaction_id", permission.ordinal());
        executeSQL(sqlText, map);
    }

    /**
     * Разрушает связь контролируемого объекта с ролью
     */
    public void unbindControlObject(Integer accessRoleId, Integer controlObjectId) {
        String sqlText =
                "DELETE FROM controlobjectrole " +
                        "WHERE accessrole_id=:accessrole_id AND controlobject_id=:controlobject_id ";
        Map<String, Object> map = new HashMap<>();
        map.put("controlobject_id", controlObjectId);
        map.put("accessrole_id", accessRoleId);
        executeSQL(sqlText, map);
    }


    @Override
    public void create() {
        Resource resource = new ClassPathResource("sql/400230-accessrole.sql");
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(resource);
        databasePopulator.setSqlScriptEncoding("UTF-8");
        databasePopulator.execute(Objects.requireNonNull(jdbcTemplate.getDataSource()));
        logger.info("accessrole created");
    }

    @Override
    public int load() {
        AccessRole[] data = new AccessRole[]{
                new AccessRole(1, "SYSDBA", "Полный доступ на все", 1),
                new AccessRole(2, "ADMIN", "Администрирование", 1),
                new AccessRole(3, "EDIZM", "Единицы измерения", 1),
        };
        insert(Arrays.asList(data));
        logger.info(String.format("%d accessrole loaded", data.length));
        DatabaseUtils.setSequence("accessrole_id_gen", data.length + 1);

        // Пользователь SYSDBA всегда связан с ролью SYSDBA
        bindWithProgUser(1, 1);

        // Пользователь 2-ADMIN связан с ролью 2-ADMIN
        bindWithProgUser(2, 2);
        // Пользователь 2-ADMIN связан с ролью 3-EDIZM
        bindWithProgUser(3, 2);

        //Роль 3- EDIZM связана с всеми функциями edizm
        // Единицы измерения: Получение списка объектов
        bindWithControlObject(3, 1, Permission.EXECUTE);
        // Единицы измерения: Получение объекта по идентификатору
        bindWithControlObject(3, 2, Permission.EXECUTE);
        // Единицы измерения: Сохранение - вставка
        bindWithControlObject(3, 3, Permission.EXECUTE);
        // Единицы измерения: Сохранение - изменение
        bindWithControlObject(3, 4, Permission.EXECUTE);
        // Единицы измерения: Удаление
        bindWithControlObject(3, 5, Permission.EXECUTE);

        // Роль 2-ADMIN связана со всеми контролируемыми объектами администрирования
        // Добавим все что загружено вручную
        List<ControlObject> controlObjectList = controlObjectService.getAllList();
        controlObjectList.forEach(controlObject -> {
            bindWithControlObject(2, controlObject.getControlObjectId(), Permission.EXECUTE);
        });
        // Роль в модуле
        //bindWithControlObject(2, 1, Permission.EXECUTE); // Единицы измерения: Получение списка объектов

        return data.length;
    }

}

