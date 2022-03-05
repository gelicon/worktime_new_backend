package biz.gelicon.core.repository;


import biz.gelicon.core.model.CapCode;
import biz.gelicon.core.model.Proguser;
import biz.gelicon.core.model.Progusergroup;
import biz.gelicon.core.utils.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProgUserRepository implements TableRepository<Proguser>  {

    private static final Logger logger = LoggerFactory.getLogger(ProgUserRepository.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Proguser> findByRole(Integer accessRoleId) {
        return findQuery(
                "SELECT pu.* " +
                        "FROM proguserrole pgr " +
                        "INNER JOIN proguser pu ON pu.proguser_id=pgr.proguser_id " +
                        "WHERE pgr.accessrole_id=:accessrole_id",
                "accessrole_id", accessRoleId);
    }


    public Proguser findByUserName(String name) {
        List<Proguser> users = findWhere("m0.proguser_name=:proguser_name", "proguser_name", name);
        return !users.isEmpty()?users.get(0):null;
    }

    public Proguser findByToken(String token) {
        List<Proguser> users = findQuery(
                "SELECT pu.* " +
                "FROM proguserauth pua inner join proguser pu on pua.proguser_id=pu.proguser_id  " +
                "WHERE pua.proguserauth_token=:proguserauth_token","proguserauth_token",token);
        return !users.isEmpty()?users.get(0):null;
    }

    public Proguser findByEmail(String email) {
        List<Proguser> users = findQuery(
                "SELECT pu.* " +
                        "FROM proguser pu  " +
                            "INNER JOIN proguserchannel puc ON puc.proguser_id = pu.proguser_id AND puc.channelnotification_id = " +CapCode.CHANNEL_EMAIL+" "+
                        "WHERE puc.proguserchannel_address=:email","email",email);
        return !users.isEmpty()?users.get(0):null;
    }

    @Override
    public void create() {
        Resource resource = new ClassPathResource("sql/400210-proguser.sql");
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(resource);
        databasePopulator.setSqlScriptEncoding("UTF-8");
        databasePopulator.execute(jdbcTemplate.getDataSource());
        logger.info("proguser created");
    }

    @Override
    public void dropForTest() {
        String[] dropTableBefore = new String[]{
        };
        TableRepository.super.drop(dropTableBefore);

        TableRepository.super.dropForTest();

        String[] dropTableAfter = new String[]{
        };
        TableRepository.super.drop(dropTableAfter);
    }

    @Override
    public int load() {
        Proguser[] data =  new Proguser[] {
                new Proguser(1, Proguser.USER_IS_ACTIVE, 1, "SYSDBA","Системный администратор"),
                new Proguser(2, Proguser.USER_IS_ACTIVE, 1, "ADMIN","Администратор"),
                new Proguser(3, Proguser.USER_IS_BLOCKED, 0, "USER","Пользователь заблокированный"),
                new Proguser(4, Proguser.USER_IS_ACTIVE, 0, "WORKER","Работник"),
        };
        insert(Arrays.asList(data));
        logger.info(String.format("%d proguser loaded", data.length));
        DatabaseUtils.setSequence("proguser_id_gen", data.length+1);
        return data.length;
    }

}
