package biz.gelicon.core.service;

import biz.gelicon.core.model.AccessRole;
import biz.gelicon.core.repository.AccessRoleRepository;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.utils.ProcessNamedFilter;
import biz.gelicon.core.utils.Query;
import biz.gelicon.core.validators.AccessRoleValidator;
import biz.gelicon.core.view.AccessRoleView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class AccessRoleService extends BaseService<AccessRole> {
    private static final Logger logger = LoggerFactory.getLogger(AccessRoleService.class);
    public static final String ALIAS_MAIN = "m0";

    @Autowired
    private AccessRoleRepository accessRoleRepository;
    @Autowired
    private AccessRoleValidator accessRoleValidator;

    // главный запрос. используется в главной таблице
    // в контроллере используется в getlist и save
    final String mainSQL=String.format(
            "SELECT * " +
                    "FROM accessrole %s /*FROM_PLACEHOLDER*/ " +
                    "WHERE 1=1 /*WHERE_PLACEHOLDER*/ " +  //WHERE_PLACEHOLDER если не пуст, всегда добавляет and, поэтому требуется 1=1
                    "/*ORDERBY_PLACEHOLDER*/"
            ,ALIAS_MAIN);


    @PostConstruct
    public void init() {
        init(accessRoleRepository, accessRoleValidator);
    }

    public List<AccessRoleView> getMainList(GridDataOption gridDataOption) {
        return new Query.QueryBuilder<AccessRoleView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setPageableAndSort(gridDataOption.buildPageRequest())
                .setFrom(gridDataOption.buildFullTextJoin("accessrole",ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(AccessRoleView.class,ALIAS_MAIN))
                .setParams(gridDataOption.buildQueryParams())
                .build(AccessRoleView.class)
                .execute();
    }

    public int getMainCount(GridDataOption gridDataOption) {
        return new Query.QueryBuilder<AccessRoleView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setFrom(gridDataOption.buildFullTextJoin("accessrole",ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(AccessRoleView.class,ALIAS_MAIN))
                .setParams(gridDataOption.buildQueryParams())
                .build(AccessRoleView.class)
                .count();

    }


    public AccessRoleView getOne(Integer id) {
        return new Query.QueryBuilder<AccessRoleView>(mainSQL)
                .setPredicate(ALIAS_MAIN+".accessrole_id=:accessroleId")
                .build(AccessRoleView.class)
                .executeOne("accessroleId", id);
    }
}
