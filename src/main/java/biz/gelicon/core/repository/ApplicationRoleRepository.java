package biz.gelicon.core.repository;

import biz.gelicon.core.model.ApplicationRole;
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
public class ApplicationRoleRepository implements TableRepository<ApplicationRole> {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationRoleRepository.class);
    public final static int INITIAL_LOAD_COUNT = 6; // Для теста - сколько грузим в начале столько и ставим
    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Override
    public void create() {
        Resource resource = new ClassPathResource("sql/400310-applicationrole.sql");
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(resource);
        databasePopulator.setSqlScriptEncoding("UTF-8");
        databasePopulator.execute(jdbcTemplate.getDataSource());
        logger.info(this.getTableMetaData().getTableName() + " created");
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
        ApplicationRole[] data = new ApplicationRole[]{
                new ApplicationRole(null, 2, 1), // ADMIN - Группы пользователей
                new ApplicationRole(null, 2, 2), // ADMIN - Приложения
                new ApplicationRole(null, 2, 3), // ADMIN - Роли доступа
                new ApplicationRole(null, 2, 4), // ADMIN - Пользователи
                new ApplicationRole(null, 2, 5), // ADMIN - Доступ роли на приложение
                new ApplicationRole(null, 3, 100), // EDIZM - Справочник единиц измерения
        };
        insert(Arrays.asList(data));
        String tableName = this.getTableMetaData().getTableName();
        DatabaseUtils.setSequence(tableName +"_id_gen", data.length + 1);
        logger.info(data.length + " " + tableName + " loaded");
        return data.length;
    }

}

