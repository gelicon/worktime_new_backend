package biz.gelicon.core.repository;

import biz.gelicon.core.annotations.Cast;
import biz.gelicon.core.dialect.DialectFactory;
import biz.gelicon.core.response.exceptions.FetchQueryException;
import biz.gelicon.core.rowmappers.RowMapForAnnotation;
import biz.gelicon.core.rowmappers.RowMapperForEntity;
import biz.gelicon.core.rowmappers.RowMapperForMap;
import biz.gelicon.core.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Интерфейс работы с @Table. <p> Реализует дефолтные методы: int count() int insert(T t) int
 * update(T t) int delete(Integer id) int deleteAll() int delete(T t) int insertOrUpdate(T t) int
 * set(T t) List<T> findAll() T findById(Integer id) <p> Для работы должна быть заполнена
 * Map<String, TableMetadata> tableMetadataMap
 * @author dav
 */
@Transactional(propagation = Propagation.REQUIRED)
public interface TableRepository<T> {

    Logger logger = LoggerFactory.getLogger(TableRepository.class);
    final String ALIAS_FOR_MAIN_TABLE = "m0";

    /**
     * Коллекция из метаданных для таблиц <p> Должна быть заполнена при запуске программы для всех
     * аннотированных @Table <p> Необходимы аннотации @Table(name), @Id, @Column(name)
     */
    Map<String, TableMetadata> tableMetadataMap = new HashMap<>();

    /**
     * Возвращает количество записей в таблице
     */
    default int count() {
        TableMetadata tableMetadata = getTableMetaData();
        JdbcTemplate jdbcTemplate = OrmUtils.getJdbcTemplate();
        String sqlText = "SELECT COUNT(*) FROM " + tableMetadata.getTableName();
        OrmUtils.logSQL(sqlText);
        Integer i = jdbcTemplate.queryForObject(
                sqlText,
                Integer.class);
        return Objects.requireNonNullElse(i, 0);
    }

    /**
     * Возвращает максимальное значение первичного ключа
     * @return
     */
    default int getMaxId(){
        TableMetadata tableMetadata = getTableMetaData();
        JdbcTemplate jdbcTemplate = OrmUtils.getJdbcTemplate();
        String sqlText = String.format(" SELECT MAX(%s) FROM %s",
                                tableMetadata.getIdFieldName(),tableMetadata.getTableName());
        OrmUtils.logSQL(sqlText);
        Integer i = jdbcTemplate.queryForObject(sqlText, Integer.class);
        return Objects.requireNonNullElse(i, 0);
    }

    default Integer getIdFromEntity(T t) {
        String tableName = OrmUtils.getTableName(t); // Ключом является имя класса
        // Найдем в коллекции описание таблицы по имени
        TableMetadata tableMetadata = tableMetadataMap.get(tableName);
        // Получим значение первичного ключа
        return OrmUtils.getIdValueIntegerOfField(tableMetadata.getIdField(), t);
    }
    default void setIdToEntity(T t, Integer id) {
        String tableName = OrmUtils.getTableName(t);
        TableMetadata tableMetadata = tableMetadataMap.get(tableName);
        OrmUtils.setIdValueIntegerOfField(tableMetadata.getIdField(),t,id);
    }

    default int executeSQL(String sqlText) {
        return executeSQL(sqlText,new HashMap<>());
    }

    default int executeSQL(String sqlText,Map<String, Object> params) {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate =
                OrmUtils.getNamedParameterJdbcTemplate();
        OrmUtils.logSQL(sqlText);
        return namedParameterJdbcTemplate.execute(sqlText,params,ps->ps.executeUpdate());
    }

    default int executeSQL(String sqlText, String arg0Name, Object arg0Value) {
        HashMap<String, Object> params = new HashMap<>();
        params.put(arg0Name,arg0Value);
        return executeSQL(sqlText,params);
    }

    default int executeSQL(String sqlText, Object[] args) {
        JdbcTemplate jdbcTemplate = OrmUtils.getJdbcTemplate();
        OrmUtils.logSQL(sqlText);
        Integer result = jdbcTemplate.execute(sqlText, new PreparedStatementCallback<Integer>() {
            @Override
            public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                for (int i = 0; i < args.length; i++) {
                    if(args[i] instanceof InputStream) {
                        InputStream in = (InputStream)args[i];
                        try {
                            ps.setBinaryStream(i+1, in, in.available());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    } else {
                        ps.setObject(i+1,args[i]);
                    }
                }
                return ps.executeUpdate();
            }
        });
        return result;
    };

