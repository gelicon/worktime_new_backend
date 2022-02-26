package biz.gelicon.core.audit;

import org.bson.types.ObjectId;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AuditUtils {

    public static String buildDatabaseName(LocalDate date) {
        return String.format("audit_db_%d_%02d",date.getYear(),date.getMonthValue());
    }

    // список меячных баз в обратном порядке
    public static List<String> getDataBaseNames(LocalDate datebeg, LocalDate dateend) {
        List<String> list = new ArrayList<>();
        LocalDate start = datebeg.withDayOfMonth(1);
        LocalDate fin = dateend.withDayOfMonth(dateend.lengthOfMonth());
        while(start.isBefore(fin)) {
            list.add(buildDatabaseName(fin));
            fin = fin.plusMonths(-1);
        }
        return list;
    }

    public static ObjectId toObjectId(BigInteger id, MongoTemplate mongoTemplate) {
        MappingMongoConverter converter = (MappingMongoConverter)mongoTemplate.getConverter();
        ConversionService conversionService = converter.getConversionService();
        return conversionService.convert(id, ObjectId.class);
    }


}
