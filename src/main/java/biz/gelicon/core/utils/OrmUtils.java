package biz.gelicon.core.utils;

import biz.gelicon.core.MainApplication;
import biz.gelicon.core.annotations.Cast;
import biz.gelicon.core.annotations.OrderBy;
import biz.gelicon.core.dialect.DialectFactory;
import biz.gelicon.core.response.exceptions.BadPagingException;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Вспомогательный класс с различными утилитами типа Jpa
 */
public class OrmUtils {

    static Logger logger = LoggerFactory.getLogger(OrmUtils.class);

    public final static String PLACEHOLDER_WHERE = "/*WHERE_PLACEHOLDER*/";
    public final static String PLACEHOLDER_ORDER = "/*ORDERBY_PLACEHOLDER*/";
    public static final String PLACEHOLDER_FROM = "/*FROM_PLACEHOLDER*/";

    /**
     * Возвращает имя таблицы у объекта аннотированного как @Table
     *
     * @param o Объект-модель
     * @return имя таблицы
     */
    public static String getTableName(Object o) {
        Class cls = o.getClass();
        return getTableName(cls);
    }

    /**
     * Возвращает имя таблицы у Класса аннотированного как @Table
     *
     * @param cls класс модели
     * @return имя таблицы
     */
    public static String getTableName(Class cls) {
        if (cls.isAnnotationPresent(Table.class)) { // Проверим аннотацию Table
            // Если есть - получим имя таблицы
            Table tableAnnotation = (Table) cls.getAnnotation(Table.class);
            return tableAnnotation.name();
        }
        return null;
    }

    /**
     * Возвращает Поле аннотированного как @Id и @Column объекта
     *
     * @param o Объект-модель
     * @return имя первичного ключа в базе данных
     */
    public static Field getIdField(Object o) {
        Class cls = o.getClass();
        return getIdField(cls);
    }

    /**
     * Возвращает Поле аннотированного как @Id и @Column класса
     *
     * @param cls Класс объекта-модели
     * @return имя первичного ключа в базе данных
     */
    public static Field getIdField(Class cls) {
        return getIdField(cls,true);
    }

    /**
     * Возвращает Поле аннотированного как @Id и @Column класса (опционно)
     *
     * @param cls Класс объекта-модели
     * @return имя первичного ключа в базе данных
     */
    public static Field getIdField(Class cls, boolean columnNeeded) {
        Set<Field> fields = ReflectionUtils.getAllFields(cls);
        return fields.stream()
                .filter(c -> c.isAnnotationPresent(Id.class)) // Проверим аннотацию Id
                .filter(c -> !columnNeeded || (columnNeeded && c.isAnnotationPresent(Column.class)))
                .findFirst()
                .orElse(null);
    }

    /**
     * Возвращает имя поля в базе данных из поля объекта аннотированного @Column
     *
     * @param f Field объекта
     * @return имя поля в базе данных, соответствующее f
     */
    public static String getColumnName(Field f) {
        if (f.isAnnotationPresent(Column.class)) {
            return ((Column) f.getAnnotation(Column.class)).name();
        }
        return null;
    }

    public static String getColumnName(Class cls,String fname) {
        Set<Field> fields = ReflectionUtils.getAllFields(cls);
        return fields.stream()
                .filter(f -> f.getName().equals(fname))
                .filter(f -> f.isAnnotationPresent(Column.class))
                .map(f->f.getAnnotation(Column.class).name())
                .findFirst()
                .orElse(null);
    }

    public static Column getColumn(Class cls,String fname) {
        Set<Field> fields = ReflectionUtils.getAllFields(cls);
        return fields.stream()
                .filter(f -> f.getName().equals(fname))
                .filter(f -> f.isAnnotationPresent(Column.class))
                .map(f->f.getAnnotation(Column.class))
                .findFirst()
                .orElse(null);
    }