    default String buildInsertClause(String tableName, String[] columnNames,String[] params) {
        return buildInsertClause(tableName,Arrays.asList(columnNames),Arrays.asList(params));
    }

    default String buildInsertClause(String tableName, List<String> columnNames,List<String> params) {
        return "INSERT INTO " + tableName +
                columnNames.stream()
                    .collect(Collectors.joining(",","(", ")"))+
                " VALUES"+
                params.stream()
                    .map(p -> ":" + p)
                    .collect(Collectors.joining(",", "(", ")"));
    }

    default String buildUpdateClause(String tableName, String[] columnNames, String where) {
        return buildUpdateClause(tableName,Arrays.asList(columnNames),where);
    }

    default String buildUpdateClause(String tableName, List<String> columnNames, String where) {
        return "UPDATE " + tableName + " SET "+
                columnNames.stream()
                        .map(c->c+"=:"+c)
                        .collect(Collectors.joining(","))+
                " WHERE "+where;
    }

    default String buildDeleteClause(String tableName, String where) {
        return "DELETE FROM " + tableName + " WHERE "+where;
    }

    /**
     * Добавление записи
     *
     * @param t Объект
     * @return код успеха
     */
    default int insert(T t) {
        TableMetadata tableMetadata = getTableMetaData();
        // Получим значение первичного ключа
        Integer id = getIdFromEntity(t);
        Integer idSave = id;
        if (id == null) {
            // Сгенерируем значение
            id = DatabaseUtils.sequenceNextValue(tableMetadata.getTableName() + "_id_gen");
            // Установим у t
            setIdToEntity(t,id);
        }
        NamedParameterJdbcTemplate namedParameterJdbcTemplate =
                OrmUtils.getNamedParameterJdbcTemplate();

        List<String> columns = tableMetadata.getColumnMetadataList()
                .stream()
                .map(c -> c.getColumnName())
                .collect(Collectors.toList());
        List<String> params = tableMetadata.getColumnMetadataList()
                .stream()
                .map(c -> c.getField().getName())
                .collect(Collectors.toList());
        String sqlText = buildInsertClause(tableMetadata.getTableName(),columns,params);

        OrmUtils.logSQL(sqlText);
        try {
            int result = namedParameterJdbcTemplate.update(sqlText,
                    new BeanPropertySqlParameterSource(t));
            // ксакадные CRUD операци
            if(result>0) {
                cascadeUpdateOperation(t,CascadeType.PERSIST);
            }
            return result;
        } catch (Exception ex) {
            setIdToEntity(t,idSave);
            throw ex;
        }
    }

    default int insert(List<T> list) {
        return list.stream()
                .mapToInt(r->insert(r))
                .sum();
    }

