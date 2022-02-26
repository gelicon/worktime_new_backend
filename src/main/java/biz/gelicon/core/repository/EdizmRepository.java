package biz.gelicon.core.repository;

import biz.gelicon.core.model.Edizm;
import biz.gelicon.core.utils.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class EdizmRepository implements TableRepository<Edizm> {

    private static final Logger logger = LoggerFactory.getLogger(EdizmRepository.class);
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void create() {
        Resource resource = new ClassPathResource("sql/500900-edizm.sql");
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(resource);
        databasePopulator.setSqlScriptEncoding("UTF-8");
        databasePopulator.execute(jdbcTemplate.getJdbcTemplate().getDataSource());
        logger.info("capcode created");
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
        Edizm[] data =  new Edizm[] {
                new Edizm(1,"Штука","шт",0,"796"),
                new Edizm(2,"Килограмм","кг",0,"166"),
                new Edizm(3,"Тонна","тн",1,"168"),
                new Edizm(4,"Грамм","г",0,"163"),
                new Edizm(5,"Метр","м",0,"006")
        };
        insert(Arrays.asList(data));
        logger.info(String.format("%d edizm loaded", data.length));
        DatabaseUtils.setSequence("edizm_id_gen", data.length+1);
        return data.length;

    }

}

