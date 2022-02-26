package biz.gelicon.core.rowmappers;

import biz.gelicon.core.repository.TableRepository;
import biz.gelicon.core.utils.ColumnMetadata;
import biz.gelicon.core.utils.OrmUtils;
import biz.gelicon.core.utils.TableMetadata;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RowMapperForEntity implements RowMapper<Object> {

    private final Class entityClass;
    private Constructor<?> constructor;
    private ResultSetMetaData resultSetMetaData;
    private List<Method> columnSetters = new ArrayList<>();
    private List<ColumnMetadata> columnJoinMetaDatas =  new ArrayList<>();

    public RowMapperForEntity(Class cls) {
        this.entityClass = cls;
        prepareMapper();
    }

    private void prepareMapper() {
        try {
            constructor = entityClass.getConstructor();
        } catch (Exception ex) {
            throw new RuntimeException
                    (String.format("Default constructor %s() not found", entityClass.getClass().getName()), ex);
        }
    }

    private void prepareDataSet(ResultSet resultSet) throws SQLException {
        if(resultSetMetaData==null) {
            resultSetMetaData = resultSet.getMetaData();
            String tableName = OrmUtils.getTableName(entityClass);
            TableMetadata tableMetadata = TableRepository.tableMetadataMap.get(tableName);
            int colCount = resultSetMetaData.getColumnCount();
            for (int idx = 1; idx <= colCount; idx++) {
                ColumnMetadata metaColumn = null;
                String colName = resultSetMetaData.getColumnLabel(idx).toLowerCase();
                Method methodSet = tableMetadata.getColumnMetadataList().stream()
                        .filter(c -> c.getColumnName().equalsIgnoreCase(colName))
                        .findAny()
                        .map(ColumnMetadata::getMethodSet)
                        .orElse(null);
                if(methodSet==null) {
                    // имя колонки должно совпадать с именем поля в объекте
                    methodSet = tableMetadata.getJoinColumnMetadataList().stream()
                            .filter(c -> c.getField().getName().equalsIgnoreCase(colName))
                            .findAny()
                            .map(ColumnMetadata::getMethodSet)
                            .orElse(null);
                    metaColumn = tableMetadata.getJoinColumnMetadataList().stream()
                            .filter(c -> c.getField().getName().equalsIgnoreCase(colName))
                            .findAny()
                            .orElse(null);
                }
                columnSetters.add(methodSet);
                // !!! только для join column, для остальных null
                columnJoinMetaDatas.add(metaColumn);
            }
        }
    }

    @Override
    public Object mapRow(ResultSet resultSet, int i) throws SQLException {
        prepareDataSet(resultSet);
        Object obj = null;
        try {
            obj = constructor.newInstance();
            int colCount = resultSetMetaData.getColumnCount();
            for (int idx = 1; idx <= colCount; idx++) {
                Method methodSet = columnSetters.get(idx-1);
                if(methodSet!=null) {
                    // анализируем если ли тут join. Для простых полей здесь null
                    ColumnMetadata meta = columnJoinMetaDatas.get(idx - 1);
                    if(meta!=null) {
                        // работа со списком
                        Object value = resultSet.getObject(idx);
                        if(value!=null) {
                            String[] values = ((String) value).split(",");
                            methodSet.invoke(obj, Arrays.asList(values));
                        } else {
                            methodSet.invoke(obj, Collections.emptyList());
                        }
                    } else {
                        // Обычное присваивание
                        Object value = resultSet.getObject(idx);
                        methodSet.invoke(obj, value);
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


}