    default void cascadeUpdateOperation(T t, CascadeType op) {
        TableMetadata tableMetadata = getTableMetaData();
        Integer id = getIdFromEntity(t);
        String idName = tableMetadata.getIdFieldName();
        // JOIN LIST Cascade
        // с необходимыми cascade в OneToMany
        tableMetadata.getJoinColumnMetadataList().stream()
                .filter(c->c.getMetaKind()== ColumnMetadata.ColumnMetaKind.JOINLIST)
                .filter(c->{
                    OneToMany oneToMany = c.getField().getAnnotation(OneToMany.class);
                    if(oneToMany!=null) {
                        List<CascadeType> cascades = Arrays.asList(oneToMany.cascade());
                        return cascades.contains(op) || cascades.contains(CascadeType.ALL);
                    };
                    return false;
                })
                .forEach(columnMetadata -> {
                    // только при изменении
                    if(op == CascadeType.MERGE) {
                        String sqlText = buildDeleteClause(columnMetadata.getJoinTableName(),
                                columnMetadata.getReferencedColumnName()+"=:"+idName);
                        Map<String,Object> paramMap = new HashMap<>();
                        paramMap.put(idName,id);
                        executeSQL(sqlText,paramMap);
                    }
                    // вставка новых данных из списка
                    List<Object> valueList;
                    try {
                        valueList = (List<Object>) columnMetadata.getMethodGet().invoke(t);
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        throw new RuntimeException(ex);
                    }
                    if(valueList!=null && !valueList.isEmpty()) {
                        String sqlText = buildInsertClause(columnMetadata.getJoinTableName(),
                                new String[]{columnMetadata.getJoinTableName()+"_id",columnMetadata.getReferencedColumnName(),columnMetadata.getColumnName()},
                                new String[]{columnMetadata.getJoinTableName()+"_id",columnMetadata.getReferencedColumnName(),columnMetadata.getColumnName()});
                        Map<String, Object> params = new HashMap<>();
                        params.put(columnMetadata.getReferencedColumnName(), id);
                        // exec insert
                        Cast castAn = columnMetadata.getField().getAnnotation(Cast.class);
                        final PropertyEditor convertor = castAn!=null?PropertyEditorManager.findEditor(castAn.toDatabase()):null;
                        if(convertor==null) {
                            throw new RuntimeException(String.format("Не найден конвертор String->%s для поля %s",
                                    castAn.toDatabase().getName(),columnMetadata.getColumnName()));
                        }
                        // цикл по всем значениям
                        valueList.stream().forEach(v->{
                            int idJoin = DatabaseUtils.sequenceNextValue(columnMetadata.getJoinTableName()+"_id_gen");
                            params.put(columnMetadata.getJoinTableName()+"_id", idJoin);
                            if(convertor!=null) {
                                // всегда String
                                convertor.setAsText((String) v);
                                params.put(columnMetadata.getColumnName(), convertor.getValue());
                            } else {
                                params.put(columnMetadata.getColumnName(), v);
                            }
                            executeSQL(sqlText,params);
                        });
                    }
                });
    }

    /**
     * Изменение записи
     *
     * @param t Объект
     * @return код успеха
     */
    default int update(T t) {
        TableMetadata tableMetadata = getTableMetaData();
        StringBuilder sqlText = new StringBuilder(
                "UPDATE " + tableMetadata.getTableName() + " SET ");
        String comma = "";
        String idName = "";
        for (int i = 0; i < tableMetadata.getColumnMetadataList().size(); i++) {
            if (!tableMetadata.getColumnMetadataList().get(i).getIdFlag()) {
                sqlText.append(comma).append(tableMetadata.getColumnMetadataList().get(i)
                        .getColumnName());
                sqlText.append(" = :")
                        .append(tableMetadata.getColumnMetadataList().get(i).getField()
                                .getName());
                if (comma.equals("")) { comma = ", "; }
            } else {
                idName = tableMetadata.getColumnMetadataList().get(i).getField().getName();
            }
        }
        sqlText.append(" WHERE ").append(tableMetadata.getIdFieldName()).append(" = :")
                .append(idName);
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = OrmUtils
                .getNamedParameterJdbcTemplate();
        OrmUtils.logSQL(sqlText.toString());

        int result = namedParameterJdbcTemplate.update(sqlText.toString(),
                new BeanPropertySqlParameterSource(t));
        // каскадные CRUD операци
        if(result>0) {
            cascadeUpdateOperation(t,CascadeType.MERGE);
        }
        return result;
    }

    /**
     * Удаление записи по первичному ключу. <p> Если вызвать с id = -123 то удалит все записи
     *
     * @param id значение первичного ключа
     * @return код успеха
     */
    default int delete(Integer id) {
        TableMetadata tableMetadata = getTableMetaData();
        String idName = tableMetadata.getIdFieldName();
        String sqlText = " DELETE FROM " + tableMetadata.getTableName()+
                         " WHERE " + idName + " = :" + idName;
        OrmUtils.logSQL(sqlText);
        NamedParameterJdbcTemplate jdbcTemplate = OrmUtils.getNamedParameterJdbcTemplate();
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put(idName,id);
        return jdbcTemplate.update(sqlText,paramMap);
    }

    /**
     * Удаление всех записей из таблицы
     */
    default int deleteAll() {
        TableMetadata tableMetadata = getTableMetaData();
        String sqlText = " DELETE FROM " + tableMetadata.getTableName();
        OrmUtils.logSQL(sqlText);
        JdbcTemplate jdbcTemplate = OrmUtils.getJdbcTemplate();
        return jdbcTemplate.update(sqlText);
    }

