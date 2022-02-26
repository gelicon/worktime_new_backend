package biz.gelicon.core.service;

import biz.gelicon.core.model.Application;
import biz.gelicon.core.model.Proguser;
import biz.gelicon.core.repository.ApplicationRepository;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.utils.ProcessNamedFilter;
import biz.gelicon.core.utils.Query;
import biz.gelicon.core.validators.ApplicationValidator;
import biz.gelicon.core.view.ApplicationRoleView;
import biz.gelicon.core.view.ApplicationView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationService extends BaseService<Application> {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);
    public static final String ALIAS_MAIN = "m0";

    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private ApplicationValidator applicationValidator;

    // главный запрос. используется в главной таблице
    // в контроллере используется в getlist и save
    final String mainSQLForRole=String.format(
            "SELECT %s.*, ar.applicationrole_id " +
                    "FROM application %1$s " +
                        "LEFT OUTER JOIN applicationrole ar " +
                            "ON ar.application_id=%1$s.application_id AND " +
                            "ar.accessrole_id=:accessRoleId " +
                    "/*FROM_PLACEHOLDER*/ " +
                    "WHERE %1$s.application_type=%d /*WHERE_PLACEHOLDER*/ " +
                    "/*ORDERBY_PLACEHOLDER*/"
            ,ALIAS_MAIN,Application.TYPE_GELICON_CORE_APP);


    // главный запрос для выбра всех модулей. используется в главной таблице
    // в контроллере используется в getlist и save
    final String mainSQL=String.format(
            "SELECT %s.* " +
            "FROM application %1$s " +
                "/*FROM_PLACEHOLDER*/ " +
            "WHERE %1$s.application_type=%d /*WHERE_PLACEHOLDER*/ " +
             "/*ORDERBY_PLACEHOLDER*/"
            ,ALIAS_MAIN,Application.TYPE_GELICON_CORE_APP);


    @PostConstruct
    public void init() {
        init(applicationRepository, applicationValidator);
    }

    public List<ApplicationRoleView> getMainListForRole(GridDataOption gridDataOption) {
        return new Query.QueryBuilder<ApplicationRoleView>(mainSQLForRole)
                .setMainAlias(ALIAS_MAIN)
                .setPageableAndSort(gridDataOption.buildPageRequest())
                .putColumnSubstitution("applicationRoleAccessFlag","applicationroleId")
                .setFrom(gridDataOption.buildFullTextJoin("application",ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(ApplicationView.class,ALIAS_MAIN))
                .setParams(gridDataOption.buildQueryParams())
                .build(ApplicationRoleView.class)
                .execute();
    }

    public int getMainCountForRole(GridDataOption gridDataOption) {
        return new Query.QueryBuilder<ApplicationRoleView>(mainSQLForRole)
                .setMainAlias(ALIAS_MAIN)
                .setFrom(gridDataOption.buildFullTextJoin("application",ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(ApplicationView.class,ALIAS_MAIN))
                .setParams(gridDataOption.buildQueryParams())
                .build(ApplicationRoleView.class)
                .count();

    }

    public ApplicationRoleView getOneForRole(Integer id) {
        return new Query.QueryBuilder<ApplicationRoleView>(mainSQLForRole)
                .setPredicate(ALIAS_MAIN+".application_id=:applicationId")
                .build(ApplicationRoleView.class)
                .executeOne("applicationId", id);
    }

    public List<ApplicationView> getMainList(GridDataOption gridDataOption) {
        return new Query.QueryBuilder<ApplicationView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setPageableAndSort(gridDataOption.buildPageRequest())
                .setFrom(gridDataOption.buildFullTextJoin("application",ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(ApplicationView.class,ALIAS_MAIN))
                .setParams(gridDataOption.buildQueryParams())
                .build(ApplicationView.class)
                .execute();
    }

    public int getMainCount(GridDataOption gridDataOption) {
        return new Query.QueryBuilder<ApplicationView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setFrom(gridDataOption.buildFullTextJoin("application",ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(ApplicationView.class,ALIAS_MAIN))
                .setParams(gridDataOption.buildQueryParams())
                .build(ApplicationView.class)
                .count();

    }


    public void allow(Integer accessRoleId, Integer[] appIds) {
        for (Integer appId :appIds) {
            applicationRepository.allow(accessRoleId,appId);
        }
    }

    public void deny(Integer accessRoleId, Integer[] appIds) {
        for (Integer appId :appIds) {
            applicationRepository.deny(accessRoleId,appId);
        }
    }

    public List<ApplicationView> getAccessList(Integer proguserId) {
        String sql;
        List<Application> list;
        // proguser_id=1 - для sysdba все модули доступны
        if (proguserId.intValue()== Proguser.SYSDBA_PROGUSER_ID) {
            sql = "SELECT a.* " +
                    "FROM application a " +
                    "WHERE a.application_type= " + Application.TYPE_GELICON_CORE_APP;
            list = applicationRepository.findQuery(sql);
        } else {
            sql = "SELECT DISTINCT a.* " +
                    "FROM application a " +
                    "INNER JOIN applicationrole ar ON ar.application_id=a.application_id " +
                    "INNER JOIN proguserrole pur ON pur.accessrole_id=ar.accessrole_id " +
                    "WHERE a.application_type= " + Application.TYPE_GELICON_CORE_APP+
                    " AND (pur.proguser_id=:proguser_id)";
            list = applicationRepository.findQuery(sql, "proguser_id", proguserId);
        }
        return list.stream()
                .map(ApplicationView::new)
                .collect(Collectors.toList());
    }


}

