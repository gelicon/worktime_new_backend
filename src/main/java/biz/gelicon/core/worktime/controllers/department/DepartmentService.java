package biz.gelicon.core.worktime.controllers.department;

import biz.gelicon.core.annotations.Audit;
import biz.gelicon.core.annotations.CheckPermission;
import biz.gelicon.core.audit.AuditKind;
import biz.gelicon.core.response.exceptions.NotFoundException;
import biz.gelicon.core.service.BaseService;
import biz.gelicon.core.utils.ConstantForControllers;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.utils.Query;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class DepartmentService extends BaseService<Department> {
    private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);
    public static final String ALIAS_MAIN = "D";
    final String mainSQL = String.format(""
                    + " SELECT %s.*"
                    + " FROM   department %1$s "
                    + "        /*FROM_PLACEHOLDER*/ "
                    + " WHERE 1=1 /*WHERE_PLACEHOLDER*/ "
                    + " /*ORDERBY_PLACEHOLDER*/"
            ,ALIAS_MAIN);

    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private DepartmentValidator departmentValidator;

    @PostConstruct
    public void init() {
        init(departmentRepository, departmentValidator);
    }

    /**
     * Список отделов
     * @param gridDataOption - опции выборки
     * @return - список
     */
    public List<Department> getMainList(GridDataOption gridDataOption) {
        String predicate = gridDataOption.buildPredicate(Department.class, ALIAS_MAIN);
        return new Query.QueryBuilder<Department>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setPageableAndSort(gridDataOption.buildPageRequest())
                .setFrom(gridDataOption.buildFullTextJoin("department", ALIAS_MAIN))
                .setPredicate(predicate)
                .setParams(gridDataOption.buildQueryParams())
                .build(Department.class)
                .execute();
    }

    /**
     * Количество записей в выборке
     * @param gridDataOption -  опции выборки
     * @return - кол-во
     */
    public int getMainCount(GridDataOption gridDataOption) {
        return new Query.QueryBuilder<Department>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setParams(gridDataOption.buildQueryParams())
                .build(Department.class)
                .count();
    }

    /**
     * Получает одну запись по ид
     * @param id - ид
     * @return - запись
     */
    public Department getOne(Integer id) {
        return new Query.QueryBuilder<Department>(mainSQL)
                .setPredicate(ALIAS_MAIN+".department_id = :departmentId")
                .build(Department.class)
                .executeOne("departmentId", id);
    }

    /**
     * Вызывается перед удалением
     * @param id - идентификатор удаляемого
     */
    public void beforeDelete(int id) {
        if (id == 1) {
            throw new RuntimeException("Этот отдел запрещено удалять");
        }
    }

}

