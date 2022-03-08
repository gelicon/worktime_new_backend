package biz.gelicon.core.config;

import biz.gelicon.core.config.Config;
import biz.gelicon.core.MainApplication;
import biz.gelicon.core.artifacts.ArtifactManagerImpl;
import biz.gelicon.core.maintenance.MaintenanceSystemService;
import biz.gelicon.core.reports.ReportManagerImpl;
import biz.gelicon.core.repository.RecreateDatabase;
import biz.gelicon.core.security.ACL;
import biz.gelicon.core.utils.TableMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Запускается при запуске Spring и инициализирует все что надо
 */
@Component
public class InitApp implements ApplicationRunner {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    RecreateDatabase recreateDatabase;

    @Autowired
    ACL acl;

    @Autowired
    ReportManagerImpl reportManager;

    @Autowired
    ArtifactManagerImpl artifactManager;

    @Autowired
    MaintenanceSystemService maintenanceSystemService;

    @Value("${gelicon.run-as-test:false}")
    private Boolean runAsTest;

    /**
     * Для пересмоздания базы данных установить
     * recreatedatabase=true
     * в application.properties
     */
    @Value("${gelicon.orm.recreatedatabase:false}")
    private Boolean recreatedatabase;

    @Value("${gelicon.report.restcrictOverlapping:false}")
    private Boolean restcrictOverlappingReport;
    @Value("${gelicon.artifact.restcrictOverlapping:false}")
    private Boolean restcrictOverlappingArtifact;
    /** префикс пакетов системы */
    @Value("${gelicon.core.prefix:biz.gelicon.core}")
    private String geliconCorePrefix;

    @Value("${spring.datasource.url}")
    private String dataBaseUrl;

    /**
     * Порт на котором запускается Spring Boot
     */
    @Value("${server.port:8081}")
    private String serverPort;

    private static final Logger logger = LoggerFactory.getLogger(InitApp.class);

    public void run(ApplicationArguments args) {
        logger.info("");
        logger.info("=================================================");
        logger.info("=================================================");
        logger.info("InitApp running...");
        logger.info("Database connection string: " + dataBaseUrl);
        MainApplication.setApplicationContext(applicationContext);
        // Считаем все аннотации @Table
        TableMetadata.readTableMetadataAnnotation(geliconCorePrefix);
        if (recreatedatabase) { // Пересоздание базы данных
            logger.info("recreateDatabase...");
            recreateDatabase.recreate();
            logger.info("recreateDatabase...Ok");
        }
        // первая сборка таблицы доступа
        acl.buildAccessTable();

        if(!runAsTest) { // Все, что необходимо запускать, но не в тесте - сюда!
            // загрузка печатных форм
            reportManager.setOverlappingReportFlag(!restcrictOverlappingReport);
            reportManager.loadReports();
            // загрузка артефактов
            artifactManager.setOverlappingFlag(!restcrictOverlappingArtifact);
            artifactManager.loadArtifacts();

            reportManager.testJNDI();

            // Заполним таблицу ControlObject
            maintenanceSystemService.fillControlObject(geliconCorePrefix);

        }

        logger.info("InitApp running...Ok");
        String swaggerURL = "http://localhost:" + serverPort + "/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config";
        logger.info(swaggerURL);
        logger.info("Use token for SYSDBA=e9b3c034-fdd5-456f-825b-4c632f2053ac");
        logger.info("Use token for ADMIN=15a5a967-7a71-46f4-9af9-e3878b7fffac");
        logger.info("=================================================");

    }

}