    /**
     * Удаление записи. <p> Должо быть установлено поле - первичный ключ
     *
     * @param t Объект
     * @return код успеха
     */
    default int delete(T t) {
        String tableName = OrmUtils.getTableName(t); // Ключем является имя класса
        // Найдем в коллекции описание таблицы по имени
        TableMetadata tableMetadata = tableMetadataMap.get(tableName);
        // Получим значение первичного ключа
        Integer id = OrmUtils.getIdValueIntegerOfField(tableMetadata.getIdField(), t);
        return delete(id);
    }

    /**
     * Добавление или изменение записи в зависимости от id
     *
     * @param t Объект
     * @return код успеха
     */
    default int insertOrUpdate(T t) {
        if (t == null) {return -1;}
        String tableName = OrmUtils.getTableName(t); // Ключем является имя класса
        // Найдем в коллекции описание таблицы по имени
        TableMetadata tableMetadata = tableMetadataMap.get(tableName);
        // Получим значение первичного ключа
        Integer id = OrmUtils.getIdValueIntegerOfField(tableMetadata.getIdField(), t);
        if (id == null) {
            return insert(t);
        } else {
            return update(t);
        }
    }

    /**
     * Добавление или изменение записи в зависимости от наличия в базе. <p> Наличие проверяет по
     * первичному ключу (если не null). Если пк = null или если записи по пк нет в базе -
     * добавляет.
     *
     * @param t Объект
     * @return код успеха
     */
    default int set(T t) {
        if (t == null) {return -1;}
        String tableName = OrmUtils.getTableName(t); // Ключем является имя класса
        // Найдем в коллекции описание таблицы по имени
        TableMetadata tableMetadata = tableMetadataMap.get(tableName);
        // Получим значение первичного ключа
        Integer id = OrmUtils.getIdValueIntegerOfField(tableMetadata.getIdField(), t);
        if (id == null || findById(id) == null) {
            return insert(t);
        } else {
            return update(t);
        }
    }

    /**
     * Возвращает все записи из таблицы без условия
     *
     * @return коллекция из объектов
     */
    default List<T> findAll() {
        return findAll((Pageable)null);
    }

    default List<T> findAll(Sort sort) {
        return findAll(QueryUtils.allPagesAndSort(sort.toList()));
    }

    default List<T> findAll(String fields) {
        List<Sort.Order> orders = Stream.of(fields.split(","))
                    .map(f -> new Sort.Order(Sort.Direction.ASC, f))
                    .collect(Collectors.toList());
        return findAll(PageRequest.of(0,Integer.MAX_VALUE,Sort.by(orders)));
    }

    /**
     * Возвращает записи из таблицы в соответствие с page и условиями where
     *
     * @param page
     * @return
     */
    default List<T> findWhere(String where, Map<String,?> args, Pageable page) {
        TableMetadata tableMetadata = getTableMetaData();
        // создаем SQL запрос
        String sqlText = OrmUtils.generateSQL(tableMetadata,ALIAS_FOR_MAIN_TABLE,where,page);
        OrmUtils.logSQL(sqlText);
        // Маппер с классом для модели
        RowMapper resultSetRowMapper = new RowMapperForEntity(getTableModelClass());
        try {
            NamedParameterJdbcTemplate jdbcTemplate = OrmUtils.getNamedParameterJdbcTemplate();
            return jdbcTemplate.query(sqlText, args, resultSetRowMapper);
        } catch (Exception e) {
            throw new FetchQueryException("SQL execute failed: " + sqlText, e);
        }
    }

    default List<T> findWhere(String where, Map<String,?> args) {
        return findWhere(where,args,null);
    }

    default List<T> findWhere(String where, String arg0Name, Object arg0Value) {
        return findWhere(where,arg0Name,arg0Value,(Pageable)null);
    }

    default List<T> findWhere(String where, String arg0Name, Object arg0Value,Pageable page) {
        Map<String,Object> args = new HashMap<>();
        args.put(arg0Name,arg0Value);
        return findWhere(where,args,page);
    }

    default List<T> findWhere(String where) {
        return findWhere(where,new HashMap<>());
    }

