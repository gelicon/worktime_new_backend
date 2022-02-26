package biz.gelicon.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@ConfigurationProperties
public class MainApplication {

    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

    private static ApplicationContext applicationContext;

    public static void main(String[] args) {
        logger.info("SpringApplication.run...");
        SpringApplication.run(MainApplication.class, args);
        logger.info("SpringApplication.run...Ok");

    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext ac) {
        applicationContext = ac;
    }

}

