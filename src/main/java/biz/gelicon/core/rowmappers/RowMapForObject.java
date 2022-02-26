package biz.gelicon.core.rowmappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RowMapForObject<T>  implements RowMapper<T> {
    private static final Logger logger = LoggerFactory.getLogger(RowMapForObject.class);

    private final Class objectClass;
    private Constructor<T> constructor;
    private ResultSetMetaData resultSetMetaData;
    private List<Method> columnSetters = new ArrayList<>();

    public RowMapForObject(Class objectClass) {
        this.objectClass = objectClass;
        prepareMapper();
    }

    private void prepareMapper() {
        try {
            constructor = objectClass.getConstructor();
        } catch (Exception ex) {
            throw new RuntimeException
                    (String.format("Default constructor %s() not found", objectClass.getClass().getName()), ex);
        }
    }

    private void prepareDataSet(ResultSet resultSet) throws SQLException {
        if(resultSetMetaData==null) {
            resultSetMetaData = resultSet.getMetaData();
            int colCount = resultSetMetaData.getColumnCount();
            for (int idx = 1; idx <= colCount; idx++) {
                String colName = resultSetMetaData.getColumnLabel(idx).toLowerCase();
                String setterName = "set"+ colName.substring(0, 1).toUpperCase()+ colName.substring(1);
                Method methodSet = null;
                try {
                    Field fld = objectClass.getDeclaredField(colName);
                    methodSet = objectClass.getDeclaredMethod(setterName,new Class[]{fld.getType()});
                    columnSetters.add(methodSet);
                } catch (NoSuchMethodException | NoSuchFieldException e) {
                    logger.warn(String.format("Setter %s for colum %s not found ", setterName,colName));
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
                    methodSet.invoke(obj, value);
                }
            }
            return obj;
        } catch ( InstantiationException ex) {
            throw new RuntimeException("Instance object for record error",ex);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException("Invoke setter for field fault",ex);
        }
    }
}
