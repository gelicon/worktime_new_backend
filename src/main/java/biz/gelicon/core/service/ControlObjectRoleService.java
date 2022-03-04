package biz.gelicon.core.service;

import biz.gelicon.core.model.ControlObjectRole;
import biz.gelicon.core.repository.ControlObjectRoleRepository;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.utils.Query;
import biz.gelicon.core.validators.ControlObjectRoleValidator;
import biz.gelicon.core.view.ControlObjectRoleView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class ControlObjectRoleService extends BaseService<ControlObjectRole> {

    private static final Logger logger = LoggerFactory.getLogger(ControlObjectRoleService.class);
    public static final String ALIAS_MAIN = "COR";
    public static final String MAINTABLE_NAME = "controlobjectrole";

    @Autowired
    private ControlObjectRoleRepository controlObjectRoleRepository;
    @Autowired
    private ControlObjectRoleValidator controlObjectRoleValidator;

    // главный запрос. используется в главной таблице
    // в контроллере используется в getlist и save
    final String mainSQL = ""
            + " SELECT COR.*, "
            + "        CO.controlobject_name, "
            + "        CO.controlobject_url, "
            + "        AR.accessrole_name, "
            + "        AR.accessrole_note, "
            + "        A.sqlaction_note "
            + " FROM   controlobjectrole COR, "
            + "        controlobject CO, "
            + "        accessrole AR, "
            + "        sqlaction A "
            + "/*FROM_PLACEHOLDER*/ "
            + " WHERE  CO.controlobject_id = COR.controlobject_id "
            + "   AND  AR.accessrole_id = COR.accessrole_id  "
            + "   AND  A.sqlaction_id  = COR.sqlaction_id "
            + "/*WHERE_PLACEHOLDER*/ "
            + " /*ORDERBY_PLACEHOLDER*/";

    @PostConstruct
    public void init() {
        init(controlObjectRoleRepository, controlObjectRoleValidator);
    }

    public List<ControlObjectRoleView> getMainList(GridDataOption gridDataOption) {
        return new Query.QueryBuilder<ControlObjectRoleView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setPageableAndSort(gridDataOption.buildPageRequest())
                .setFrom(gridDataOption.buildFullTextJoin(MAINTABLE_NAME, ALIAS_MAIN))
                .setPredicate(
                        gridDataOption.buildPredicate(ControlObjectRoleView.class, ALIAS_MAIN))
                .setParams(gridDataOption.buildQueryParams())
                .build(ControlObjectRoleView.class)
                .execute();
    }

    public int getMainCount(GridDataOption gridDataOption) {
        return new Query.QueryBuilder<ControlObjectRoleView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setFrom(gridDataOption.buildFullTextJoin(MAINTABLE_NAME, ALIAS_MAIN))
                .setPredicate(
                        gridDataOption.buildPredicate(ControlObjectRoleView.class, ALIAS_MAIN))
                .setParams(gridDataOption.buildQueryParams())
                .build(ControlObjectRoleView.class)
                .count();

    }


    public ControlObjectRoleView getOne(Integer id) {
        return new Query.QueryBuilder<ControlObjectRoleView>(mainSQL)
                .setPredicate(ALIAS_MAIN + "." + MAINTABLE_NAME + "_id = :" + MAINTABLE_NAME + "Id")
                .build(ControlObjectRoleView.class)
                .executeOne(MAINTABLE_NAME + "Id", id);
    }
}