    public static Field getField(Class cls,String colName) {
        Set<Field> fields = ReflectionUtils.getAllFields(cls);
        return fields.stream()
                .filter(f -> f.isAnnotationPresent(Column.class))
                .filter(f->f.getAnnotation(Column.class).name().equalsIgnoreCase(colName))
                .findFirst()
                .orElse(null);
    }

    public static List<Field> getOneToManyFields(Class cls) {
        Set<Field> fields = ReflectionUtils.getAllFields(cls);
        return fields.stream()
                .filter(f -> f.isAnnotationPresent(OneToMany.class))
                .collect(Collectors.toList());
    }

    /**
     * Возвращает значение первичного ключа из поля объекта
     *
     * @param f Поле соответствующее первочному ключу
     * @param o объект
     * @return значение поля
     */
    public static Integer getIdValueIntegerOfField(Field f, Object o) {
        f.setAccessible(true); // Дадим доступ к полю
        try {
            return (Integer) f.get(o);
        } catch (IllegalAccessException e) {
            String errText = String
                    .format("No access for %s field of % object", f.toString(), o.toString());
            logger.error(errText, e);
        }
        return null;
    }

    public static void setIdValueIntegerOfField(Field f, Object o, Integer id) {
        f.setAccessible(true); // Дадим доступ к полю
        try {
            f.set(o,id);
        } catch (IllegalAccessException e) {
            String errText = String
                    .format("No access for %s field of % object", f.toString(), o.toString());
            logger.error(errText, e);
        }
    }

    /**
     * Возвращает созданную из контекста JdbcTemplate
     *
     * @return JdbcTemplate
     */
    public static JdbcTemplate getJdbcTemplate() {
        ApplicationContext applicationContext = MainApplication.getApplicationContext();
        DataSource dataSource = applicationContext.getBean(DataSource.class);
        return new JdbcTemplate(dataSource);
    }

