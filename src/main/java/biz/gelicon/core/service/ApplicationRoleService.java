package biz.gelicon.core.service;

import biz.gelicon.core.model.Application;
import biz.gelicon.core.model.ApplicationRole;
import biz.gelicon.core.model.Proguser;
import biz.gelicon.core.repository.ApplicationRepository;
import biz.gelicon.core.repository.ApplicationRoleRepository;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.utils.Query;
import biz.gelicon.core.validators.AccessRoleValidator;
import biz.gelicon.core.view.ApplicationRoleView;
import biz.gelicon.core.view.ApplicationView;
import biz.gelicon.core.view.ApplicationWithAccessRoleView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationRoleService extends BaseService<ApplicationRole> {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationRoleService.class);
    public static final String ALIAS_MAIN = "TT";

    @Autowired
    private ApplicationRoleRepository applicationRoleRepository;
    @Autowired
    private AccessRoleValidator accessRoleValidator;
    @Autowired
    private ApplicationRepository applicationRepository;

    // главный запрос. используется в главной таблице
    // в контроллере используется в getlist и save
    final String mainSQL = ""
            + " SELECT TT.* "
            + " FROM   ("
            + " SELECT AR.*, "
            + "        A.application_type, "
            + "        A.application_code, "
            + "        A.application_name "
            + " FROM   applicationrole AR, "
            + "        application A "
            + "        /*FROM_PLACEHOLDER*/ "
            + " WHERE  A.application_type = " + Application.TYPE_GELICON_CORE_APP
            + "   AND  A.application_id = AR.application_id "
            + " ) TT "
            + " WHERE  1=1 /*WHERE_PLACEHOLDER*/"
            + " /*ORDERBY_PLACEHOLDER*/";

    final String mainSQLForRole = ""
            + " SELECT TT.* "
            + " FROM   ("
            + " SELECT A.*, "
            + "        AR.applicationrole_id,"
            + "        AR.accessrole_id "
            + " FROM   application A "
            + "        LEFT OUTER JOIN applicationrole AR "
            + "          ON AR.application_id = A.application_id "
            + "         AND AR.accessrole_id = :accessRoleId "
            + " ) TT "
            + " /*FROM_PLACEHOLDER*/ "
            + " WHERE  TT.application_type = " + Application.TYPE_GELICON_CORE_APP
            + " /*WHERE_PLACEHOLDER*/ "
            + " /*ORDERBY_PLACEHOLDER*/";

    @PostConstruct
    public void init() {
        init(applicationRoleRepository, accessRoleValidator);
    }

    public List<ApplicationRoleView> getMainList(GridDataOption gridDataOption) {
        return new Query.QueryBuilder<ApplicationRoleView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setPageableAndSort(gridDataOption.buildPageRequest())
                .putColumnSubstitution("applicationRoleAccessFlag", "applicationroleId")
                .setFrom(gridDataOption.buildFullTextJoin("applicationrole", ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(ApplicationRoleView.class, ALIAS_MAIN))
                .setParams(gridDataOption.buildQueryParams())
                .build(ApplicationRoleView.class)
                .execute();
    }

    public List<ApplicationWithAccessRoleView> getMainListForRole(GridDataOption gridDataOption) {
        String predicate = gridDataOption.buildPredicate(ApplicationView.class, ALIAS_MAIN);
        // говнокод
        int index = predicate.lastIndexOf("TT.accessrole_id");
        if (index == 0) {
            predicate = "";
        } else {
            index = predicate.lastIndexOf("and TT.accessrole_id");
            if (index > 0) {
                predicate = predicate.substring(0, index - 1);
            }
        }
        return new Query.QueryBuilder<ApplicationWithAccessRoleView>(mainSQLForRole)
                .setMainAlias(ALIAS_MAIN)
                .setPageableAndSort(gridDataOption.buildPageRequest())
                .putColumnSubstitution("applicationRoleAccessFlag", "applicationroleId")
                .setFrom(gridDataOption.buildFullTextJoin("application", ALIAS_MAIN))
                .setPredicate(predicate)
                .setParams(gridDataOption.buildQueryParams())
                .build(ApplicationWithAccessRoleView.class)
                .execute();
    }

    public int getMainCountForRole(GridDataOption gridDataOption) {
        return new Query.QueryBuilder<ApplicationWithAccessRoleView>(mainSQLForRole)
                .setMainAlias(ALIAS_MAIN)
                .setFrom(gridDataOption.buildFullTextJoin("application", ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(ApplicationView.class, ALIAS_MAIN))
                .setParams(gridDataOption.buildQueryParams())
                .build(ApplicationWithAccessRoleView.class)
                .count();

    }

    public int getMainCount(GridDataOption gridDataOption) {
        return new Query.QueryBuilder<ApplicationRoleView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setFrom(gridDataOption.buildFullTextJoin("application", ALIAS_MAIN))
                //.setPredicate(gridDataOption.buildPredicate(ApplicationRoleView.class, ALIAS_MAIN))
                .setParams(gridDataOption.buildQueryParams())
                .build(ApplicationRoleView.class)
                .count();

    }

    public ApplicationRoleView getOne(Integer id) {
        return new Query.QueryBuilder<ApplicationRoleView>(mainSQL)
                .setPredicate(ALIAS_MAIN + ".applicationrole_id = :applicationroleId")
                .build(ApplicationRoleView.class)
                .executeOne("applicationroleId", id);
    }

    /**
     * Связывает роль с приложением
     * @param accessRoleId - роль
     * @param appIds - приложения
     */
    public void allow(Integer accessRoleId, Integer[] appIds) {
        for (Integer appId : appIds) {
            applicationRoleRepository.allow(accessRoleId,appId);
        }
    }

    /**
     * Отвязывает роль от приложения
     * @param accessRoleId - роль
     * @param appIds - приложения
     */
    public void deny(Integer accessRoleId, Integer[] appIds) {
        for (Integer appId : appIds) {
            applicationRoleRepository.deny(accessRoleId,appId);
        }
    }

    public List<ApplicationView> getAccessList(Integer proguserId) {
        String sql;
        List<Application> list;
        // proguser_id=1 - для sysdba все модули доступны
        if (proguserId == Proguser.SYSDBA_PROGUSER_ID) {
            sql = "SELECT a.* " +
                    "FROM application a " +
                    "WHERE a.application_type= " + Application.TYPE_GELICON_CORE_APP;
            list = applicationRepository.findQuery(sql);
        } else {
            sql = "SELECT DISTINCT a.* " +
                    "FROM application a " +
                    "INNER JOIN applicationrole ar ON ar.application_id=a.application_id " +
                    "INNER JOIN proguserrole pur ON pur.accessrole_id=ar.accessrole_id " +
                    "WHERE a.application_type= " + Application.TYPE_GELICON_CORE_APP +
                    " AND (pur.proguser_id=:proguser_id)";
            list = applicationRepository.findQuery(sql, "proguser_id", proguserId);
        }
        return list.stream()
                .map(ApplicationView::new)
                .collect(Collectors.toList());
    }

}

