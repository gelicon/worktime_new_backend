package biz.gelicon.core.repository;

import biz.gelicon.core.utils.UsefulUtils;
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

/**
 * Пересоздание всех таблиц базы данных capital
 */
@Repository
public class RecreateDatabase {

    static Logger logger = LoggerFactory.getLogger(RecreateDatabase.class);

    @Autowired
    private PlatformTransactionManager transactionManager;
    DefaultTransactionDefinition defaultTransactionDefinition;
    TransactionStatus transactionStatus;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CapCodeTypeRepository capCodeTypeRepository;
    @Autowired
    private CapCodeRepository capCodeRepository;
    @Autowired
    private ProgUserGroupRepository progUserGroupRepository;
    @Autowired
    private ProgUserRepository progUserRepository;
    @Autowired
    private ProgUserAuthRepository progUserAuthRepository;
    @Autowired
    private ControlObjectRepository controlObjectRepository;
    @Autowired
    private SqlactionRepository sqlactionRepository;
    @Autowired
    private AccessRoleRepository accessRoleRepository;
    @Autowired
    private ProguserRoleRepository proguserRoleRepository;
    @Autowired
    private ControlObjectRoleRepository controlObjectRoleRepository;
    @Autowired
    private ProguserChannelRepository proguserChannelRepository;
    @Autowired
    private ProguserCredentialRepository proguserCredentialRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private ApplicationRoleRepository applicationRoleRepository;
    @Autowired
    private ConstantRepository constantRepository;
    @Autowired
    private EdizmRepository edizmRepository;

    protected TableRepository[] registerRepos;

    public void registerRepo() {
        registerRepos = new TableRepository[]{
                capCodeTypeRepository,
                capCodeRepository,
                progUserGroupRepository,
                progUserRepository,
                progUserAuthRepository,
                controlObjectRepository,
                sqlactionRepository,
                accessRoleRepository,
                proguserRoleRepository,
                controlObjectRoleRepository,
                proguserChannelRepository,
                proguserCredentialRepository,
                applicationRepository,
                applicationRoleRepository,
                constantRepository,
                edizmRepository
                //TODO здесь регистриуются репозитории. Внимание! В правильном порядке СОЗДАНИЯ таблиц
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
                .collect(UsefulUtils.toListReversed()).forEach(r -> r.dropForTest());
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
        defaultTransactionDefinition = new DefaultTransactionDefinition();
        transactionStatus = transactionManager.getTransaction(defaultTransactionDefinition);
        try {
            registerRepo();
            drop();
            create();
            load();
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            String errText = String.format("Error. Transaction will be rolled back");
            logger.error(errText, e);
            transactionManager.rollback(transactionStatus);
            throw new RuntimeException(errText, e);
        }
    }


}
