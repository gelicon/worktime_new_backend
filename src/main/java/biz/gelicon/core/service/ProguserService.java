package biz.gelicon.core.service;

import biz.gelicon.core.model.Proguser;
import biz.gelicon.core.repository.AccessRoleRepository;
import biz.gelicon.core.repository.ProgUserRepository;
import biz.gelicon.core.repository.ProguserChannelRepository;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.utils.Query;
import biz.gelicon.core.validators.ProguserValidator;
import biz.gelicon.core.view.ProguserView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class ProguserService extends BaseService<Proguser> {
    private static final Logger logger = LoggerFactory.getLogger(ProguserService.class);
    public static final String ALIAS_MAIN = "T";
    public static final String ALIAS_ENTITY = "U";

    @Autowired
    private ProgUserRepository proguserRepository;
    @Autowired
    private ProguserValidator proguserValidator;
    @Autowired
    private ProguserChannelRepository proguserChannelRepository;
    @Autowired
    private AccessRoleRepository accessRoleRepository;

    // главный запрос. используется в главной таблице
    // в контроллере используется в getlist и save
    final String mainSQL=""
                    + " SELECT T.* "
                    + " FROM ("
                    + " SELECT U.* "
                    + " FROM   proguser U "
                    + " /*FROM_PLACEHOLDER*/ "
                    + " WHERE 1=1 /*WHERE_PLACEHOLDER*/ "
                    + " ) T "
                    + " /*ORDERBY_PLACEHOLDER*/";


    @PostConstruct
    public void init() {
        init(proguserRepository, proguserValidator);
    }

    public List<ProguserView> getMainList(GridDataOption gridDataOption) {

        return new Query.QueryBuilder<ProguserView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setPageableAndSort(gridDataOption.buildPageRequest())
                .setFrom(gridDataOption.buildFullTextJoin("proguser",ALIAS_ENTITY))
                .setParams(gridDataOption.buildQueryParams())
                .build(ProguserView.class)
                .execute();
    }

    public int getMainCount(GridDataOption gridDataOption) {

        return new Query.QueryBuilder<Proguser>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setFrom(gridDataOption.buildFullTextJoin("proguser",ALIAS_ENTITY))
                .setParams(gridDataOption.buildQueryParams())
                .build(Proguser.class)
                .count();

    }


    public ProguserView getOne(Integer id) {
        return new Query.QueryBuilder<ProguserView>(mainSQL)
                .setPredicate(ALIAS_ENTITY+".proguser_id = :proguserId")
                .build(ProguserView.class)
                .executeOne("proguserId", id);
    }

    public List<ProguserView> getListForFind(GridDataOption gridDataOption) {
        return new Query.QueryBuilder<ProguserView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setPageableAndSort(gridDataOption.buildPageRequest())
                .setFrom(gridDataOption.buildFullTextJoin("proguser",ALIAS_ENTITY))
                .setPredicate(gridDataOption.buildPredicate(Proguser.class,ALIAS_ENTITY,null))
                .setParams(gridDataOption.buildQueryParams())
                .build(ProguserView.class)
                .execute();

    }
}