    /**
     * Возвращает созданную из контекста NamedParameterJdbcTemplate
     *
     * @return NamedParameterJdbcTemplate
     */
    public static NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        DataSource dataSource = MainApplication.getApplicationContext()
                .getBean(DataSource.class);
        return new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Возвращает класс дженерика интерфейса аннотированного как @Table
     *
     * @param clsOwn Объект реализующий интерфейс TableRepository
     * @return Класс дженерика объекта
     */
    public static Class getClassGenericInterfaceAnnotationTable(Class clsOwn) {
        Type[] genericInterfaces = clsOwn.getGenericInterfaces(); // Все реализованные интерфейсы объекта
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) { // Проверим не из ParameterizedType ли он создан ???
                Type[] genericTypes = ((ParameterizedType) genericInterface) // Его аргументы
                        .getActualTypeArguments();
                for (Type genericType : genericTypes) {
                    if (genericType instanceof Class) { // Если это класс - возвращаем
                        Class cls = (Class) genericType;
                        if (cls.isAnnotationPresent(Table.class)) { // Проверим аннотацию Table
                            return (Class) genericType;
                        }
                    }
                }
            }
        }
        // Не нашли
        return null;
    }

    /**
     * Заменяет описания полей на наименования в сотрировке
     *
     * @param page
     * @param tableMetadata
     * @return
     */
    public static Pageable transformSortColumnName(Pageable page, TableMetadata tableMetadata) {
        if (page == null || tableMetadata == null) {return null;}
        List<Sort.Order> orders = new ArrayList<>(); // пустая сортировка
        // Цикл по всем полям сортировки
        StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        page.getSort().iterator(),
                        Spliterator.ORDERED),
                false)
                .forEach(s -> {
                    String columnName = s.getProperty();
                    // Найдем его в списке колонок
                    if (tableMetadata.getColumnMetadataList().stream()
                            .map(ColumnMetadata::getColumnName)
                            .filter(cn -> cn.equals(s.getProperty()))
                            .findAny()
                            .orElse(null) == null) {
                        // Его нет - надо искать
                        // Попробуем найти по наименованию поля
                        String columnNameNew = tableMetadata.getColumnNameByFieldName(s.getProperty());
                        if (columnNameNew != null) {
                            // Нашли - переприсваиваем
                            columnName = columnNameNew;
                        }
                    }
                    orders.add(new Sort.Order(s.getDirection(), columnName));
                });
        page = PageRequest.of(page.getPageNumber(), page.getPageSize(), Sort.by(orders));
        return page;
    }

    public static TwoTuple<String, Map<String,Object>> buildWhereFromGridDataOption(GridDataOption options,TableMetadata tableMetadata) {
        Map<String,Object> agrs = new HashMap<>();
        String where = options.getQuickFilters().stream()
                .map(f-> {
                    switch (f.getOper()) {
                        case eq:
                            agrs.put(f.getField(),f.getValue());
                            break;
                        case like:
                            agrs.put(f.getField(),"%"+f.getValue().toString()+"%");
                            break;
                    }
                    String columnName = tableMetadata.getColumnNameByFieldName(f.getField());
                    if(columnName==null)
                        throw new RuntimeException(String.format("Колонка с именем %s не найдена", f.getField()));

                    return columnName +GridDataOption.getOperationSQL(f.getOper(),f.getField());
                })
                .collect(Collectors.joining(" and "));
        options.getNamedFilters().stream()
                .forEach(f-> {
                    agrs.put(f.getName(),f.getValue());
                });
        if(where.isEmpty() && !options.getNamedFilters().isEmpty()) {
            where = "1=1"; // чтобы был вызван processor
        }
        return new TwoTuple<>(where,agrs);

    }

    public static String generateSQL(TableMetadata tableMetadata,String alias, String where, Pageable page) {
        if(alias==null) {
            alias = "topq";
        }
        // Сформируем текст запроса
        StringBuilder sqlTextBuilder = new StringBuilder("SELECT ")
                // поля
                .append(genSelectClause(tableMetadata, alias))
                // from
                .append(" FROM ").append(tableMetadata.tableName).append(" ").append(alias);
        if(where!=null) {
            sqlTextBuilder.append(" WHERE ").append(where);
        }
        if (page != null) {
            // Поправим названия полей - заменим имена на колонки
            page = transformSortColumnName(page, tableMetadata);
            String orderBy = buildOrderByFromPageble(page,alias,"ORDER BY ");
            String limit = buildLimitFromPageable(page);
            if (orderBy != null) {sqlTextBuilder.append(" ").append(orderBy);}
            if (limit != null) { sqlTextBuilder.append(" ").append(limit);}
            // Если есть пагинация, но нет сортировки - ошибка
            if (limit != null && orderBy == null) {
                // Вызовем наше исключение
                String errText = "SQL build error: " + sqlTextBuilder.toString();
                logger.error(errText);
                throw new BadPagingException(errText);
            }
        } else {
            // сортировка по умолчанию
            OrderBy orderBy = (OrderBy) tableMetadata.modelCls.getAnnotation(OrderBy.class);
            if(orderBy!=null) {
                String[] orderByParts = orderBy.value().split(",");
                final String finalAlias = alias;
                String orderByClause = Arrays.asList(orderByParts).stream()
                        .map(c->{
                            if(c.indexOf(".")>0) {
                                return c;
                            } else {
                                return finalAlias +"."+c;
                            }
                        })
                        .collect(Collectors.joining(",","ORDER BY ",""));
                sqlTextBuilder.append(" ").append(orderByClause);
            }
        }
        return sqlTextBuilder.toString();
    }

    private static String genSelectClause(TableMetadata tableMetadata, String alias) {
        String columns = tableMetadata.getColumnMetadataList().stream()
                .map(c -> alias + "." + c.getColumnName())
                .collect(Collectors.joining(","));
        // только для списков
        String joinColumns = tableMetadata.getJoinColumnMetadataList().stream()
                .filter(c->c.getMetaKind()==ColumnMetadata.ColumnMetaKind.JOINLIST)
                .map(c-> {
                    String column = c.getColumnName();
                    Cast cast = c.getField().getAnnotation(Cast.class);
                    if(cast!=null) {
                        column = String.format("cast(%s as %s)",column,cast.fromDatabase());
                    }
                    String stringAgg = DialectFactory.getDialect().stringAggFunc(column);
                    return String.format("(SELECT %s FROM %s WHERE %s.%s=%s) as %s",
                            stringAgg,c.joinTableName,
                            alias,tableMetadata.getIdFieldName(),c.getReferencedColumnName(),
                            c.getField().getName()
                    );
                })
                .collect(Collectors.joining(","));
        return columns+(!joinColumns.isEmpty()?","+joinColumns:"");
    }


    /**
     * Составляет секцию ORDER BY из Pageable
     *
     * @param pageable
     * @return
     */
    public static String buildOrderByFromPageble(Pageable pageable, String alias, String prefix) {
        if (pageable == null) {return null;}
        if (pageable.getSort().isEmpty()) {return null;}
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        pageable.getSort().iterator(),
                        Spliterator.ORDERED),
                false)
                .map(o -> (alias!=null?alias+".":"")+o.getProperty() + " " + o.getDirection().toString())
                .collect(Collectors.joining(",", prefix, ""));
    }

    public static String buildOrderByFromPageble(Pageable pageable, String prefix) {
        return buildOrderByFromPageble(pageable,null,prefix);
    }

    /**
     * Составляет секцию LIMIT OFFSET из Pageable
     *
     * @param pageable
     * @return
     */
    public static String buildLimitFromPageable(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged() || pageable.getPageSize() < 0) {
            return null;
        }
        Integer offset = null;
        if (pageable.getPageNumber() > 0) {
            offset = pageable.getPageNumber() * pageable.getPageSize();
        }
        return DialectFactory.getDialect().limitAndOffset(pageable.getPageSize(),offset);
    }


    public static String prepareSQLText(String sqlText, String mainAlias,String predicate,TableMetadata tableMetadata,Pageable page) {
        // Поиск placeholder для predicate
        int idxWhere = sqlText.indexOf(PLACEHOLDER_WHERE);
        if(predicate!=null && idxWhere<0) {
            throw new RuntimeException(String.format("Placeholder %s not found in sql text", PLACEHOLDER_WHERE));
        }
        int idxOrderBy = sqlText.indexOf(PLACEHOLDER_ORDER);
        if(page!=null && idxOrderBy<0) {
            // todo убрать эту проверку, так как я могу сортировку установить вручную, без возможности изменения
            // проверку на PLACEHOLDER_WHERE так же убрать - ее легко может не быть
            throw new RuntimeException(String.format("Placeholder %s not found in sql text", PLACEHOLDER_ORDER));
        }
        // заменяем
        sqlText = sqlText.replace(PLACEHOLDER_WHERE,predicate!=null?" and "+ predicate:"");
        if (page != null) {
            // Поправим названия полей - заменим имена на колонки
            page = OrmUtils.transformSortColumnName(page, tableMetadata);
            String orderBy = OrmUtils.buildOrderByFromPageble(page,mainAlias,"ORDER BY ");
            String limit = OrmUtils.buildLimitFromPageable(page);
            if (orderBy != null) {
                sqlText = sqlText.replace(PLACEHOLDER_ORDER,orderBy);
            }
            if (limit != null) {
                sqlText +=" "+limit;
            }
            // Если есть пагинация, но нет сортировки - ошибка
            if (limit != null && orderBy == null) {
                // Вызовем наше исключение
                String errText = "SQL build error: " + sqlText;
                throw new BadPagingException(errText);
            }
        }
        return sqlText;
    }

    public static void logSQL(String sqlText) {
        Boolean flag = (Boolean) MainApplication.getApplicationContext().getBean("getShowSQLFlag");
        if (flag) {
            logger.info("SQL:" + sqlText);
        }
    }
}
