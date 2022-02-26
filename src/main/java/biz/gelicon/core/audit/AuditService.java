package biz.gelicon.core.audit;

import biz.gelicon.core.config.MongoConfig;
import biz.gelicon.core.utils.DateUtils;
import biz.gelicon.core.utils.TwoTuple;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class AuditService {
    private static final int QUERY_PARALLELISM = 4;

    @Autowired
    private MongoConfig.MongoTemplateFactory mongoTemplateFactory;

    public TwoTuple<List<AuditRecord>,Long> query(Date datebeg, Date dateend,
                                                  List<Integer> kinds, AuditRecord pattern,
                                                  Pageable page,
                                                  String search) throws ExecutionException, InterruptedException {
        MongoClient mongoCient = mongoTemplateFactory.newMongoClient();
        if(mongoCient==null) {
            return new TwoTuple<>(new ArrayList<>(),0l);
        }
        try {
            LocalDate ldatestart = LocalDate.ofInstant(Instant.ofEpochMilli(datebeg.getTime()), ZoneId.systemDefault());
            LocalDate ldatefin = LocalDate.ofInstant(Instant.ofEpochMilli(dateend.getTime()), ZoneId.systemDefault());
            List<MongoTemplate> templates = AuditUtils.getDataBaseNames(ldatestart, ldatefin).stream()
                    .map(db -> mongoTemplateFactory.getMongoTemplateForRead(mongoCient, db))
                    .filter(Objects::nonNull) // месячные базы данных могут отсутствовать
                    .collect(Collectors.toList());
            // формируем запрос
            // устанавливаем limit так как необходимый максимум выбираемых записей
            // ограничен сверху размером страницы для показа
            Query query;
            if(search==null) {
                query = new Query()
                        .with(Sort.by(Sort.Direction.DESC, "datetime"));
            } else {
                TextCriteria cr = TextCriteria.forLanguage("russian").matchingPhrase(search);
                query = TextQuery.queryText(cr).limit(page.getPageSize())
                        .with(Sort.by(Sort.Direction.DESC, "datetime"));
            }
            // по датам
            query.addCriteria(Criteria
                    .where("datetime")
                    .gte(DateUtils.atStartOfDay(datebeg).getTime())
                    .lte(DateUtils.atEndOfDay(dateend).getTime())
            );

            // по пользователю
            if(pattern.getProguser()!=null) {
                query.addCriteria(Criteria
                        .where("proguser.proguserId")
                        .is(pattern.getProguser().getProguserId()));
            }
            // по типас
            if(kinds!=null && !kinds.isEmpty()) {
                query.addCriteria(Criteria
                        .where("kind")
                        .in(kinds));
            }
            // по сущности
            if(pattern.getEntity()!=null) {
                query.addCriteria(Criteria
                        .where("entity")
                        .regex(".*"+pattern.getEntity()+".*","i")
                        );
            }
            // по id
            if(pattern.getIdValue()!=null) {
                query.addCriteria(Criteria
                        .where("idValue")
                        .is(pattern.getIdValue())
                );
            }
            // по duration
            if(pattern.getDuration()!=null) {
                query.addCriteria(Criteria
                        .where("duration")
                        .gte(pattern.getDuration())
                );
            }

            //запросы запускаем параллельно в своем пуле
            ForkJoinPool threadPool = new ForkJoinPool(QUERY_PARALLELISM);
            try {
                AtomicLong count = new AtomicLong();
                return new TwoTuple<>(
                        threadPool.submit(()-> {
                            // получаем количество
                            count.getAndAdd(templates.parallelStream()
                                    .flatMap(t ->
                                            t.find(query, AuditRecord.class).stream()
                                    ).count());
                            // выполняем сам запрос и фетч
                            return templates.parallelStream()
                                    .flatMap(t ->
                                            t.find(query, AuditRecord.class).stream()
                                    )
                                    .skip(page.getOffset())
                                    .limit(page.getPageSize())
                                    .collect(Collectors.toList());
                        }).get(),count.get());
            } finally {
                threadPool.shutdown();
            }
        } finally {
            mongoCient.close();
        }
    }

    public AuditRecord get(String id, Date date) {
        MongoClient mongoCient = mongoTemplateFactory.newMongoClient();
        if(mongoCient==null) {
            throw new RuntimeException("Нет доступа к серверу аудита");
        }

        try {
            LocalDate ldate = LocalDate.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
            List<String> allDataBases = getDatabases(mongoCient);

            String dbName = AuditUtils.buildDatabaseName(ldate);
            boolean found = allDataBases.stream().anyMatch(db -> db.equalsIgnoreCase(dbName));
            if(!found) {
                throw new RuntimeException(String.format("Запись с id=%s не найдена. Отсутствует база данных %s",id,dbName));
            }
            MongoTemplate template = mongoTemplateFactory.getMongoTemplateForRead(mongoCient, dbName);
            AuditRecord logRec = template.findById(new ObjectId(id), AuditRecord.class);
            if(logRec==null) {
                throw new RuntimeException(String.format("Запись с id=%s не найдена в базе данных %s",id,dbName));
            }
            return logRec;
        } finally {
            mongoCient.close();
        }
    }

    private List<String> getDatabases(MongoClient mongoCient) {
        List<String> list = new ArrayList<>();
        MongoIterable<String> databaseNames = mongoCient.listDatabaseNames();
        MongoCursor<String> dbsCursor = databaseNames.iterator();
        while(dbsCursor.hasNext()) {
            list.add(dbsCursor.next());
        }
        return list;
    }
}
