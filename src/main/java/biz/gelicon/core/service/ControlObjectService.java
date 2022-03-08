package biz.gelicon.core.service;

import biz.gelicon.core.model.ControlObject;
import biz.gelicon.core.repository.AccessRoleRepository;
import biz.gelicon.core.repository.ControlObjectRepository;
import biz.gelicon.core.security.Permission;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.utils.ProcessNamedFilter;
import biz.gelicon.core.utils.Query;
import biz.gelicon.core.validators.ControlObjectValidator;
import biz.gelicon.core.view.ControlObjectView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Service
public class ControlObjectService extends BaseService<ControlObject> {
    private static final Logger logger = LoggerFactory.getLogger(ControlObjectService.class);
    public static final String ALIAS_MAIN = "CO";

    @Autowired
    private ControlObjectRepository controlObjectRepository;
    @Autowired
    private ControlObjectValidator controlObjectValidator;

    // главный запрос. используется в главной таблице
    // в контроллере используется в getlist и save
    final String mainSQL=String.format(""
                    + " SELECT %s.*,"
                    + "        COR.controlobjectrole_id "
                    + " FROM   controlobject %1$s "
                    + "        LEFT OUTER JOIN controlobjectrole COR "
                    + "          ON COR.controlobject_id = %1$s.controlobject_id "
                    + "         AND COR.sqlaction_id = :sqlactionId "
                    + "         AND COR.accessrole_id = :accessRoleId "
                    + " /*FROM_PLACEHOLDER*/ "
                    + " WHERE 1 = 1 /*WHERE_PLACEHOLDER*/ "
                    + " /*ORDERBY_PLACEHOLDER*/"
            ,ALIAS_MAIN);

    @PostConstruct
    public void init() {
        init(controlObjectRepository, controlObjectValidator);
    }

    public List<ControlObjectView> getMainList(GridDataOption gridDataOption) {
        Map<String, Object> params = gridDataOption.buildQueryParams();
        params.put("sqlactionId", Permission.EXECUTE.ordinal());
        return new Query.QueryBuilder<ControlObjectView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setPageableAndSort(gridDataOption.buildPageRequest())
                .putColumnSubstitution("controlObjectRoleAccessFlag","controlobjectroleId")
                .setFrom(gridDataOption.buildFullTextJoin("controlobject",ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(ControlObjectView.class,ALIAS_MAIN))
                .setParams(params)
                .build(ControlObjectView.class)
                .execute();
    }

    /**
     * Спикок всех контролируемых объектов
     */
    public List<ControlObject> getAllList() {
        String sql = ""
                + " SELECT * "
                + " FROM   controlobject "
                + " ORDER BY 1";
        return new Query.QueryBuilder<ControlObject>(sql)
                .build(ControlObject.class)
                .execute();
    }

    public int getMainCount(GridDataOption gridDataOption) {
        Map<String, Object> params = gridDataOption.buildQueryParams();
        params.put("sqlactionId", Permission.EXECUTE.ordinal());
        return new Query.QueryBuilder<ControlObjectView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setFrom(gridDataOption.buildFullTextJoin("controlobject",ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(ControlObjectView.class,ALIAS_MAIN))
                .setParams(params)
                .build(ControlObjectView.class)
                .count();

    }

    public ControlObjectView getOne(Integer id) {
        return new Query.QueryBuilder<ControlObjectView>(
                String.format(""
                        + "SELECT * "
                        + " FROM controlobject %s "
                        + " WHERE 1 = 1 /*WHERE_PLACEHOLDER*/",
                        ALIAS_MAIN))
                .setPredicate(ALIAS_MAIN+".controlobject_id = :controlobjectId")
                .build(ControlObjectView.class)
                .executeOne("controlobjectId", id);
    }

    public void allow(Integer accessroleId, Integer[] controlObjectIds) {
        // Для SYSDBA нечего добавлять - он и так все умеет
        if (accessroleId == AccessRoleRepository.SYSDBA_ACCESSROLE_ID) return;
        for (Integer coId :controlObjectIds) {
            controlObjectRepository.allow(accessroleId,coId,Permission.EXECUTE);
        }
    }

    public void deny(Integer accessroleId, Integer[] controlObjectIds) {
        for (Integer coId :controlObjectIds) {
            controlObjectRepository.deny(accessroleId,coId,Permission.EXECUTE);
        }
    }
}

