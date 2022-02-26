package biz.gelicon.core.config;

import biz.gelicon.core.convertors.DateDeserializer;
import biz.gelicon.core.jobs.JobDispatcher;
import biz.gelicon.core.jobs.TokenRunnable;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.catalina.Context;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Конфигурирование приложения
 */
@Configuration
@EnableScheduling
public class Config {

    private static final Logger logger = LoggerFactory.getLogger(Config.class);

    public final static int CURRENT_VERSION_V1 = 1;

    public final static int CURRENT_VERSION = CURRENT_VERSION_V1;

    public final static EditionTag CURRENT_EDITION_TAG = EditionTag.GELICON_UTILITIES;

    @Value("${spring.datasource.url}")
    private String dataBaseUrl;
    @Value("${spring.datasource.username}")
    private String dataBaseUser;
    @Value("${spring.datasource.password}")
    private String dataBasePassword;
    @Value("${spring.datasource.driver-class-name}")
    private String dataBaseDriverClass;


    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                logger.info("addCorsMappings");
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "PUT", "POST", "PATCH", "DELETE", "OPTIONS")
                        .exposedHeaders("Content-Disposition");
            }
        };
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setDefaultCharset(StandardCharsets.UTF_8);
        SimpleModule module = new SimpleModule();

        // десериализатор дат, вместо стандартного
        module.addDeserializer(Date.class, new DateDeserializer());

        jsonConverter.getObjectMapper().registerModule(module);
        return jsonConverter;
    }

    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        return new TomcatServletWebServerFactory() {
            @Override
            protected TomcatWebServer getTomcatWebServer(
                    org.apache.catalina.startup.Tomcat tomcat) {
                tomcat.enableNaming();

                // JNDI lookup with embedded tomcat:  https://github.com/spring-projects/spring-boot/issues/2308
                Context context = (Context) tomcat.getHost().findChild("");
                Thread.currentThread().setContextClassLoader(context.getLoader().getClassLoader());

                return super.getTomcatWebServer(tomcat);
            }

            @Override
            protected void postProcessContext(Context context) {
                // Это JNDI datasource через pool Hikari для отчетов
                // Ищется по сторке java:comp/env/jdbc/gelicon-core-datasource
                ContextResource resource = new ContextResource();
                resource.setName("jdbc/gelicon-core-datasource");
                resource.setType(DataSource.class.getName());
                resource.setProperty("factory", "com.zaxxer.hikari.HikariJNDIFactory");
                resource.setProperty("driverClassName", dataBaseDriverClass);
                // Для Hikari jdbcUrl вместо url
                resource.setProperty("jdbcUrl", dataBaseUrl);
                resource.setProperty("username", dataBaseUser);
                resource.setProperty("password", dataBasePassword);
                context.getNamingResources()
                        .addResource(resource);
                super.postProcessContext(context);

            }
        };
    }

    @Value("${gelicon.orm.showSQL:false}")
    private boolean showSQL;

    @Bean
    public Boolean getShowSQLFlag() {
        return showSQL;
    }

    @Bean
    public JobDispatcher jobACL() {
        return new JobDispatcher();
    }

    @Bean
    public JobDispatcher jobAudit() {
        return new JobDispatcher();
    }

    @Bean
    public JobDispatcher jobTokenUpdate() {
        // асинхронная задача с группировкой по токенам и выявлением последней по времени
        // записи для каждого токена
        return new JobDispatcher(60*1000,buf->{
            Map<String, List<Runnable>> tokMap = buf.stream()
                    .collect(Collectors.groupingBy(runnable -> ((TokenRunnable) runnable).getToken()));
            tokMap.keySet().stream()
                    .forEach(token->{
                        List<Runnable> list = tokMap.get(token);
                        Runnable last = list.get(list.size() - 1);
                        logger.info("pull token update job {}",last.hashCode());
                        try {
                            last.run();
                        } catch (Exception ex) {
                            logger.error(ex.getMessage(),ex);
                        }
                    });

        });
    }


    /**
     * Транзакционный менеджер БЕЗ маркировки RollbackOnly. Нужен в случаях когда
     * нельзя откатывать на основании возникшего, но обработанного Exception
     * Стандартный менеджер транзакций с setGlobalRollbackOnParticipationFailure(true)
     * @return
     */
    @Bean
    @Qualifier("txNoRollbackFlagManager")
    public PlatformTransactionManager txNoRollbackFlagManager(DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
        dataSourceTransactionManager.setGlobalRollbackOnParticipationFailure(false);
        return dataSourceTransactionManager;
    }

}
