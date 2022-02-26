package biz.gelicon.core.repository;

import biz.gelicon.core.model.CapCode;
import biz.gelicon.core.model.ProguserCredential;
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
public class ProguserCredentialRepository implements TableRepository<ProguserCredential> {

    private static final Logger logger = LoggerFactory.getLogger(ProguserCredentialRepository.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ProguserCredential findByProguser(Integer proguserId, Integer authType, Integer tempFlag) {
        Map<String, Object> params = new HashMap<>();
        params.put("proguser_id",proguserId);
        params.put("type",authType);
        params.put("tempflag",tempFlag);
        List<ProguserCredential> list = findWhere("m0.proguser_id=:proguser_id and m0.progusercredential_type=:type " +
                "and m0.progusercredential_tempflag=:tempflag", params);
        return !list.isEmpty()?list.get(0):null;
    }

    @Override
    public void create() {
        Resource resource = new ClassPathResource("sql/400250-progusercredential.sql");
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(resource);
        databasePopulator.setSqlScriptEncoding("UTF-8");
        databasePopulator.execute(jdbcTemplate.getDataSource());
        logger.info("progusercredential created");
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
        // это хеш от pswd
        final String pswd = "e572cec4ee0d79651b3f8b15339047e1e51600036cce3f65dfecc556e6cd1279edaba62938dba7f53475706679790797";
        // это хеш от masterkey
        final String masterkey = "e572cec4ee0d79651b3f8b15339047e1c70f4ea0d43c3ca3287ff7ba623ca15fb4d0fa73ef2d04a51cf84f644aa1ee62";
        ProguserCredential[] data =  new ProguserCredential[] {
                new ProguserCredential(1,masterkey,1, CapCode.AUTH_BYPASSWORD,0,0),
                new ProguserCredential(2,masterkey,2, CapCode.AUTH_BYPASSWORD,0,0),
                new ProguserCredential(3,masterkey,3, CapCode.AUTH_BYPASSWORD,0,0),
                new ProguserCredential(4,masterkey,4, CapCode.AUTH_BYPASSWORD,0,0),
        };
        insert(Arrays.asList(data));
        logger.info(String.format("%d progusercredential loaded", data.length));
        DatabaseUtils.setSequence("progusercredential_id_gen", data.length+1);
        return data.length;
    }

}

