package biz.gelicon.core.worktime;

import biz.gelicon.core.model.Application;
import biz.gelicon.core.model.ControlObject;
import biz.gelicon.core.repository.AccessRoleRepository;
import biz.gelicon.core.repository.ApplicationRepository;
import biz.gelicon.core.repository.TableRepository;
import biz.gelicon.core.security.Permission;
import biz.gelicon.core.service.ApplicationService;
import biz.gelicon.core.service.ControlObjectService;
import biz.gelicon.core.utils.DatabaseUtils;
import biz.gelicon.core.utils.UsefulUtils;
import biz.gelicon.core.worktime.controllers.department.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Пересоздание всех таблиц базы данных worktime
 */
@Repository
public class RecreateDatabaseWorktime {

    static Logger logger = LoggerFactory.getLogger(RecreateDatabaseWorktime.class);

    @Autowired
    private PlatformTransactionManager transactionManager;
    DefaultTransactionDefinition defaultTransactionDefinition;
    TransactionStatus transactionStatus;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DepartmentRepository departmentRepositoryy;
    @Autowired
    protected TableRepository[] registerRepos;

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    ControlObjectService controlObjectService;

    @Autowired
    AccessRoleRepository accessRoleRepository;

    public void registerRepo() {
        // здесь регистриуются репозитории. Внимание! В правильном порядке СОЗДАНИЯ таблиц
        registerRepos = new TableRepository[]{
                // worktime
                departmentRepositoryy,
        };
    }

    /**
     * Создание всех таблиц базы данных
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void create() {
        logger.info("Creating tables ...");
        Arrays.asList(registerRepos).forEach(r -> {
            r.create();
        });
        logger.info("Creating tables ... Ok");
    }

    /**
     * Удаление всех таблиц базы данных
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void drop() {
        logger.info("Dropping tables ...");
        // удаляем таблицы в обратном порядке
        Arrays.asList(registerRepos).stream()
                .collect(UsefulUtils.toListReversed()).forEach(r -> {
                    r.dropForTest();
                });
        logger.info("Dropping tables ... Ok");
    }

    /**
     * Загрузка начальных данных в таблицы
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void load() {
        logger.info("Loading tables ...");
        Arrays.asList(registerRepos).forEach(r -> r.load());
        logger.info("Loading tables ... Ok");
    }

    /**
     * Удаление данных из всех таблиц
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void delete() {
        logger.info("Deleting tables ...");
        Arrays.asList(registerRepos).forEach(r -> r.deleteAll());
        logger.info("Deleting tables ... Ok");
    }

    /**
     * Пересоздание всех таблиц и прогрузка данными
     */
    public void recreate() {
        // Открываем таранзакцию
        logger.info("Recreating database for project Worktime");
        defaultTransactionDefinition = new DefaultTransactionDefinition();
        transactionStatus = transactionManager.getTransaction(defaultTransactionDefinition);
        try {
            registerRepo();
            drop();
            create();
            load();
            applicationLoad(); // Добавить приложения
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            String errText = "Error. Transaction will be rolled back";
            logger.error(errText, e);
            transactionManager.rollback(transactionStatus);
            throw new RuntimeException(errText, e);
        }
    }

    /**
     * Прогрузка таблицы application для проекта worktime Доступ для роли ADMIN на все приложения
     * проекта
     */
    @Transactional(propagation = Propagation.REQUIRED)
    private void applicationLoad() {
        logger.info("Applications for project Worktime...");
        applicationRepository.insert(new Application(
                        200,
                        Application.TYPE_GELICON_CORE_APP,
                        "refbooks.orgstruct.department",
                        // Позиция в меню - Справочники-Оргструктура-Отделы
                        "Справочник отделов",
                        "orgstruct", // application_exe
                        "department" // Подмодуль
                )
        );
        // Дадим доступ для роли (2	ADMIN	Администрирование) на приложение
        applicationRepository.allow(2, 200);
        // Роль 2-ADMIN связана со всеми контролируемыми объектами
        // не только администрирования, но и worktime
        // Добавим все что загружено
        List<ControlObject> controlObjectList = controlObjectService.getAllList();
        controlObjectList.forEach(controlObject -> {
            accessRoleRepository.bindWithControlObject(
                    2, // ADMIN
                    controlObject.getControlObjectId(),
                    Permission.EXECUTE
            );
        });
        // Так как мы сами установили ид, то надо переустановить последовательность
        DatabaseUtils.setSequence("application_id_gen", 200);
        logger.info("Applications for project Worktime...Ok");
    }

    /**
     * Доступ для роли ADMIN на все контролируемые объекты проекта
     */
//    @Transactional(propagation = Propagation.REQUIRED)
    public void controlObjectRoleLoad() {
        // Роль 2-ADMIN связана со всеми контролируемыми объектами
        // не только администрирования, но и worktime
        // Добавим все что загружено
        List<ControlObject> controlObjectList = controlObjectService.getAllList();
        logger.info("controlObjectRole for project Worktime loading...");
        controlObjectList.forEach(controlObject -> {
            accessRoleRepository.bindWithControlObject(
                    2, // ADMIN
                    controlObject.getControlObjectId(),
                    Permission.EXECUTE
            );
        });
        logger.info("controlObjectRole for project Worktime loading...Ok");
    }


}
