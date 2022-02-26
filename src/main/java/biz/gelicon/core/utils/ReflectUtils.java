package biz.gelicon.core.utils;

import biz.gelicon.core.annotations.MethodPermission;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class ReflectUtils {

    /**
     * Условия:
     *  - есть RequestMapping с методом POST,
     *  - value у RequestMapping заканчивается на get
     *  - нет MethodPermission с disableGetAndSaveExtension==true
     *
     *  расширенные url для get:
     *  get#add - получение записи для добавления
     *  get#edit - получение записи для изменения
     *
     * @param method
     * @return
     */
    public static boolean isGetMethod(Method method) {
        return isRequestMappingMethodWithSuffix(method,"get");
    }

    /**
     * Условия:
     *  - есть RequestMapping с методом POST,
     *  - value у RequestMapping заканчивается на save
     *  - нет MethodPermission с disableGetAndSaveExtension==true

     *  расширенные url для save:
     *  save#ins - получение записи для вставки
     *  save#upd - получение записи для обновления
     *
     * @param method
     * @return
     */
    public static boolean isSaveMethod(Method method) {
        return isRequestMappingMethodWithSuffix(method,"save");
    }

    public static boolean isRequestMappingMethodWithSuffix(Method method, String suffix) {
        RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
        // метод отключен явно
        MethodPermission permMethod = method.getAnnotation(MethodPermission.class);
        if(permMethod!=null && permMethod.noStore()) {
            return false;
        }
        if(methodRequestMapping!=null) {
            boolean isPost = Stream.of(methodRequestMapping.method()).anyMatch(m -> m == RequestMethod.POST);
            if(isPost) {
                for (String url:methodRequestMapping.value()) {
                    if(url.endsWith(suffix)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String[] getNullPropertyNames (Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for(java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }

        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }


}
