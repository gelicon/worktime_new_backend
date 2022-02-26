package biz.gelicon.core.rowmappers;

import biz.gelicon.core.utils.OrmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import javax.persistence.OneToMany;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class RowMapForAnnotation<T> implements RowMapper<T> {
    private static final Logger logger = LoggerFactory.getLogger(RowMapForAnnotation.class);

    private final Class objectClass;
    private Constructor<T> constructor;
    private ResultSetMetaData resultSetMetaData;
    private List<Method> columnSetters = new ArrayList<>();
    private List<JoinField> columnsOneToMany = new ArrayList<>();

    public RowMapForAnnotation(Class objectClass) {
        this.objectClass = objectClass;
        prepareMapper();
    }

    private void prepareMapper() {
        constructor = getDefaultConstructor(objectClass);
    }

    static private <M> Constructor<M> getDefaultConstructor(Class cls) {
        try {
            return cls.getConstructor();
        } catch (Exception ex) {
            throw new RuntimeException
                    (String.format("Default constructor %s() not found", cls.getClass().getName()), ex);
        }
    }

    static private <M> Constructor<M> getDefaultCollectionConstructor(Class cls) {
        Class implClass;

        if(cls.isAssignableFrom(List.class)) {
            implClass = ArrayList.class;
        } else
        if(cls.isAssignableFrom(Set.class)) {
            implClass = HashSet.class;
        } else {
            throw new RuntimeException(String.format("Unsuitable class %s", cls.getName()));
        };
        try {
            return implClass.getConstructor();
        } catch (Exception ex) {
            throw new RuntimeException
                    (String.format("Default constructor %s() not found", cls.getClass().getName()), ex);
        }
    }

    static private Method getSetter(Field fld) {
        String setterName = "set"+ fld.getName().substring(0, 1).toUpperCase()+ fld.getName().substring(1);
        try {
            return fld.getDeclaringClass().getDeclaredMethod(setterName,new Class[]{fld.getType()});
        } catch (NoSuchMethodException e) {
            logger.warn(String.format("Setter %s for field %s not found ", setterName,fld.getName()));
            return null;
        }
    };

    static private Method getGetter(Field fld) {
        String getterName = "get"+ fld.getName().substring(0, 1).toUpperCase()+ fld.getName().substring(1);
        try {
            return fld.getDeclaringClass().getDeclaredMethod(getterName);
        } catch (NoSuchMethodException e) {
            logger.warn(String.format("Setter %s for field %s not found ", getterName,fld.getName()));
            return null;
        }
    };

    private void prepareDataSet(ResultSet resultSet) throws SQLException {
        if(resultSetMetaData==null) {
            // все классы, связанные через аннотацию OneToMany
            List<Field> oneToManyFields =
                    OrmUtils.getOneToManyFields(objectClass).stream()
                            .collect(Collectors.toList());

            resultSetMetaData = resultSet.getMetaData();
            int colCount = resultSetMetaData.getColumnCount();
            for (int idx = 1; idx <= colCount; idx++) {
                String colName = resultSetMetaData.getColumnLabel(idx).toLowerCase();
                Field fld = OrmUtils.getField(objectClass, colName);
                if(fld!=null) {
                    columnSetters.add(getSetter(fld));
                    columnsOneToMany.add(null);
                } else {
                    // поиск поля в связанном классе
                    JoinField joinFld = oneToManyFields.stream()
                            .map(f->{
                                Class cls = f.getAnnotation(OneToMany.class).targetEntity();
                                return new JoinField(f,OrmUtils.getField(cls, colName));
                            })
                            .filter(jf->jf.targetField !=null)
                            .findFirst()
                            .orElse(null);
                    if(joinFld!=null) {
                        columnSetters.add(getSetter(joinFld.targetField));
                        columnsOneToMany.add(joinFld);
                    } else {
                        columnSetters.add(null);
                        columnsOneToMany.add(null);
                    }
                }
            }
        }
    }

    @Override
    public T mapRow(ResultSet resultSet, int i) throws SQLException {
        prepareDataSet(resultSet);
        T obj = null;
        try {
            obj = constructor.newInstance();
            int colCount = resultSetMetaData.getColumnCount();
            for (int idx = 1; idx <= colCount; idx++) {
                Method methodSet = columnSetters.get(idx-1);
                if(methodSet!=null) {
                    Object value = resultSet.getObject(idx);
                    // для основного объекта
                    if(methodSet.getDeclaringClass().isAssignableFrom(objectClass)) {
                        methodSet.invoke(obj, value);
                    } else {
                    // для связанных объектов OneToMany
                        JoinField columnOneToMany = columnsOneToMany.get(idx - 1);
                        if(columnOneToMany!=null) {
                            // если поле аннотированное OneToMany является null то getTarget создает его (это collection ),
                            // также создает один связанный объект и добавляет его в collection
                            // Если collection уже создана и нее извлекается единственный элемент
                            Object target = getTarget(obj,columnOneToMany);
                            columnOneToMany.targetSetter.invoke(target,value);
                        }
                    }
                }
            }
            return obj;
        } catch ( InstantiationException ex) {
            throw new RuntimeException("Instance object for record error",ex);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException("Invoke setter for field fault",ex);
        }
    }

    private Object getTarget(T obj, JoinField columnOneToMany) throws ReflectiveOperationException {
        // получаем Collection
        Collection<Object> list = (Collection<Object>) columnOneToMany.parentGetter.invoke(obj);
        if(list==null) {
            list = (Collection<Object>) columnOneToMany.parentConstructor.newInstance();
            Object target = columnOneToMany.targetConstructor.newInstance();
            list.add(target);
            columnOneToMany.parentSetter.invoke(obj,list);
            return target;
        } else {
            return list.iterator().next();
        }
    }

    static class JoinField {
        // поле класса, который содержит many object (List, Set  и т.д)
        Field parentField;
        Method parentSetter;
        Method parentGetter;
        Constructor parentConstructor;

        // поле target класса, которому нужно присвоить значение
        Field targetField;
        Method targetSetter;
        Constructor targetConstructor;

        public JoinField(Field parentField, Field targetField) {
            this.parentField = parentField;
            this.parentSetter = getSetter(parentField);
            this.parentGetter = getGetter(parentField);
            this.parentConstructor = getDefaultCollectionConstructor(parentField.getType());

            this.targetField = targetField;
            if(targetField!=null) {
                this.targetSetter = getSetter(targetField);
                this.targetConstructor=getDefaultConstructor(targetField.getDeclaringClass());
            }
        }
    }
}
