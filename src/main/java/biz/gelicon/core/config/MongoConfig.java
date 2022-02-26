package biz.gelicon.core.config;

import biz.gelicon.core.audit.AuditRecord;
import biz.gelicon.core.audit.AuditUtils;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.SslSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;

@Configuration
public class MongoConfig {
    private static final Logger logger = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.host}")
    private String mongoDBHost;
    @Value("${spring.data.mongodb.port}")
    private Integer mongoDBPort;
    @Value("${gelicon.audit:false}")
    private Boolean audit;
    @Value("${spring.data.mongodb.sslEnabled:false}")
    private Boolean sslEnabled;

    private MongoClient createMongoClient() {
        if(!audit) {
            return null;
        }
        ConnectionString connectionString = new ConnectionString(String.format("mongodb://%s:%d"+(sslEnabled?"/?tls=true":""),mongoDBHost,mongoDBPort));
        MongoClientSettings mongoClientSettings;
        // включен SSL
        if(sslEnabled) {
            // отключаем доверенные сертификаты
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs,String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs,String authType) {}
                    }
            };

            final SSLContext sslContext;
            try {
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, null);
            } catch (NoSuchAlgorithmException | KeyManagementException ex) {
                logger.error(ex.getMessage(),ex);
                throw new RuntimeException(ex);
            }

            mongoClientSettings = MongoClientSettings.builder()
                    .applyToSslSettings(builder -> {
                        builder.enabled(true);
                        builder.context(sslContext);
                        builder.invalidHostNameAllowed(true);
                    })
                    .applyConnectionString(connectionString)
                    .build();

            return MongoClients.create(mongoClientSettings);

        } else {
            mongoClientSettings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();
        }
        return MongoClients.create(mongoClientSettings);

    }

    @Bean
    public MongoTemplateFactory mongoTemplateFactory() throws Exception {
        return new MongoTemplateFactory();
    }

    public class MongoTemplateFactory {
        MongoTemplate current;
        MongoClient mongoClient;

        public MongoTemplate getMongoTemplate(LocalDate date) {
            if(!audit) return null;
            String dbName = AuditUtils.buildDatabaseName(date);
            return getMongoTemplate(dbName);
        }

        public MongoTemplate getMongoTemplate(String dbName) {
            if(!audit) return null;
            if(current==null || !current.getDb().getName().equals(dbName)) {
                logger.info("Open/Create audit database: {}}",dbName);
                // релогин только через клиента. Нужно освободить БД предыдущего месяца
                if(current!=null) {
                    logger.info("Close audit database: {}}",current.getDb().getName());
                    mongoClient.close();
                }
                mongoClient =  createMongoClient();
                current = new MongoTemplate(mongoClient, dbName);
                initIndicesAfterStartup(current);
            }
            return current;
        }

        public MongoTemplate getMongoTemplateForRead(MongoClient cli,String dbName) {
            if(!audit) return null;
            MongoTemplate template = new MongoTemplate(cli, dbName);
            initIndicesAfterStartup(template);
            return template;
        }

        public MongoClient newMongoClient() {
            return createMongoClient();
        }


    }

    public void initIndicesAfterStartup(MongoTemplate template) {
        MappingContext ctx = template.getConverter().getMappingContext();

        MongoPersistentEntityIndexResolver resolver = new MongoPersistentEntityIndexResolver(ctx);

        IndexOperations idxOps = template.indexOps(AuditRecord.class);
        resolver.resolveIndexFor(AuditRecord.class).forEach(idxOps::ensureIndex);
        // добавляем fulltext индекс
        TextIndexDefinition fullTextIdx = new TextIndexDefinition.TextIndexDefinitionBuilder()
                .named("full_text_search")
                .withDefaultLanguage("russian")
                .onAllFields()
                .build();
        idxOps.ensureIndex(fullTextIdx);

    }

}
