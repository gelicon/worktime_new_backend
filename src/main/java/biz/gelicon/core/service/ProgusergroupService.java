package biz.gelicon.core.service;

import biz.gelicon.core.model.Progusergroup;
import biz.gelicon.core.repository.ProgUserGroupRepository;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.utils.Query;
import biz.gelicon.core.validators.ProgusergroupValidator;
import biz.gelicon.core.view.ApplicationView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class ProgusergroupService extends BaseService<Progusergroup> {
    private static final Logger logger = LoggerFactory.getLogger(ProgusergroupService.class);
    public static final String ALIAS_MAIN = "UG";

    @Autowired
    private ProgUserGroupRepository progUserGroupRepository;
    @Autowired
    private ProgusergroupValidator progusergroupValidator;

    // главный запрос. используется в главной таблице
    // в контроллере используется в getlist и save
    final String mainSQL=String.format(""
                    + " SELECT %s.*"
                    + " FROM   progusergroup %1$s "
                    + " /*FROM_PLACEHOLDER*/ "
                    + " WHERE 1=1 /*WHERE_PLACEHOLDER*/ "
                    + " /*ORDERBY_PLACEHOLDER*/"
            ,ALIAS_MAIN);


    @PostConstruct
    public void init() {
        init(progUserGroupRepository, progusergroupValidator);
    }

    public List<Progusergroup> getMainList(GridDataOption gridDataOption) {
        String predicate = gridDataOption.buildPredicate(Progusergroup.class, ALIAS_MAIN);
        return new Query.QueryBuilder<Progusergroup>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setPageableAndSort(gridDataOption.buildPageRequest())
                .setFrom(gridDataOption.buildFullTextJoin("progusergroup", ALIAS_MAIN))
                .setPredicate(predicate)
                .setParams(gridDataOption.buildQueryParams())
                .build(Progusergroup.class)
                .execute();
    }

    public int getMainCount(GridDataOption gridDataOption) {
        return new Query.QueryBuilder<Progusergroup>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setParams(gridDataOption.buildQueryParams())
                .build(Progusergroup.class)
                .count();
    }


    public Progusergroup getOne(Integer id) {
        return new Query.QueryBuilder<Progusergroup>(mainSQL)
                .setPredicate(ALIAS_MAIN+".progusergroup_id = :progusergroupId")
                .build(Progusergroup.class)
                .executeOne("progusergroupId", id);
    }

    public void beforeDelete(int id) {
        if (id == 1) {
            throw new RuntimeException("Эту группу запрещено модифицировать");
        }
    }

}

