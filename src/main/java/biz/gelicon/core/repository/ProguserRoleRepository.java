package biz.gelicon.core.repository;

import biz.gelicon.core.model.CapCode;
import biz.gelicon.core.model.ProguserChannel;
import biz.gelicon.core.model.ProguserRole;
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
public class ProguserRoleRepository implements TableRepository<ProguserRole> {

    private static final Logger logger = LoggerFactory.getLogger(ProguserRoleRepository.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Override
    public void create() {
        Resource resource = new ClassPathResource("sql/400232-proguserrole.sql");
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(resource);
        databasePopulator.setSqlScriptEncoding("UTF-8");
        databasePopulator.execute(jdbcTemplate.getDataSource());
        logger.info("proguserrole created");
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

}

