package biz.gelicon.core.service;

import biz.gelicon.core.model.AccessRole;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProguserService extends BaseService<Proguser> {

    private static final Logger logger = LoggerFactory.getLogger(ProguserService.class);
    public static final String ALIAS_MAIN = "U";

    @Autowired
    private ProgUserRepository proguserRepository;
    @Autowired
    private ProguserValidator proguserValidator;
    @Autowired
    private ProguserChannelRepository proguserChannelRepository;
    @Autowired
    private AccessRoleRepository accessRoleRepository;
    @Autowired
    private ProguserService proguserService;

    // главный запрос. используется в главной таблице
    // в контроллере используется в getlist и save
    final String mainSQL = ""
            + " SELECT U.* "
            + " FROM   proguser U "
            + " /*FROM_PLACEHOLDER*/ "
            + " WHERE 1=1 /*WHERE_PLACEHOLDER*/ "
            + " /*ORDERBY_PLACEHOLDER*/";


    @PostConstruct
    public void init() {
        init(proguserRepository, proguserValidator);
    }

    public List<ProguserView> getMainList(GridDataOption gridDataOption) {

        //gridDataOption.addFilter("statusId",1);

        return new Query.QueryBuilder<ProguserView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setPageableAndSort(gridDataOption.buildPageRequest())
                .setFrom(gridDataOption.buildFullTextJoin("proguser", ALIAS_MAIN))
                // .setPredicate(gridDataOption.buildPredicate(ProguserView.class,ALIAS_MAIN,subsColumns))
                .setPredicate(gridDataOption.buildPredicate(ProguserView.class, ALIAS_MAIN))
                .setParams(gridDataOption.buildQueryParams())
                .build(ProguserView.class)
                .execute();
    }

    public int getMainCount(GridDataOption gridDataOption) {

        return new Query.QueryBuilder<Proguser>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setFrom(gridDataOption.buildFullTextJoin("proguser", ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(ProguserView.class, ALIAS_MAIN))
                .setParams(gridDataOption.buildQueryParams())
                .build(Proguser.class)
                .count();

    }


    public ProguserView getOne(Integer id) {
        return new Query.QueryBuilder<ProguserView>(mainSQL)
                .setPredicate(ALIAS_MAIN + ".proguser_id = :proguserId")
                .build(ProguserView.class)
                .executeOne("proguserId", id);
    }

    public List<ProguserView> getListForFind(GridDataOption gridDataOption) {
        return new Query.QueryBuilder<ProguserView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setPageableAndSort(gridDataOption.buildPageRequest())
                .setFrom(gridDataOption.buildFullTextJoin("proguser", ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(Proguser.class, ALIAS_MAIN, null))
                .setParams(gridDataOption.buildQueryParams())
                .build(ProguserView.class)
                .execute();

    }

    public List<AccessRole> getRoleList(Integer proguserId) {
        List<AccessRole> list = accessRoleRepository.findByUser(proguserId);
        return list.stream()
                .sorted((r1, r2) -> r1.getAccessRoleName().compareTo(r2.getAccessRoleName()))
                .collect(Collectors.toList());
    }

    //
    public void saveRoles(Integer proguserId, List<Integer> accessRoleIds) {
        // todo - сделаеть по уму, без удаления
        if (true) {
            // SYSDBA не надо ничего делать - он и так в доступе
            if (proguserId == Proguser.SYSDBA_PROGUSER_ID) {
                return;
            }
            // Список ролей пользователя из базы данных
            List<AccessRole> roles = proguserService.getRoleList(proguserId);
            // Удалим те, которых нет в нужных
            roles.stream().filter(r -> !accessRoleIds.contains(r.getAccessRoleId()))
                    .forEach(r -> accessRoleRepository.unbindProgUser(
                            r.getAccessRoleId(),
                            proguserId));
            // Добавим нужные, которых нет в базе данных
            accessRoleIds.stream().filter(r -> !roles.contains(r))
                    .forEach(r -> accessRoleRepository.bindWithProgUser(
                            r,
                            proguserId
                    ));
        } else {
            // удалили все связи ролей с пользователем
            accessRoleRepository.unbindProgUser(proguserId);
            // создаем новые связи ролей с пользователем
            accessRoleIds.stream()
                    .forEach(roleId -> accessRoleRepository.bindWithProgUser(roleId, proguserId));
        }
    }

}

