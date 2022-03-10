package biz.gelicon.core.utils;

import biz.gelicon.core.dialect.DialectFactory;
import biz.gelicon.core.response.exceptions.FetchQueryException;
import biz.gelicon.core.rowmappers.RowMapForAnnotation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.persistence.Column;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Различные операции с SQL запросами
 * @param <T>
 */
public class Query<T> {

    /** Текст запроса */
    private final String sqlText;
    /** Возвращаемый объект */
    private final Class<T> objectClass;
    /** Список аргументов */
    private final Map<String, Object> innerArgs;
    private final Map<String, Function> argTransformer;

    protected Query(String sql,Class<T> cls,Map<String, Object> args, Map<String, Function> argTransformer) {
        this.sqlText = sql;
        this.objectClass  = cls;
        this.innerArgs = args;
        this.argTransformer = argTransformer;
    }

    public static <T> Query<T> union(String prefix, Query<T> q1,Query<T> q2, String sufix, Pageable page) {
        Map<String, Object> args = new HashMap<>();
        args.putAll(q1.innerArgs);
        args.putAll(q2.innerArgs);

        Query<T> orderByClause = null;
        if(page!=null) {
            orderByClause = new QueryBuilder<T>("")
                    .setPageableAndSort(page)
                    .build(q1.objectClass);
        }

        return new Query<T>(prefix+q1.sqlText+" UNION ALL "+q2.sqlText+sufix+
                (page!=null?orderByClause.sqlText:""),
                q1.objectClass, args,q1.argTransformer);
    }

    /**
     * Выполняет SQL и возвращает результат в виде списка объектов
     * @param args - аргументы для jdbcTemplate.query
     * @return Список объектов
     */
    public List<T> execute(Map<String, Object> args) {
        RowMapForAnnotation rowMapper = new RowMapForAnnotation(objectClass);
        try {
            // выполняем трансформацию параметров
            transformParams(args);

            NamedParameterJdbcTemplate jdbcTemplate = OrmUtils.getNamedParameterJdbcTemplate();
            OrmUtils.logSQL(sqlText);
            return (List<T>) jdbcTemplate.query(sqlText, args, rowMapper);
        } catch (Exception e) {
            throw new FetchQueryException("SQL execute : " + sqlText, e);
        }
    }

    /**
     * Возвращает количество записей запроса. Выполняет запрос.
     * @param args список аргументов для jdbcTemplate.queryForObject
     * @return количество записей
     */
    public int count(Map<String, Object> args) {
        String sql = DialectFactory.getDialect().countRecord(sqlText);
        try {
            // выполняем трансформацию параметров
            transformParams(args);

            NamedParameterJdbcTemplate jdbcTemplate = OrmUtils.getNamedParameterJdbcTemplate();
            OrmUtils.logSQL(sql);
            return jdbcTemplate.queryForObject(sql, args, Integer.class);
        } catch (Exception e) {
            throw new FetchQueryException("SQL execute : " + sql, e);
        }
    }

    public List<T> execute() {
        return execute(innerArgs);
    }

    public int count() {
        return count(innerArgs);
    }

    public List<T> execute(String arg0, Integer arg0Value) {
        Map<String, Object> map =  new HashMap<>();
        if(this.innerArgs!=null) {
            map.putAll(this.innerArgs);
        }
        map.put(arg0,arg0Value);
        return execute(map);
    }

    public T executeOne(String arg0, Integer arg0Value) {
        List<T> rows = execute(arg0, arg0Value);
        if(rows.isEmpty())
            throw new RuntimeException(String.format("Record for %s=%d not found",arg0,arg0Value));
        return rows.get(0);
    }

    public T executeOne(Map<String, Object> args) {
        List<T> rows = execute(args);
        if(rows.isEmpty())
            throw new RuntimeException(String.format("Record not found"));
        return rows.get(0);
    }

    private void transformParams(Map<String, Object> args) {
        Object o = argTransformer.entrySet();
        argTransformer.entrySet().forEach(entry->{

            if(args.containsKey(entry.getKey())) {
                Object argValue = args.get(entry.getKey());
                Object result = entry.getValue().apply(argValue);
                args.put(entry.getKey(),result);
            }
        });
    }

    /**
     * Построитель запросов
     * @param <T>
     */
    public static class QueryBuilder<T> {
        private String sqlText;
        private String predicate;
        private Pageable page;
        private String alias;
        private Map<String, Object> args = new HashMap<>();
        private String fromClause;
        private String prefixOrderBy;
        private String suffixOrderBy;
        private final Map<String, String> columnSubstitution = new HashMap<>();
        private final Map<String, String> injects = new HashMap<>();
        private Map<String,Function> argsTransformer = new HashMap<>();

