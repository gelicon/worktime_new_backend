package biz.gelicon.core.service;

import biz.gelicon.core.model.Proguser;
import biz.gelicon.core.model.ProguserAuth;
import biz.gelicon.core.repository.ProgUserAuthRepository;
import biz.gelicon.core.repository.ProgUserRepository;
import biz.gelicon.core.security.AuthenticationBean;
import biz.gelicon.core.security.AuthenticationCashe;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.utils.Query;
import biz.gelicon.core.validators.ProgUserAuthValidator;
import biz.gelicon.core.view.ProguserAuthView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;

@Service
public class SessionService  extends BaseService<ProguserAuth> {
    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);
    public static final String ALIAS_MAIN = "M0";

    @Autowired
    private ProgUserAuthRepository progUserAuthRepository;
    @Autowired
    private ProgUserRepository progUserRepository;

    @Autowired
    private ProgUserAuthValidator progUserAuthValidator;
    @Autowired
    private AuthenticationBean authenticationBean;
    @Autowired
    AuthenticationCashe authenticationCashe;

    // главный запрос. используется в главной таблице
    // в контроллере используется в getlist и save
    final String mainSQL= ""
            + " SELECT m0.*,"
            + "        pu.proguser_name, "
            + "        pu.proguser_status_id, "
            + "        CASE "
            + "          WHEN pu.proguser_status_id = 0 THEN 'Неактивный' "
            + "          ELSE 'Активный' "
            + "        END proguser_status_display "
            + " FROM   proguserauth m0 "
            + "        INNER JOIN proguser pu ON PU.proguser_id = m0.proguser_id "
            + " /*FROM_PLACEHOLDER*/ "
            + " WHERE 1 = 1 "
            + "    AND (   :status = 0 "
                        // действительный токен
            + "         OR (:status = 1 AND (m0.proguserauth_dateend is null OR m0.proguserauth_dateend > CURRENT_TIMESTAMP)) OR "
                        // токен закрыт
            + "            (:status = 2 AND m0.proguserauth_dateend is not null AND m0.proguserauth_dateend <= CURRENT_TIMESTAMP) "
            + "        ) AND "
            + "        (:proguserId = 0 OR m0.proguser_id = :proguserId"
            + "        )" +
            "/*WHERE_PLACEHOLDER*/ " +  //WHERE_PLACEHOLDER если не пуст, всегда добавляет and, поэтому требуется 1=1
            "/*ORDERBY_PLACEHOLDER*/";

    @PostConstruct
    public void init() {
        init(progUserAuthRepository, progUserAuthValidator);
    }

    public List<ProguserAuthView> getMainList(GridDataOption gridDataOption) {
        Map<String, String> subsColumns = new HashMap<>();
        subsColumns.put("proguser_name", "pu.proguser_name");
        subsColumns.put("proguser_status_display", "cc.capcode_name");

        Map<String, Function<GridDataOption.QuickFilter, String>> processQuickFilter = new HashMap<>();
        processQuickFilter.put("proguserAuthDateEnd",qf->String.format("cast(%s.proguserauth_dateend as date) = cast(:proguserAuthDateEnd as date)",ALIAS_MAIN));
        gridDataOption.setProcessQuickFilter(processQuickFilter);

        return new Query.QueryBuilder<ProguserAuthView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setPageableAndSort(gridDataOption.buildPageRequest())
                .setFrom(gridDataOption.buildFullTextJoin("proguserauth",ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(ProguserAuthView.class,ALIAS_MAIN,subsColumns))
                .setParams(gridDataOption.buildQueryParams())
                .setParamTransformer("proguserAuthDateEnd",(Function<Long, Date>) arg -> new Date(arg))
                .setParamTransformer("proguserAuthDateCreate",(Function<Long, Date>) arg -> new Date(arg))
                .setParamTransformer("proguserAuthLastQuery",(Function<Long, Date>) arg -> new Date(arg))
                .setParamTransformer("status",(Function<String,Integer>) arg -> Integer.valueOf(arg))
                .build(ProguserAuthView.class)
                .execute();
    }

    public int getMainCount(GridDataOption gridDataOption) {
        Map<String, String> subsColumns = new HashMap<>();
        subsColumns.put("proguser_name", "pu.proguser_name");
        subsColumns.put("proguser_status_display", "cc.capcode_name");

        Map<String, Function<GridDataOption.QuickFilter, String>> processQuickFilter = new HashMap<>();
        processQuickFilter.put("proguserAuthDateEnd",qf->String.format("cast(%s.proguserauth_dateend as date) = cast(:proguserAuthDateEnd as date)",ALIAS_MAIN));
        gridDataOption.setProcessQuickFilter(processQuickFilter);

        return new Query.QueryBuilder<ProguserAuthView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setFrom(gridDataOption.buildFullTextJoin("proguserauth",ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(ProguserAuthView.class,ALIAS_MAIN,subsColumns))
                .setParams(gridDataOption.buildQueryParams())
                .setParamTransformer("proguserAuthDateEnd",(Function<Long, Date>) arg -> new Date(arg))
                .setParamTransformer("proguserAuthDateCreate",(Function<Long, Date>) arg -> new Date(arg))
                .setParamTransformer("proguserAuthLastQuery",(Function<Long, Date>) arg -> new Date(arg))
                .setParamTransformer("status",(Function<String,Integer>) arg -> Integer.valueOf(arg))
                .build(ProguserAuthView.class)
                .count();

    }

    public ProguserAuthView getOne(Integer id) {
        return new Query.QueryBuilder<ProguserAuthView>(mainSQL)
                .setPredicate("m0.proguserauth_id = :proguserauthId")
                .build(ProguserAuthView.class)
                .executeOne("proguserauthId", id);
    }

    public void closeSessions(int[] ids, final Date closeDate, String currentToken) {
        List<Integer> processIds = new ArrayList<>();

        progUserAuthRepository.findById(
            Arrays.stream(ids)
                    .boxed()
                    .toArray(Integer[]::new))
                .forEach(pa->{
                    if (pa.getProguserAuthDateEnd()==null) {
                        if(currentToken!=null && currentToken.equals(pa.getProguserAuthToken())) {
                            throw new RuntimeException("Текущую сессию закрыть нельзя");
                        }
                        pa.setProguserAuthDateEnd(closeDate);
                        progUserAuthRepository.update(pa);

                        // сбросим кэш токенов для пользователя
                        if(!processIds.contains(pa.getProguserId())) {
                            Proguser pu = progUserRepository.findById(pa.getProguserId());
                            authenticationCashe.clearByUserName(pu.getProguserName());
                            processIds.add(pa.getProguserId());
                        }

                    }
                });
    }
}
