package biz.gelicon.core.service;

import biz.gelicon.core.model.AccessRole;
import biz.gelicon.core.model.CapCode;
import biz.gelicon.core.model.Proguser;
import biz.gelicon.core.model.ProguserChannel;
import biz.gelicon.core.repository.AccessRoleRepository;
import biz.gelicon.core.repository.ProgUserRepository;
import biz.gelicon.core.repository.ProguserChannelRepository;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.utils.ProcessNamedFilter;
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
    public static final String ALIAS_MAIN = "m0";

    @Autowired
    private ProgUserRepository proguserRepository;
    @Autowired
    private ProguserValidator proguserValidator;
    @Autowired
    private ProguserChannelRepository proguserChannelRepository;
    @Autowired
    private AccessRoleRepository accessRoleRepository;

    // главный запрос. используется в главной таблице
    // в контроллере используется в getlist и save
    final String mainSQL=String.format(
            "SELECT %s.*, " +
                    "PUC.proguserchannel_address, " +
                    "CC.capcode_name proguser_status_display " +
            "FROM proguser %1$s " +
                  "INNER JOIN capcode CC ON CC.capcode_id=%1$s.proguser_status_id " +
                  "LEFT OUTER JOIN proguserchannel PUC ON PUC.proguser_id = %1$s.proguser_id AND PUC.channelnotification_id = 9001 " +
                  "/*FROM_PLACEHOLDER*/ " +
            "WHERE 1=1 /*WHERE_PLACEHOLDER*/ " +  //WHERE_PLACEHOLDER если не пуст, всегда добавляет and, поэтому требуется 1=1
            "/*ORDERBY_PLACEHOLDER*/"
            ,ALIAS_MAIN);


    @PostConstruct
    public void init() {
        init(proguserRepository, proguserValidator);
    }

    public List<ProguserView> getMainList(GridDataOption gridDataOption) {
        Map<String, String> subsColumns = new HashMap<>();
        subsColumns.put("proguser_status_display","CC.capcode_name");
        subsColumns.put("proguserchannel_address","PUC.proguserchannel_address");

        return new Query.QueryBuilder<ProguserView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setPageableAndSort(gridDataOption.buildPageRequest())
                .setFrom(gridDataOption.buildFullTextJoin("proguser",ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(ProguserView.class,ALIAS_MAIN,subsColumns))
                .setParams(gridDataOption.buildQueryParams())
                .build(ProguserView.class)
                .execute();
    }

    public int getMainCount(GridDataOption gridDataOption) {
        Map<String, String> subsColumns = new HashMap<>();
        subsColumns.put("proguser_status_display","CC.capcode_name");
        subsColumns.put("proguserchannel_address","PUC.proguserchannel_address");

        return new Query.QueryBuilder<ProguserView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setFrom(gridDataOption.buildFullTextJoin("proguser",ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(ProguserView.class,ALIAS_MAIN,subsColumns))
                .setParams(gridDataOption.buildQueryParams())
                .build(ProguserView.class)
                .count();

    }


    public ProguserView getOne(Integer id) {
        return new Query.QueryBuilder<ProguserView>(mainSQL)
                .setPredicate(ALIAS_MAIN+".proguser_id=:proguserId")
                .build(ProguserView.class)
                .executeOne("proguserId", id);
    }

    public ProguserChannel getlEmail(Integer proguserId) {
        return proguserChannelRepository.getlEmail(proguserId);
    }

    public void saveEmail(Integer proguserId, String address) {
        proguserChannelRepository.saveEmail(proguserId,address);
    }


    public List<AccessRole> getRoleList(Integer proguserId) {
        List<AccessRole> list = accessRoleRepository.findByUser(proguserId);
        return list.stream()
                .sorted((r1,r2)->r1.getAccessRoleName().compareTo(r2.getAccessRoleName()))
                .collect(Collectors.toList());
    }

    public void saveRoles(Integer proguserId, List<Integer> accessRoleIds) {
        // удалили все связи ролей с пользователем
        accessRoleRepository.unbindProgUser(proguserId);
        // создаем новые связи ролей с пользователем
        accessRoleIds.stream()
                .forEach(roleId->accessRoleRepository.bindWithProgUser(roleId,proguserId));

    }

    public List<ProguserView> getListForFind(GridDataOption gridDataOption) {
        return new Query.QueryBuilder<ProguserView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setPageableAndSort(gridDataOption.buildPageRequest())
                .setFrom(gridDataOption.buildFullTextJoin("proguser",ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(ProguserView.class,ALIAS_MAIN,null))
                .setParams(gridDataOption.buildQueryParams())
                .build(ProguserView.class)
                .execute();

    }
}