    /**
     * Возвращает все записи из таблицы в соответствие с page
     *
     * @param page
     * @return
     */
    default List<T> findAll(Pageable page) {
        return findWhere(null,(Map)null, page);
    }

    /**
     * Подсчет количесво записей с учетом where и args
     * @param where
     * @param args
     * @return
     */
    default int countWhere(String where, Map<String,?> args) {
        TableMetadata tableMetadata = getTableMetaData();
        // создаем SQL запрос
        String sqlText = "SELECT COUNT(*) FROM("+
                            OrmUtils.generateSQL(tableMetadata,ALIAS_FOR_MAIN_TABLE,where,null)+
                         ")  AS x "+DialectFactory.getDialect().limitAndOffset(1,null);
        OrmUtils.logSQL(sqlText);
        // Маппер с классом для модели
        RowMapper resultSetRowMapper = new RowMapperForEntity(getTableModelClass());
        try {
            NamedParameterJdbcTemplate jdbcTemplate = OrmUtils.getNamedParameterJdbcTemplate();
            return jdbcTemplate.queryForObject(sqlText, args, Integer.class);
        } catch (Exception e) {
            throw new FetchQueryException("SQL execute failed: " + sqlText, e);
        }
    }


    /**
     * Возвращает запись по id
     *
     * @param id - значение первичного ключа
     * @return объект
     */
    default T findById(Integer id) {
        TableMetadata tableMetadata = getTableMetaData();
        String sqlText = " SELECT * FROM " + tableMetadata.getTableName()
                + " WHERE " + tableMetadata.getIdFieldName() + " = :id";
        OrmUtils.logSQL(sqlText);
        // Маппер с классом для модели
        RowMapper resultSetRowMapper = new RowMapperForEntity(getTableModelClass());
        List<T> tList = null;
        try {
            NamedParameterJdbcTemplate jdbcTemplate = OrmUtils.getNamedParameterJdbcTemplate();
            Map<String,Integer> params = new HashMap<>();
            params.put("id",id);
            tList = jdbcTemplate.query(sqlText, params, resultSetRowMapper);
        } catch (Exception e) {
            String errText = "SQL execute failed: " + sqlText;
            throw new RuntimeException(errText, e);
        }
        if (tList.size() == 0) {
            return null;
        }
        if (tList.size() > 1) {
            String errText = String.format("Too many rows for %s", sqlText);
            throw new RuntimeException(errText);
        }
        return (T) tList.get(0);
    }

    default List<T> findById(Integer[] ids) {
        TableMetadata tableMetadata = getTableMetaData();
        JdbcTemplate jdbcTemplate = OrmUtils.getJdbcTemplate();

        String inStr = Arrays.asList(ids).stream()
                            .map(i->i.toString())
                            .collect(Collectors.joining(",","(",")"));

        String sqlText = " SELECT * FROM " + tableMetadata.getTableName()
                + " WHERE " + tableMetadata.getIdFieldName() + " IN " + inStr;
        OrmUtils.logSQL(sqlText);
        // Маппер с классом для модели
        RowMapper resultSetRowMapper = new RowMapperForEntity(getTableModelClass());
        List<T> tList = null;
        try {
            tList = jdbcTemplate.query(sqlText, resultSetRowMapper);
        } catch (Exception e) {
            String errText = "SQL execute failed: " + sqlText;
            throw new RuntimeException(errText, e);
        }
        return tList;

    }

    default T getOne(Integer id) {
        T o = findById(id);
        if(o==null) {
            throw new RuntimeException(String.format("Запись с идентификатором %d не найдена", id));
        }
        return o;
    }

    /**
     * Удаление таблицы из базы данных для тестов. Расчитано на использование в Postgres
     */
    default void dropForTest() {
        drop(true);
    }

    /**
     * Удаление таблицы из базы данных Удаляет так же последовательность Если
     * существуют
     */
    default void drop() {
        drop(false);
    }

    default void drop(boolean cascadeFlag) {
        Class cls = getTableModelClass();
        String tableName = OrmUtils.getTableName(cls); // Ключем является имя класса
        drop(tableName,cascadeFlag);
    }

    default void drop(String tableName) {
        drop(tableName,false);
    }

