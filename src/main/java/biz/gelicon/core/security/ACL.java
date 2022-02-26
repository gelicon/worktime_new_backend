package biz.gelicon.core.security;

import biz.gelicon.core.repository.AccessRoleRepository;
import biz.gelicon.core.view.ObjectRoleView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ACL {
    private static final Logger logger = LoggerFactory.getLogger(ACL.class);
    @Autowired
    AccessRoleRepository accessRoleRepository;

    private Map<String, List<String>> accessTable = new HashMap<>();

    public boolean checkPermission(String accobject, UserDetails user, Permission permission) {
        // системному пользователю можно все
        if(((UserDetailsImpl)user).isSysDba()) return true;
        // список ролей пользователя
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        if(authorities.isEmpty())
            return false;
        // получаем список ролей для объекта и операции
        List<String> objectRoles = accessTable.get(genKey(accobject, permission.ordinal()));
        // вообще нет ролей
        if(objectRoles==null)
            return false;
        // ищем пересечение
        GrantedAuthority intersect = authorities.stream()
                .filter(gauth -> objectRoles.contains(gauth.getAuthority()))
                .findFirst() // хотя бы одна роль найдена
                .orElse(null);
        return intersect!=null;
    }

    public void buildAccessTable() {
        logger.info("Build ACL...");
        accessTable.clear();
        List<ObjectRoleView> list = accessRoleRepository.findAllObjectRoles();
        accessTable = list.stream()
                .collect(Collectors.groupingBy(ACL::genKey,
                            Collectors.mapping(orv->genRoleName(orv.accessRoleId),Collectors.toList())));
        logger.info("Build ACL [Ok]");
    }

    public static String genKey(ObjectRoleView orv) {
        return  genKey(orv.getControlObjectUri(),orv.getSqlActionId());
    }

    public static String genKey(String accobject,Integer sqlAction) {
        return  accobject+"::"+sqlAction;
    }

    public static String genRoleName(Integer roleId) {
        return "R#"+roleId;
    }


}