        public QueryBuilder(String sql) {
            this.sqlText =sql;
        }
        public QueryBuilder<T> setMainAlias(String alias) {
            this.alias = alias;
            return this;
        };

        public QueryBuilder<T> setPredicate(String clause) {
            if(clause!=null && clause.isEmpty()) clause = null;
            this.predicate = clause;
            return this;
        };

        public QueryBuilder<T> injectSQL(String tag, String clause) {
            injects.put(tag,clause);
            return this;
        }
        public QueryBuilder<T> injectSQLIf(boolean cond, String tag, String clause) {
            if(cond) {
                injects.put(tag,clause);
            }
            return this;
        }


        public QueryBuilder<T> setPageableAndSort(Pageable page) {
            this.page = page;
            return this;
        };

        public QueryBuilder<T> setParams(Map<String, Object> args) {
            this.args.putAll(args);
            return this;
        }

        public QueryBuilder<T> setParam(String argName, Object argValue) {
            Map<String, Object> args = new HashMap<>();
            args.put(argName,argValue);
            return setParams(args);
        }

        /**
         * Позволяют написать алгоритм трансформации значений параметров непосредственно
         * перед отправкой в запрос
         * @param argName - имя аргумента
         * @param func - функция преобразования
         * @param <A> тип аргумента функции
         * @param <R> тип результата функции
         * @return
         */
        public <A,R> QueryBuilder<T> setParamTransformer(String argName, Function<A,R> func) {
            argsTransformer.put(argName,func);
            return this;
        }

        public QueryBuilder<T> setFrom(String fromClause) {
            this.fromClause = fromClause;
            return this;
        }

        public QueryBuilder<T> sortOrderFirst(String order) {
            this.prefixOrderBy = order;
            return this;
        }

        public QueryBuilder<T> sortOrderLast(String order) {
            this.suffixOrderBy = order;
            return this;
        }

        public QueryBuilder<T> putColumnSubstitution(String column,String substitutionColumn) {
            columnSubstitution.put(column,substitutionColumn);
            return this;
        }

        public Query<T> build(Class<T> cls) {
            String sql = sqlText.trim();
            if(fromClause!=null) {
                if(sqlText.indexOf(OrmUtils.PLACEHOLDER_FROM)<0) {
                    throw new RuntimeException(String.format("Placeholder %s not found in sql text", OrmUtils.PLACEHOLDER_FROM));
                }
            }

            if(predicate!=null) {
                if(sqlText.indexOf(OrmUtils.PLACEHOLDER_WHERE)<0) {
                    sql+=" WHERE 1=1 "+OrmUtils.PLACEHOLDER_WHERE;
                }
            }
            if(page!=null && page.getSort().isSorted()) {
                if(sqlText.indexOf(OrmUtils.PLACEHOLDER_ORDER)<0) {
                    sql+=" "+OrmUtils.PLACEHOLDER_ORDER;
                }
            }
            sql = sql.replace(OrmUtils.PLACEHOLDER_WHERE,predicate!=null?" and "+ predicate:"");
            if(fromClause!=null) {
                sql = sql.replace(OrmUtils.PLACEHOLDER_FROM,fromClause);
            }

            for (Map.Entry<String,String> entry :injects.entrySet()) {
                sql = sql.replace(entry.getKey(),entry.getValue());
            }

            if(page!=null) {
                //if (alias == null) return null;
                // сортировка
                List<String> orderlist = new ArrayList<>();
                if(this.prefixOrderBy!=null) {
                    orderlist.add(this.prefixOrderBy);
                }
                for(Sort.Order order:page.getSort()) {
                    // сначало в подстановках
                    String columnName = columnSubstitution.get(order.getProperty());
                    if(columnName==null) {
                        columnName = order.getProperty();
                    }
                    Column column = OrmUtils.getColumn(cls, columnName);
                    if(column==null) {
                        throw new RuntimeException(String.format("@Column for field %s not found", order.getProperty()));
                    }
                    String colName;
                    if(column.table().isEmpty()) {
                        colName  = (alias!=null?alias+".":"")+column.name();
                    } else {
                        // предполагается, что имя колонки будет в select
                        colName  = column.name();
                    }
                    orderlist.add(colName+" "+order.getDirection().toString());
                }
                if(this.suffixOrderBy!=null) {
                    orderlist.add(this.suffixOrderBy);
                }
                if(!orderlist.isEmpty()) {
                    sql = sql.replace(OrmUtils.PLACEHOLDER_ORDER,
                            orderlist.stream().collect(Collectors.joining(",","ORDER BY ","")));
                }
                //лимит
                if(page.getPageSize()<Integer.MAX_VALUE) {
                    sql+=" "+OrmUtils.buildLimitFromPageable(page);
                }
            }
            return new Query<T>(sql,cls,args,argsTransformer);
        }


    }
}