    default void drop(String tableName, boolean cascadeFlag) {
        JdbcTemplate jdbcTemplate = OrmUtils.getJdbcTemplate();
        // Удалим полнотекстовую view, если есть
        String viewName = "ft_"+tableName;
        if (DatabaseUtils.checkViewExist(viewName)) {
            String sqlText = "DROP VIEW IF EXISTS " + viewName + " CASCADE";
            OrmUtils.logSQL(sqlText);
            DatabaseUtils.executeSql(sqlText, jdbcTemplate);
            logger.info("View " + viewName + " dropped");
        }
        // Удалим последовательность
        String sequenceName = tableName + "_id_gen";
        if (DatabaseUtils.checkSequenceExist(sequenceName)) {
            String sqlText = " DROP SEQUENCE " + sequenceName;
            OrmUtils.logSQL(sqlText);
            DatabaseUtils.executeSql(sqlText, jdbcTemplate);
            logger.info("Sequence " + sequenceName + " dropped");
        }
        // удалим таблицу
        if (DatabaseUtils.checkTableExist(tableName)) {
            String sqlText = "DROP TABLE " + tableName;
            if(cascadeFlag) {
                sqlText = sqlText + " CASCADE";
            }
            OrmUtils.logSQL(sqlText);
            DatabaseUtils.executeSql(sqlText, jdbcTemplate);
            logger.info("Table " + tableName + " dropped");
        }

    }

    default void drop(String[] tableNames) {
        for (String tableName :tableNames) {
            drop(tableName);
        }
    }

    /**
     * Дефайлтный метод создания таблицы СОздает таблицу без полей, без последовательностей, без
     * ничего
     */
    default void create() {
        // Получим класс дженерика класса
        Class cls = getTableModelClass();
        String tableName = OrmUtils.getTableName(cls); // Ключем является имя класса
        JdbcTemplate jdbcTemplate = OrmUtils.getJdbcTemplate();
        String sqlText = "CREATE TABLE " + tableName + "()";
        OrmUtils.logSQL(sqlText);
        DatabaseUtils.executeSql(sqlText, jdbcTemplate);
        logger.info("Table " + tableName + " created");
    }

    /**
     * Дефаустный метод первоначальной загрузки ВОзвращает количество добавленных записей
     *
     * @return
     */
    default int load() {
        logger.info("0 record loaded");
        return 0;
    }

    default TableMetadata getTableMetaData() {
        Class cls = getTableModelClass();
        String tableName = OrmUtils.getTableName(cls);
        return tableMetadataMap.get(tableName);
    }

    default Class getTableModelClass() {
        return OrmUtils.getClassGenericInterfaceAnnotationTable(this.getClass());
    }

    /**
     * Формирование выборки для грида на основании произвольного запроса
     * @param sqlText - тескт запроса, должен содержать placeholders WHERE_PLACEHOLDER и ORDERBY_PLACEHOLDER
     * @param mainAlias - алиас для таблицы из которой десериализуется объект (главная таблица)
     * @param predicate - предикат. вставляется вместо WHERE_PLACEHOLDER
     * @param args - параметры запроса
     * @param page - страница и сортировки. Сортировки вставляются вместо ORDERBY_PLACEHOLDER
     * @return
     */
    default  List<T> findQuery(String sqlText, String mainAlias,String predicate, Map<String, Object> args, Pageable page) {
        TableMetadata tableMetadata = getTableMetaData();
        sqlText = OrmUtils.prepareSQLText(sqlText,mainAlias,predicate,tableMetadata,page);
        OrmUtils.logSQL(sqlText);
        // Маппер с классом для модели
        RowMapper resultSetRowMapper = new RowMapperForEntity(getTableModelClass());
        try {
            NamedParameterJdbcTemplate jdbcTemplate = OrmUtils.getNamedParameterJdbcTemplate();
            return jdbcTemplate.query(sqlText, args, resultSetRowMapper);
        } catch (Exception e) {
            throw new FetchQueryException("SQL execute failed: " + sqlText, e);
        }
    }
    default  List<T> findQuery(String sqlText, Map<String, Object> args) {
        return findQuery(sqlText,null,null,args,null);
    }
    default List<T> findQuery(String sqlText, String arg0Name, Object arg0Value) {
        Map<String,Object> args = new HashMap<>();
        args.put(arg0Name,arg0Value);
        return findQuery(sqlText,args);
    }
    default List<T> findQuery(String sqlText) {
        Map<String,Object> args = new HashMap<>();
        return findQuery(sqlText,args);
    }

