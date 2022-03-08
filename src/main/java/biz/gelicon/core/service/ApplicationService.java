package biz.gelicon.core.service;

import biz.gelicon.core.model.Application;
import biz.gelicon.core.model.ApplicationRole;
import biz.gelicon.core.model.ControlObject;
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


    /**
     * Спикок всех приложений
     */
    public List<ApplicationRole> getAllList() {
        String sql = ""
                + " SELECT * "
                + " FROM   application "
                + " ORDER BY 1";
        return new Query.QueryBuilder<ApplicationRole>(sql)
                .build(ApplicationRole.class)
                .execute();
    }
}

