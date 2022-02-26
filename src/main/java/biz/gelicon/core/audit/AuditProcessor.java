package biz.gelicon.core.audit;

import biz.gelicon.core.config.MongoConfig;
import biz.gelicon.core.repository.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class AuditProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AuditProcessor.class);
    private static final String AUDIT_COLLECTION_NAME = "audit";

    @Autowired
    private MongoConfig.MongoTemplateFactory mongoTemplateFactory;

    private PublishSubject<AuditRecord> queue;

    {
        queue = PublishSubject.create();
        queue.observeOn(Schedulers.computation()) // включаем пул thread
                .subscribe(record -> {
                    try {
                        processAuditRecord(record);
                    } catch (Throwable ex) {
                        logger.error("Ошибка аудита: "+ex.getMessage(),ex);
                    }
                });
    }

    private void processAuditRecord(AuditRecord record) {
        MongoTemplate mongoTemplate = mongoTemplateFactory.getMongoTemplate(LocalDate.now());
        if(mongoTemplate!=null) {
            if(!mongoTemplate.collectionExists(AUDIT_COLLECTION_NAME)) {
                mongoTemplate.createCollection(AUDIT_COLLECTION_NAME);
            }
            // для удаления сделаем на каждый id отдельную запись
            if(AuditKind.values()[record.getKind()] == AuditKind.CALL_FOR_DELETE) {
                List<Integer> ids = new ArrayList<>();
                ids.addAll(record.getIdValue());
                ids.forEach(id->{
                    record.setIdValue(Arrays.asList(new Integer[]{id}));
                    mongoTemplate.insert(record);
                });
            } else {
                mongoTemplate.insert(record);
            }
        } else {
            logger.info("Audit disable: {}",record.toString());
        }

    }

    public void pushToProcess(AuditRecord record) {
        queue.onNext(record);
    }
}