    default <T> List<T> findQuery(Class<T> clz, String sqlText, Map<String, Object> args) {
        RowMapForAnnotation rowMapper = new RowMapForAnnotation(clz);
        OrmUtils.logSQL(sqlText);
        try {
            NamedParameterJdbcTemplate jdbcTemplate = OrmUtils.getNamedParameterJdbcTemplate();
            return (List<T>) jdbcTemplate.query(sqlText, args, rowMapper);
        } catch (Exception e) {
            throw new FetchQueryException("SQL execute failed: " + sqlText, e);
        }
    }

    default <T> List<T> findQuery(Class<T> clz, String sqlText, String arg0Name, Object arg0Value) {
        Map<String,Object> args = new HashMap<>();
        args.put(arg0Name,arg0Value);
        return findQuery(clz,sqlText,args);
    }

    default <T> List<T> findQuery(Class<T> clz, String sqlText) {
        Map<String,Object> args = new HashMap<>();
        return findQuery(clz,sqlText,args);
    }

    default List<Map<String,Object>> findQueryForMap(String sqlText, String mainAlias,String predicate, Map<String, Object> args, Pageable page) {
        TableMetadata tableMetadata = getTableMetaData();
        sqlText = OrmUtils.prepareSQLText(sqlText,mainAlias,predicate,tableMetadata,page);
        OrmUtils.logSQL(sqlText);
        try {
            NamedParameterJdbcTemplate jdbcTemplate = OrmUtils.getNamedParameterJdbcTemplate();
            RowMapper resultSetRowMapper = new RowMapperForMap();
            return jdbcTemplate.query(sqlText, args, resultSetRowMapper);
        } catch (EmptyResultDataAccessException noResEx) {
            return null;
        } catch (Exception e) {
            throw new FetchQueryException("SQL execute failed: " + sqlText, e);
        }
    }

    default List<Map<String,Object>> findQueryForMap(String sqlText, Map<String, Object> args) {
        return findQueryForMap(sqlText,null,null,args,null);
    }

    default List<Map<String,Object>> findQueryForMap(String sqlText) {
        return findQueryForMap(sqlText,new HashMap<>());
    }

    default List<Map<String,Object>> findQueryForMap(String sqlText, String arg0, Object value) {
        Map<String,Object> args = new HashMap<>();
        args.put(arg0,value);
        return findQueryForMap(sqlText,args);
    }

    default Map<String,Object> findQueryForOneMap(String sqlText, Map<String, Object> args) {
        List<Map<String, Object>> list = findQueryForMap(sqlText, null, null, args, null);
        // Внгимание! Это используется
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        throw new RuntimeException("The query returned more than one record");
    }

    default Map<String,Object> findQueryForOneMap(String sqlText, String arg0, Object value) {
        Map<String,Object> args = new HashMap<>();
        args.put(arg0,value);
        return findQueryForOneMap(sqlText,args);
    }

    default Map<String,Object> findQueryForOneMap(String sqlText) {
        return findQueryForOneMap(sqlText,new HashMap<>());
    }

    default <T> T findQueryForObject(Class<T> clz, String sqlText, String mainAlias,String predicate, Map<String, Object> args, Pageable page) {
        TableMetadata tableMetadata = getTableMetaData();
        sqlText = OrmUtils.prepareSQLText(sqlText,mainAlias,predicate,tableMetadata,page);
        OrmUtils.logSQL(sqlText);
        try {
            NamedParameterJdbcTemplate jdbcTemplate = OrmUtils.getNamedParameterJdbcTemplate();
            return jdbcTemplate.queryForObject(sqlText, args, clz);
        } catch (EmptyResultDataAccessException noResEx) {
            return null;
        } catch (Exception e) {
            throw new FetchQueryException("SQL execute failed: " + sqlText, e);
        }
    }

    default <T> T findQueryForObject(Class<T> clz,String sqlText, Map<String, Object> args) {
        return findQueryForObject(clz, sqlText,null,null,args,null);
    }
    default <T> T findQueryForObject(Class<T> clz,String sqlText, String arg0Name, Object arg0Value) {
        Map<String,Object> args = new HashMap<>();
        args.put(arg0Name,arg0Value);
        return findQueryForObject(clz,sqlText,args);
    }
    default <T> T findQueryForObject(Class<T> clz, String sqlText) {
        Map<String,Object> args = new HashMap<>();
        return findQueryForObject(clz,sqlText,args);
    }


