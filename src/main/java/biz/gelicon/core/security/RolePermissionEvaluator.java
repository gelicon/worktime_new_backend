package biz.gelicon.core.security;

import biz.gelicon.core.utils.OrmUtils;
import biz.gelicon.core.utils.ReflectUtils;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

@Component
public class RolePermissionEvaluator implements PermissionEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(RolePermissionEvaluator.class);

    @Autowired
    ACL acl;

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        MethodInvocation mi = ((UserDetailsImpl) auth.getPrincipal()).getMethodInvocation();
        RequestMapping methodRequestMapping = mi.getMethod().getAnnotation(RequestMapping.class);
        if(methodRequestMapping==null) {
            logger.warn("RequestMapping annotation not found: {}",mi.getMethod().toString());
            return false;
        }
        boolean methodIsGet = ReflectUtils.isGetMethod(mi.getMethod());
        boolean methodIsSave = ReflectUtils.isSaveMethod(mi.getMethod());
        // ищем RequestMapping у класса
        RequestMapping rootRequestMapping = mi.getMethod().getDeclaringClass().getAnnotation(RequestMapping.class);
        String[] prefixes = rootRequestMapping!=null?rootRequestMapping.value():new String[]{""};

        // по всем url в RequestMapping должен быть доступ
        for (String pref:prefixes) {
            for (String url:methodRequestMapping.value()) {
                String fullUrl = normalizeObjectName(pref, url);

                if(!acl.checkPermission(fullUrl,(UserDetails)auth.getPrincipal(),Permission.EXECUTE)) {
                    // если метод get надо еще проверить расширенные права
                    if(methodIsGet) {
                        // вызов на редактирование или для добавления
                        fullUrl = makeSuffixForGet(fullUrl,mi);
                        if(!acl.checkPermission(fullUrl,(UserDetails)auth.getPrincipal(),Permission.EXECUTE)) {
                            return false;
                        }
                        // право есть, несмотря на отсутствие прав по основному url
                    } else
                        // если метод save надо еще проверить расширенные права
                        if(methodIsSave) {
                            // вызов save для вставки или обновления записи
                            fullUrl = makeSuffixForSave(fullUrl,mi);
                            if(!acl.checkPermission(fullUrl,(UserDetails)auth.getPrincipal(),Permission.EXECUTE)) {
                                return false;
                            }
                            // право есть, несмотря на отсутствие прав по основному url
                        } else {
                            return false;
                        }
                }
            }
        }

        return true;
    }

    private String makeSuffixForGet(String fullUrl, MethodInvocation mi) {
        // ищем параметр аннотированный RequestBody
        Parameter postBody = null;
        int idxParam = -1;
        Parameter[] parameters = mi.getMethod().getParameters();
        for (int i = 0; i < parameters.length ; i++) {
            Parameter p = parameters[i];
            if(p.isAnnotationPresent(RequestBody.class)) {
                postBody = p;
                idxParam = i;
                break;
            }
        }
        // тип параметры должен быть Integer
        if(postBody==null || !postBody.getType().isAssignableFrom(Integer.class)) {
            return fullUrl;
        }
        Object value = mi.getArguments()[idxParam];
        if(value==null) {
            return fullUrl+"#add";
        } else {
            return fullUrl+"#edit";
        }
    }

    private String makeSuffixForSave(String fullUrl, MethodInvocation mi) {
        // ищем параметр аннотированный RequestBody
        Parameter postBody = null;
        int idxParam = -1;
        Parameter[] parameters = mi.getMethod().getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            if (p.isAnnotationPresent(RequestBody.class)) {
                postBody = p;
                idxParam = i;
                break;
            }
        }
        if(postBody!=null) {
            Object value = mi.getArguments()[idxParam];
            if(value!=null) {
                Field idFld = OrmUtils.getIdField(value.getClass(), false);
                Integer idValue = OrmUtils.getIdValueIntegerOfField(idFld, value);
                if(idValue==null) {
                    return fullUrl+"#ins";
                } else {
                    return fullUrl+"#upd";
                }
            }
        }
        return fullUrl;
    }

    private String normalizeObjectName(String pref, String url) {
        pref = pref.endsWith("/")?pref:pref+"/";
        url = url.startsWith("/")?url.substring(1):url;
        return pref+url;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }
}
