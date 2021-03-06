package biz.gelicon.core.repository;

import biz.gelicon.core.model.ControlObjectRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Repository;

@Repository
public class ControlObjectRoleRepository implements TableRepository<ControlObjectRole> {

    private static final Logger logger = LoggerFactory.getLogger(ControlObjectRoleRepository.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Override
    public void create() {
        Resource resource = new ClassPathResource("sql/400234-controlobjectrole.sql");
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(resource);
        databasePopulator.setSqlScriptEncoding("UTF-8");
        databasePopulator.execute(jdbcTemplate.getDataSource());
        logger.info("controlobjectrole created");
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

