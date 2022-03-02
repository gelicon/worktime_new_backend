package biz.gelicon.core.utils;

import biz.gelicon.core.annotations.SQLExpression;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.Column;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Описание текущей страницы запроса указывается, какую страницу надо вернуть и какой размер
 * страницы в строках и какая сортировка.
 * Нумерация страниц начинается с единицы
 */
@Schema(description = "Параметры выборки данных в таблицу")
public class GridDataOption {
    public static final int DEFAULT_PAGE_SIZE = 10; /** Рзамер страницы в строках по умолчанию */
    public static final int UNLIMIT_PAGE_SIZE = -1;

    public static final String FULLTEXT_ALIAS = "ft_";
    public static final String FULLTEXT_PARAM_NAME = "search_";

    // не меняьт регистр
    public enum FilterOperation {
        eq,
        like,
        isnull
    }

    private Pagination pagination = new Pagination();

    private String search;

    private List<OrderBy> sort = new ArrayList<>();

    @JsonIgnore
    private List<NamedFilter> namedFilters = new ArrayList<>();

    @JsonIgnore
    private List<QuickFilter> quickFilters = new ArrayList<>();

    private transient ProcessNamedFilter processNamedFilter;
    private transient Map<String, Function<QuickFilter,String>> processQuickFilter;

    public GridDataOption() {
    }

    @Schema(description = "Параметры страницы с данными")
    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    @Schema(description = "Строка полнотекстового поиска", example = "Сто")
    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    @Schema(description = "Установленные сортировки. Порядок имеет значение")
    public List<OrderBy> getSort() {
        return sort;
    }

    public void setSort(List<OrderBy> sort) {
        this.sort = sort;
    }

    @Schema(description = "Именованный фильтры", example = " \"onlyNotBlock\" : 1")
    public List<NamedFilter> getNamedFilters() {
        return namedFilters;
    }

    public void setNamedFilters(List<NamedFilter> namedFilters) {
        this.namedFilters = namedFilters;
    }

    public List<QuickFilter> getQuickFilters() {
        return quickFilters;
    }

    public void setQuickFilters(List<QuickFilter> quickFilters) {
        this.quickFilters = quickFilters;
    }

    @Schema(description = "Установленные фильтры. Ключом является имя фильтра", example = " \"filters\":{\"quick.proguserName.eq\":\"SYSDBA\"}")
    public Map<String, Object> getFilters() {
        Map<String, Object> filters = new HashMap<>();
        this.namedFilters.stream()
                .forEach(f -> filters.put(f.getName(), f.getValue()));
        this.quickFilters.stream()
                .forEach(f -> filters.put("quick." + f.getField() + "." + f.getOper().name(), f.getValue()));
        return filters;
    }

    public void setFilters(Map<String, Object> filters) {
        this.quickFilters.clear();
        this.namedFilters.clear();

        if (filters != null) {
            this.namedFilters = filters.keySet().stream()
                    .filter(k -> !k.startsWith("quick."))
                    .map(k -> new NamedFilter(k, filters.get(k)))
                    .collect(Collectors.toList());
            this.quickFilters = filters.keySet().stream()
                    .filter(k -> k.startsWith("quick."))
                    .map(k -> {
                        String[] parts = k.split("\\.");
                        return new QuickFilter(parts[1],
                                FilterOperation.valueOf(parts[2]),
                                filters.get(k));
                    })
                    .collect(Collectors.toList());
        }
    }

    public void addFilter(String name, Object value) {
        this.namedFilters.add(new NamedFilter(name,value));
    }

