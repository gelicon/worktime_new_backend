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
import biz.gelicon.core.view.ProguserView;
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
    public static final String ALIAS_MAIN = "AR";

    @Autowired
    private ApplicationRoleRepository applicationRoleRepository;
    @Autowired
    private AccessRoleValidator accessRoleValidator;
    @Autowired
    private ApplicationRepository applicationRepository;

    // главный запрос. используется в главной таблице
    // в контроллере используется в getlist и save
    final String mainSQL = ""
            + " SELECT AR.*, "
            + "        A.application_type, "
            + "        A.application_code, "
            + "        A.application_name  "
            + " FROM   applicationrole AR, "
            + "        application A "
            + "        /*FROM_PLACEHOLDER*/ "
            + " WHERE  A.application_type = " + Application.TYPE_GELICON_CORE_APP
            + "   AND  A.application_id = AR.application_id "
            + "        /*WHERE_PLACEHOLDER*/"
            + " /*ORDERBY_PLACEHOLDER*/";

    @PostConstruct
    public void init() {
        init(applicationRoleRepository, accessRoleValidator);
    }

    public List<ApplicationRoleView> getMainList(GridDataOption gridDataOption) {
        return new Query.QueryBuilder<ApplicationRoleView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setPageableAndSort(gridDataOption.buildPageRequest())
                .putColumnSubstitution("applicationRoleAccessFlag","applicationroleId")
                .setFrom(gridDataOption.buildFullTextJoin("applicationrole",ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(ApplicationRoleView.class,ALIAS_MAIN))
                .setParams(gridDataOption.buildQueryParams())
                .build(ApplicationRoleView.class)
                .execute();
    }

    public int getMainCount(GridDataOption gridDataOption) {
        return new Query.QueryBuilder<ApplicationRoleView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setFrom(gridDataOption.buildFullTextJoin("applicationrole",ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(ApplicationRoleView.class,ALIAS_MAIN))
                .setParams(gridDataOption.buildQueryParams())
                .build(ApplicationRoleView.class)
                .count();

    }

    public ApplicationRoleView getOne(Integer id) {
        return new Query.QueryBuilder<ApplicationRoleView>(mainSQL)
                .setPredicate(ALIAS_MAIN+".applicationrole_id = :applicationroleId")
                .build(ApplicationRoleView.class)
                .executeOne("applicationroleId", id);
    }

    public void allow(Integer accessRoleId, Integer[] appIds) {
        for (Integer appId :appIds) {
            // todo
            //applicationRoleRepository.allow(accessRoleId,appId);
        }
    }

    public void deny(Integer accessRoleId, Integer[] appIds) {
        for (Integer appId :appIds) {
            // todo
            //applicationRoleRepository.deny(accessRoleId,appId);
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

