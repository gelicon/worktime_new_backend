package biz.gelicon.core.repository;


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

@Repository
public class ProgUserGroupRepository implements TableRepository<Progusergroup>  {

    private static final Logger logger = LoggerFactory.getLogger(ProgUserGroupRepository.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void create() {
        Resource resource = new ClassPathResource("sql/400200-progusergroup.sql");
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(resource);
        databasePopulator.setSqlScriptEncoding("UTF-8");
        databasePopulator.execute(jdbcTemplate.getDataSource());
        logger.info("progusergroup created");
    }

    @Override
    public int load() {
        Progusergroup[] data =  new Progusergroup[] {
                new Progusergroup(Progusergroup.EVERYONE,"Все пользователи","",1)
        };
        insert(Arrays.asList(data));
        logger.info(String.format("%d proguser loaded", data.length));
        DatabaseUtils.setSequence("progusergroup_id_gen", data.length+1);
        return data.length;
    }

}