    public void setFilterValue(String name, Object value) {
        NamedFilter nfilter = this.namedFilters.stream()
                .filter(nf -> nf.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
        nfilter.setValue(value);
    }

    /**
     * Строит и возвращает PageRequest из pageNumber, pageSize и List<OrderBy> sort
     *
     * @return PageRequest
     */
    public PageRequest buildPageRequest() {
        List<Sort.Order> orders = new ArrayList<>();
        if (sort != null && sort.size() > 0) {
            orders = sort.stream()
                    .map(o -> new Sort.Order(o.direction, o.field))
                    .collect(Collectors.toList());
        }

        return PageRequest.of(
                pagination.getCurrent() - 1,  // особенности клиента. Номер страницы начинается с 1
                pagination.getPageSize()<0?Integer.MAX_VALUE:pagination.getPageSize(),
                Sort.by(orders));
    }

    public Map<String, Object> buildQueryParams() {
        Map<String, Object> agrs = new HashMap<>();
        getQuickFilters().stream()
                .forEach(f -> {
                    switch (f.getOper()) {
                        case eq:
                            agrs.put(f.getField(),f.getValue());
                            break;
                        case like:
                            agrs.put(f.getField(),"%"+f.getValue().toString()+"%");
                            break;
                    }
                });
        getNamedFilters().stream()
                .forEach(f -> {
                    agrs.put(f.getName(), f.getValue());
                });
        if(isFullTextSearch()) {
            agrs.put(FULLTEXT_PARAM_NAME, "%"+this.search.toLowerCase()+"%");
        }
        return agrs;
    }

    public boolean isFullTextSearch() {
        return this.search!=null && !this.search.isEmpty();
    }

    public String buildPredicate(Class<?> cls,String mainAlias) {
        return buildPredicate(cls,mainAlias,null);
    }

    public String buildPredicate(Class<?> cls,String mainAlias,Map<String,String> columnSubstitution) {
        List<String> where = new ArrayList<>();
        // полнотектовый поиск
        if(isFullTextSearch()) {
            where.add(String.format("lower(%s.fulltext) like :%s", FULLTEXT_ALIAS,FULLTEXT_PARAM_NAME));
        }
        // быстрый фильтры
        String qfilters = getQuickFilters().stream()
                .map(f -> {
                    String columnName;
                    Column column = OrmUtils.getColumn(cls, f.getField());
                    if (column == null) {
                        // поля нет, поищем @SQLCalculate в bean property
                        String expression = getSQLExpression(cls, f);
                        if(expression==null) {
                            throw new RuntimeException(String.format("Колонка с именем %s не найдена", f.getField()));
                        }
                        return expression+getOperationSQL(f.getOper(),f.getField());
                    } else {
                        columnName = column.name();
                    }

                    String subsName = null;
                    if(columnSubstitution!=null) {
                        subsName = columnSubstitution.get(columnName);
                    }
                    if(subsName==null) {
                        columnName = mainAlias + "." + columnName;
                    } else {
                        columnName = subsName;
                    }

                    if(this.processQuickFilter!=null) {
                        Function<QuickFilter, String> func = this.processQuickFilter.get(f.getField());
                        if(func!=null) {
                            return func.apply(f);
                        }
                    }

                    String s = columnName +getOperationSQL(f.getOper(),f.getField());
                    return  s;
                })
                .collect(Collectors.joining(" and "));
        if(!qfilters.isEmpty()) {
            where.add(qfilters);
        }
        // именованные фильтры
        if(this.processNamedFilter!=null) {
            String namedWhere = this.processNamedFilter.process(getNamedFilters());
            if(!namedWhere.isEmpty()) {
                where.add(namedWhere);
            }
        }
        String s = where.stream().collect(Collectors.joining(" and "));
        return s;
    }

    public static String getOperationSQL(FilterOperation oper, String paramName) {
        switch (oper) {
            case eq:return " = :"+paramName;
            case like:return " LIKE :"+paramName;
            case isnull:return " IS NULL";
            default:return null;
        }
    }

    private String getSQLExpression(Class<?> cls, QuickFilter f) {
        PropertyDescriptor propDesc = BeanUtils.getPropertyDescriptor(cls, f.getField());
        if(propDesc!=null) {
            // в методе на чтение может находится @SQLCalculate
            Method m = propDesc.getReadMethod();
            if(m!=null) {
                SQLExpression annCalculate = m.getAnnotation(SQLExpression.class);
                if(annCalculate!=null) {
                    return annCalculate.value();
                }
            }
        }
        return null;
    }

    ;

    public String buildFullTextJoin(String tableName, String aliasName) {
        return buildFullTextJoin(tableName,"ft_"+tableName,aliasName);
    }

    // формирование строки для соединения с view для быстрого поиска
    public String buildFullTextJoin(String tableName, String viewName, String aliasName) {
        if(isFullTextSearch()) {
            String idName = tableName+"_id";
            return String.format("inner join %s %s on %2$s.%3$s=%4$s.%3$s",viewName, FULLTEXT_ALIAS,idName, aliasName);
        }
        return null;
    }

    public static Pageable allPagesAndSort(List<Sort.Order> orders) {
        return PageRequest.of(1,UNLIMIT_PAGE_SIZE,Sort.by(orders));
    }

    public ProcessNamedFilter getProcessNamedFilter() {
        return processNamedFilter;
    }

    public void setProcessNamedFilter(ProcessNamedFilter processNamedFilter) {
        this.processNamedFilter = processNamedFilter;
    }

    public Map<String, Function<QuickFilter, String>> getProcessQuickFilter() {
        return processQuickFilter;
    }

    public void setProcessQuickFilter(Map<String, Function<QuickFilter, String>> processQuickFilter) {
        this.processQuickFilter = processQuickFilter;
    }

    @Override
    public String toString() {
        return "GridDataOption {pagination=" + pagination
                + ", search=" + search
                + ", filters=" + getFilters()
                + ", sort=" + this.sort.stream()
                .map(OrderBy::toString)
                .collect(Collectors.joining(",", "{", "}")) + "}";
    }

    /**
     * Вспомогательный класс для сортровки Имя поля - как в мобели, а не как в базе
     */
    @Schema(description = "Сортировка по полю")
    public static class OrderBy {
        enum InputOrder {
            ascend,
            descend
        }

        @Schema(description = "Имя поля для сортировки", example = "edizmCode")
        private String field;

        @JsonIgnore
        private Sort.Direction direction;

        public OrderBy() {
        }

        public OrderBy(String fieldName, Sort.Direction direction) {
            this.field = fieldName;
            this.direction = direction;
        }

        public Sort.Direction getDirection() {
            return direction;
        }

        public void setDirection(Sort.Direction direction) {
            this.direction = direction;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        @Schema(description = "Направление сортировки", example = "ascend")
        public String getOrder() {
            return InputOrder.values()[direction.ordinal()].name();
        }

        public void setOrder(String order) {
            direction = Sort.Direction.values()[InputOrder.valueOf(order).ordinal()];
        }

        @Override
        public String toString() {
            return "OrderBy{" +
                    "field='" + field + '\'' +
                    ", direction=" + direction +
                    '}';
        }
    }

    @Schema(description = "Параметры страницы с данными")
    static public class Pagination {
        @Schema(description = "Текущая страница, нумерация с 1", example = "1")
        private int current;
        @Schema(description = "Размер страницы. Если -1, то все записи", example = "10")
        private int pageSize;

        public Pagination() {
            this(1, DEFAULT_PAGE_SIZE);
        }

        public Pagination(int current, int pageSize) {
            this.current = current;
            this.pageSize = pageSize;
        }

        public int getCurrent() {
            return current;
        }

        public void setCurrent(int current) {
            this.current = current;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        @Override
        public String toString() {
            return "Pagination{" +
                    "current=" + current +
                    ", pageSize=" + pageSize +
                    '}';
        }
    }

    static public class QuickFilter {
        private String field;
        private FilterOperation oper;
        private Object value;

        public QuickFilter(String field, FilterOperation oper, Object value) {
            this.field = field;
            this.oper = oper;
            this.value = value;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public FilterOperation getOper() {
            return oper;
        }

        public void setOper(FilterOperation oper) {
            this.oper = oper;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "QuickFilter{" +
                    "field='" + field + '\'' +
                    ", oper=" + oper +
                    ", value=" + value +
                    '}';
        }
    }

    @Schema(description = "Именованный фильтры", example = " \"onlyNotBlock\" : 1")
    static public class NamedFilter {
        @Schema(description = "Имя именованного фильтра", example = "documentrealId")
        private String name;
        @Schema(description = "Значение для именованного фильтра", example = "324")
        private Object value;

        public NamedFilter(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "NamedFilter{" +
                    "name='" + name + '\'' +
                    ", value=" + value +
                    '}';
        }
    }

    public static class Builder {
        private Pagination pagination;
        private String search;
        private List<OrderBy> sort = new ArrayList<>();
        Map<String, Object> filters = new HashMap<>();


        public Builder pagination(int current, int pageSize) {
            pagination = new Pagination(current, pageSize);
            return this;
        }

        public Builder search(String mask) {
            this.search = mask;
            return this;
        }

        public Builder addSort(String fldName, Sort.Direction direction) {
            sort.add(new OrderBy(fldName, direction));
            return this;
        }

        public Builder addFilter(String name, Object value) {
            filters.put(name, value);
            return this;
        }

        public GridDataOption build() {
            GridDataOption opt = new GridDataOption();
            if (pagination != null) opt.setPagination(pagination);
            if (search != null) opt.setSearch(search);
            opt.setSort(sort);
            opt.setFilters(filters);
            return opt;
        }

        public <B extends GridDataOption> B build(Class<B> cls) throws ReflectiveOperationException {
            B opt = cls.getConstructor().newInstance();
            if (pagination != null) opt.setPagination(pagination);
            if (search != null) opt.setSearch(search);
            opt.setSort(sort);
            opt.setFilters(filters);
            return opt;
        }

    }
}