    /**
     * Подсчет записи  для грида на основании произвольного запроса
     * @param sql - тескт запроса, должен содержать placeholders WHERE_PLACEHOLDER и ORDERBY_PLACEHOLDER
     * @param mainAlias - алиас для таблицы из которой десериализуется объект (главная таблица)
     * @param predicate - предикат. вставляется вместо WHERE_PLACEHOLDER
     * @param args - параметры запроса
     * @return
     */

    default int countQuery(String sql, String mainAlias, String predicate, Map<String, Object> args) {
        TableMetadata tableMetadata = getTableMetaData();
        // создаем SQL запрос
        String sqlText = "SELECT COUNT(*) FROM("+
                            OrmUtils.prepareSQLText(sql,mainAlias,predicate,tableMetadata,null)+
                        ")  AS x "+DialectFactory.getDialect().limitAndOffset(1,null);
        OrmUtils.logSQL(sqlText);
        try {
            NamedParameterJdbcTemplate jdbcTemplate = OrmUtils.getNamedParameterJdbcTemplate();
            return jdbcTemplate.queryForObject(sqlText, args, Integer.class);
        } catch (Exception e) {
            throw new FetchQueryException("SQL execute failed: " + sqlText, e);
        }

    }

    default int countQuery(String sqlText, Map<String, Object> args) {
        OrmUtils.logSQL(sqlText);
        try {
            NamedParameterJdbcTemplate jdbcTemplate = OrmUtils.getNamedParameterJdbcTemplate();
            return jdbcTemplate.queryForObject(sqlText, args, Integer.class);
        } catch (Exception e) {
            throw new FetchQueryException("SQL execute failed: " + sqlText, e);
        }
    }

    default <F> F getForeignKeyEntity(T obj, Class<F> target) {
        List<Field> fields = Arrays.stream(obj.getClass().getDeclaredFields())
                .filter(f -> f.getAnnotation(ManyToOne.class)!=null && f.getAnnotation(Column.class)!=null)
                .filter(f -> f.getAnnotation(ManyToOne.class).targetEntity()==target)
                .collect(Collectors.toList());
        if(fields.isEmpty())
            throw new RuntimeException(String.format("Unsuitable target %s. Field@ManyToOne with this result type was not found.",target.getName()));
        if(fields.size()>1)
            throw new RuntimeException(String.format("Unsuitable target %s.  Many fields Field@ManyToOne are of this same result type.",target.getName()));
        Field field = fields.get(0);
        field.setAccessible(true);
        String tableName = OrmUtils.getTableName(target);
        if(tableName==null)
            throw new RuntimeException(String.format("Unsuitable target %s. @Table for %s not found",target.getName(),field.getType().getName()));
        TableMetadata tableMetadata = tableMetadataMap.get(tableName);
        // Получим запрос для звлечения объекта
        try {
            String sqlText = String.format("SELECT * FROM %s WHERE %s=:value",
                    tableName,tableMetadata.getIdFieldName());
            OrmUtils.logSQL(sqlText);
            RowMapper resultSetRowMapper = new RowMapperForEntity(target);
            NamedParameterJdbcTemplate jdbcTemplate = OrmUtils.getNamedParameterJdbcTemplate();
            Map<String,Object> params = new HashMap<>();
            params.put("value",field.get(obj));
            List<F> list = jdbcTemplate.query(sqlText,params, resultSetRowMapper);
            return list.isEmpty()?null:list.get(0);

        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format("Unsuitable target %s: %s",target.getName(),e.getMessage()));
        }

    }

    /**
     * Проверяет есть ли у запроса результат
     *
     * @param sqlText
     * @param args
     * @return
     */
    default boolean findQueryExists(String sqlText,Map<String, Object> args) {
        String sqlExists = DialectFactory.getDialect().existsSql(sqlText);
        List<Map<String, Object>> list = findQueryForMap(sqlExists, args);
        return !list.isEmpty();
    }

    default boolean findQueryExists(String sqlText) {
        return findQueryExists(sqlText,new HashMap<>());
    }


}



