package biz.gelicon.core.repository;

import biz.gelicon.core.model.ProguserAuth;
import biz.gelicon.core.utils.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Repository
public class ProgUserAuthRepository implements TableRepository<ProguserAuth>   {
    private static final int TOKEN_DEFAULT_EXPIRY_DURATION = 30;

    private static final Logger logger = LoggerFactory.getLogger(ProgUserAuthRepository.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void updateAccessToken(ProguserAuth auth, Date lastQuery) {
        Map<String, Object> params = new HashMap<>();
        params.put("proguserauthId",auth.getProguserAuthId());
        params.put("proguserauthLastQuery",lastQuery);
        executeSQL("" +
                "UPDATE proguserauth " +
                "SET proguserauth_lastquery=:proguserauthLastQuery " +
                "WHERE proguserauth_id=:proguserauthId",params);
    }

    public ProguserAuth findActiveLastByDate(Integer progUserId) {
        Map<String,Object> args = new HashMap<>();
        args.put("progUserId",progUserId);
        args.put("now",new Date());
        List<ProguserAuth> tokens = findWhere("m0.proguser_id=:progUserId and " +
                        "(m0.proguserauth_dateend is null or m0.proguserauth_dateend>:now) and " +
                        "m0.proguserauth_datecreate=(" +
                        "SELECT max(m1.proguserauth_datecreate) " +
                        "FROM proguserauth m1 " +
                        "WHERE m1.proguser_id=:progUserId and " +
                        "(m1.proguserauth_dateend is null or m1.proguserauth_dateend>:now)" +
                        ")",
                args);
        return !tokens.isEmpty()?tokens.get(0):null;
    }

    /**
     * Создаем новый токен. Срок действия в днях
     * @param progUserId
     * @param duration срок действия
     * @return
     */
    public ProguserAuth reNew(Integer progUserId, int duration) {
        String tok = UUID.randomUUID().toString();
        Date now = new Date();
        Date expiryDate =  new Date(now.getTime() + duration*24*60*60*1000l);
        ProguserAuth pua = new ProguserAuth(null, progUserId, new Date(), expiryDate, tok);
        insert(pua);
        return pua;
    }

    /**
     * Токен на TOKEN_DEFAULT_EXPIRY_DURATION дней
     * @param progUserId
     * @return
     */
    public ProguserAuth reNew(Integer progUserId) {
        return reNew(progUserId,TOKEN_DEFAULT_EXPIRY_DURATION);
    }

    public ProguserAuth findByValue(String token) {
        List<ProguserAuth> list = findWhere("m0.proguserauth_token=:proguserauth_token", "proguserauth_token", token);
        return !list.isEmpty()?list.get(0):null;
    }

    @Override
    public void create() {
        Resource resource = new ClassPathResource("sql/400220-proguserauth.sql");
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(resource);
        databasePopulator.setSqlScriptEncoding("UTF-8");
        databasePopulator.execute(jdbcTemplate.getDataSource());
        logger.info("proguser auth created");
    }


    @Override
    public int load() {

        Date oldDate = null;
        try {
            oldDate = new SimpleDateFormat("dd.MM.yyyy").parse("01.01.2000");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ProguserAuth[] data =  new ProguserAuth[] {
                new ProguserAuth(1,1,new Date(),"e9b3c034-fdd5-456f-825b-4c632f2053ac"),
                new ProguserAuth(2,2,new Date(), oldDate,"62e4e435-85da-48a4-adb0-d91ff1d26624"),// устаревший
      //          new ProguserAuth(3,4,new Date(), oldDate,"12121212-8888-48a4-adb2-d91ff1d27899"),// устаревший, но юзер живой
                new ProguserAuth(4,3,new Date(), "11111111-1111-1111-1111-111111111111"),// у заблокированного
                new ProguserAuth(5,2,new Date(), "22222222-85da-48a4-2222-d91ff1d26624"),// живой токен для test1
        };
        insert(Arrays.asList(data));
        logger.info(String.format("%d proguser auth loaded", data.length));
        DatabaseUtils.setSequence("proguserauth_id_gen", data.length+1);
        return data.length;
    }


}
