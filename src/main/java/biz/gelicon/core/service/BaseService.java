package biz.gelicon.core.service;

import biz.gelicon.core.repository.TableRepository;
import biz.gelicon.core.response.DataResponse;
import biz.gelicon.core.response.exceptions.DeleteRecordException;
import biz.gelicon.core.response.exceptions.PostRecordException;
import biz.gelicon.core.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BaseService<T> {
    private static final Logger logger = LoggerFactory.getLogger(BaseService.class);

    private Validator validator;
    private TableRepository<T> repository;
    private String idName;

    protected void init(TableRepository<T> repository, Validator validator) {
        this.validator = validator;
        this.repository = repository;
        Class<T> cls = repository.getTableModelClass();
        idName = OrmUtils.getIdField(cls).getName();
    }


    public List<T> findWhere(GridDataOption gridDataOption, ProcessNamedFilter ncb){
        TwoTuple<String, Map<String, Object>> where = OrmUtils.buildWhereFromGridDataOption(gridDataOption, repository.getTableMetaData());
        boolean namedFilterFound = ncb!=null && !gridDataOption.getNamedFilters().isEmpty();
        String flt = "";
        if(namedFilterFound) {
            flt = ncb.process(gridDataOption.getNamedFilters());
        }
        String predicate = where.a.isEmpty()?null:where.a+(!flt.isEmpty()?" and "+flt:"");
        Map<String, Object> argsMap = where.b;
        return repository.findWhere(predicate,argsMap,gridDataOption.buildPageRequest());
    }

    public int countWhere(GridDataOption gridDataOption, ProcessNamedFilter ncb){
        TwoTuple<String, Map<String, Object>> where = OrmUtils.buildWhereFromGridDataOption(gridDataOption, repository.getTableMetaData());
        boolean namedFilterFound = ncb!=null && !gridDataOption.getNamedFilters().isEmpty();
        String flt = "";
        if(namedFilterFound) {
            flt = ncb.process(gridDataOption.getNamedFilters());
        }
        String predicate = where.a.isEmpty()?null:where.a+(!flt.isEmpty()?" and "+flt:"");
        Map<String, Object> argsMap = where.b;
        return repository.countWhere(predicate,argsMap);
    }

    public List<T> findQuery(String sql, String mainAlias,GridDataOption gridDataOption, ProcessNamedFilter ncb){
        TwoTuple<String, Map<String, Object>> where = OrmUtils.buildWhereFromGridDataOption(gridDataOption, repository.getTableMetaData());
        boolean namedFilterFound = ncb!=null && !gridDataOption.getNamedFilters().isEmpty();
        String predicate = where.a.isEmpty()?null:where.a+(namedFilterFound?" and "+ncb.process(gridDataOption.getNamedFilters()):"");
        Map<String, Object> argsMap = where.b;
        return repository.findQuery(sql,mainAlias,predicate,argsMap,gridDataOption.buildPageRequest());
    };

    public int countQuery(String sql, String mainAlias,GridDataOption gridDataOption, ProcessNamedFilter ncb) {
        TwoTuple<String, Map<String, Object>> where = OrmUtils.buildWhereFromGridDataOption(gridDataOption, repository.getTableMetaData());
        boolean namedFilterFound = ncb!=null && !gridDataOption.getNamedFilters().isEmpty();
        String predicate = where.a.isEmpty()?null:where.a+(namedFilterFound?" and "+ncb.process(gridDataOption.getNamedFilters()):"");
        Map<String, Object> argsMap = where.b;
        return repository.countQuery(sql, mainAlias, predicate,argsMap);
    }

    protected DataBinder validate(T entity){
        DataBinder dataBinder = new DataBinder(entity);
        dataBinder.addValidators(validator);
        dataBinder.validate();
        if (dataBinder.getBindingResult().hasErrors()) {
            logger.error(dataBinder.getBindingResult().getAllErrors().toString());
            throw new PostRecordException(dataBinder.getBindingResult(), entity.getClass(),new Throwable("Ошибка ввода"));
        }

        return dataBinder;
    }

    public T findById(Integer id){
        return repository.findById(id);
    }

    @Transactional
    public T insertOrUpdate(T entity){
        DataBinder dataBinder = validate(entity);
        try {
            beforeSave(entity);
            Integer id = repository.getIdFromEntity(entity);
            repository.insertOrUpdate(entity);
            afterSave(entity,id==null);
            return entity;
        } catch (RuntimeException e) {
            throw new PostRecordException(dataBinder.getBindingResult(),entity.getClass(), e);
        }
    }


    @Transactional
    public void deleteByIds(int[] ids){
        for (int id : ids) {
            try {
                beforeDelete(id);
                repository.delete(id);
                afterDelete(id);
            } catch (RuntimeException ex) {
                throw new DeleteRecordException(ex.getMessage(), ex);
            }
        }
    }

    /**
     * Метод, вызываемый перед удалением
     * @param id
     */
    protected void beforeDelete(int id) {
        //empty
    }

    /**
     * Метод, вызываемый после удаления
     * @param id
     */
    protected void afterDelete(int id) {
        //empty
    }

    @Transactional
    public T add(T entity){
        beforeValidate(entity);
        DataBinder dataBinder = validate(entity);
        try {
            beforeSave(entity);
            int ret = repository.insert(entity);
            afterSave(entity,true);
            return entity;
        } catch (RuntimeException e) {
            throw new PostRecordException(dataBinder.getBindingResult(),entity.getClass(), e);
        }
    }

    /**
     * Метод, вызываемый перед сохранением. Создан для перекрывания
     * Стандартный валидатор выполняется перед этим методом
     * @param entity
     */
    protected void beforeSave(T entity) {
        //empty
    }

    protected void afterSave(T entity, boolean modeInsert) {
        //empty
    }

    protected void beforeValidate(T entity) {
        //empty
    }

    @Transactional
    public T edit(T entity){
        beforeValidate(entity);
        DataBinder dataBinder = validate(entity);
        try {
            beforeSave(entity);
            int count= repository.update(entity);
            if(count==0) {
                throw new RuntimeException(String.format("Запись с id=%s не найдена ", repository.getIdFromEntity(entity)));
            }
            afterSave(entity,false);
            return entity;
        } catch (RuntimeException e) {
            throw new PostRecordException(dataBinder.getBindingResult(),entity.getClass(), e);
        }
    }

    /**
     * Cлияние передаваемой сущности с оригиналом из БД
     * Поле NULL игнорируются, елм не включены в unconditionalFieldNames
     *
     * @param entity
     * @param unconditionalFieldNames - обязательные поля для копирования
     * @return
     */
    public T merge(T entity, List<String> unconditionalFieldNames) {
        Integer id = repository.getIdFromEntity(entity);
        if(id==null) {
            throw new RuntimeException(String.format("Идентификатор объекта не может быть null"));
        }
        T orig = repository.findById(id);
        if(orig==null) {
            throw new RuntimeException(String.format("Запись с id=%s не может быть изменена, так как ее оригинал отсутствует в базе данных", id));
        }
        // копируем только не null поля
        String[] ignores = ReflectUtils.getNullPropertyNames(entity);
        // но есть обязательные поля для копирования
        if(unconditionalFieldNames!=null) {
            List<String> ignoreList = Arrays.asList(ignores);
            ignoreList.removeAll(unconditionalFieldNames);
            ignores = ignoreList.toArray(new String[]{});
        }
        BeanUtils.copyProperties(entity, orig,ignores);
        return orig;
    }
    public T merge(T entity) {
        return merge(entity,null);
    }
    public T mergeWithIgnoreList(T entity, List<String> ignoreFieldNames) {
        Integer id = repository.getIdFromEntity(entity);
        if(id==null) {
            throw new RuntimeException(String.format("Идентификатор объекта не может быть null"));
        }
        T orig = repository.findById(id);
        if(orig==null) {
            throw new RuntimeException(String.format("Запись с id=%s не может быть изменена, так как ее оригинал отсутствует в базе данных", id));
        }
        String[] ignores = ignoreFieldNames.toArray(new String[]{});
        BeanUtils.copyProperties(entity, orig,ignores);
        return orig;
    }

    public static <V> DataResponse<V> buildResponse(List<V> currPage, GridDataOption gridDataOption, int total) {
        DataResponse<V> dataResponse = new DataResponse<>();
        dataResponse.setResult(currPage);
        dataResponse.setCurrPage(gridDataOption.getPagination().getCurrent());
        // постраничный режим
        if(gridDataOption.getPagination().getPageSize()>0) {
            dataResponse.setAllRowCount(total);
            float allPage = dataResponse.getAllRowCount() / (float)gridDataOption.getPagination().getPageSize();
            dataResponse.setAllPage(allPage > (int) allPage ? (int) allPage + 1 : (int) allPage);
        } else {
            // все записи сразу
            dataResponse.setAllRowCount(currPage.size());
            dataResponse.setAllPage(1);
        }
        return dataResponse;
    }

    public static <V> DataResponse<V> buildResponse(List<V> list) {
        DataResponse<V> dataResponse = new DataResponse<>();
        dataResponse.setResult(list);
        dataResponse.setCurrPage(1);
        // все записи сразу
        dataResponse.setAllRowCount(list.size());
        dataResponse.setAllPage(1);
        return dataResponse;
    }

}
