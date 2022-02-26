package biz.gelicon.core.repository;

import biz.gelicon.core.model.CapCode;
import biz.gelicon.core.model.ProguserChannel;
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
public class ProguserChannelRepository implements TableRepository<ProguserChannel> {

    private static final Logger logger = LoggerFactory.getLogger(ProguserChannelRepository.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;


    public ProguserChannel getlEmail(Integer proguserId) {
        Map<String, Object> params = new HashMap<>();
        params.put("proguser_id",proguserId);
        params.put("channel", CapCode.CHANNEL_EMAIL);
        List<ProguserChannel> list = findWhere(
                "m0.proguser_id=:proguser_id AND m0.channelnotification_id=:channel", params);
        return list.isEmpty()?null:list.get(0);
    }

    public void saveEmail(Integer proguserId, String address) {
        ProguserChannel email = getlEmail(proguserId);
        if(address!=null) {
            // сохраняем
            if(email!=null) {
                email.setProguserChannelAddress(address);
                update(email);
            } else {
                email = new ProguserChannel(null,proguserId,
                        CapCode.CHANNEL_EMAIL,address);
                insert(email);
            }
        } else {
            // удаляем
            if(email!=null) {
                delete(email);
            }
        }

    }

    @Override
    public void create() {
        Resource resource = new ClassPathResource("sql/400240-proguserchannel.sql");
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(resource);
        databasePopulator.setSqlScriptEncoding("UTF-8");
        databasePopulator.execute(jdbcTemplate.getDataSource());
        logger.info("proguserchannel created");
    }

    @Override
    public int load() {
        ProguserChannel[] data =  new ProguserChannel[] {
                new ProguserChannel(1,1,CapCode.CHANNEL_EMAIL,"test@test.com")
        };
        insert(Arrays.asList(data));
        logger.info(String.format("%d proguserchannel loaded", data.length));
        DatabaseUtils.setSequence("proguserchannel_id_gen", data.length+1);
        return data.length;
    }

}

