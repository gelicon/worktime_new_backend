package biz.gelicon.core.worktime.controllers.department;

import biz.gelicon.core.annotations.Audit;
import biz.gelicon.core.annotations.CheckPermission;
import biz.gelicon.core.audit.AuditKind;
import biz.gelicon.core.config.Config;
import biz.gelicon.core.response.DataResponse;
import biz.gelicon.core.response.StandardResponse;
import biz.gelicon.core.response.exceptions.NotFoundException;
import biz.gelicon.core.service.BaseService;
import biz.gelicon.core.utils.ConstantForControllers;
import biz.gelicon.core.utils.GridDataOption;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Отделы", description = "Контроллер для объектов 'Отделы'")
@RequestMapping(value = "/v" + Config.CURRENT_VERSION + "/apps/refbooks/orgstruct/department",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class DepartmentController {
    @Autowired
    DepartmentService departmentService;

    /**
     * Возвращает список отделов
     * @param gridDataOption - опциии выборки
     * @return - список
     */
    @Operation(summary = ConstantForControllers.GETLIST_OPERATION_SUMMARY,
            description = ConstantForControllers.GETLIST_OPERATION_DESCRIPTION)
    @RequestMapping(value = "getlist", method = RequestMethod.POST)
    @CheckPermission
    public DataResponse<Department> getlist(@RequestBody GridDataOption gridDataOption) {
        List<Department> result = departmentService.getMainList(gridDataOption);

        int total = 0;
        if (gridDataOption.getPagination().getPageSize() > 0) {
            total = departmentService.getMainCount(gridDataOption);
        }
        return BaseService.buildResponse(result, gridDataOption, total);
    }

    /**
     * Получение одной записи по идентификатору
     * @param id - идентификатор
     * @return - запись
     */
    @Operation(summary = ConstantForControllers.GET_OPERATION_SUMMARY,
            description = ConstantForControllers.GET_OPERATION_DESCRIPTION)
    @RequestMapping(value = "get", method = RequestMethod.POST)
    @Audit(kinds = {AuditKind.CALL_FOR_EDIT, AuditKind.CALL_FOR_ADD})
    @CheckPermission
    public Department get(@RequestBody(required = false) Integer id) {
        // для добавления
        if (id == null) {
            return new Department();
        } else {
            Department entity = departmentService.findById(id);
            if (entity == null) {
                throw new NotFoundException(
                        String.format("Отдел с идентификатором %s не найден", id));
            }
            return entity;
        }
    }

    /**
     * Сохранение записи (добавление или удаление, в зависимости от ид)
     * @param department
     * @return
     */
    @Operation(summary = ConstantForControllers.SAVE_OPERATION_SUMMARY,
            description = ConstantForControllers.SAVE_OPERATION_DESCRIPTION)
    @RequestMapping(value = "save", method = RequestMethod.POST)
    @Audit(kinds = {AuditKind.CALL_FOR_SAVE_UPDATE, AuditKind.CALL_FOR_SAVE_INSERT})
    @CheckPermission
    public Department save(@RequestBody Department department) {
        Department result;
        if (department.getDepartmentId() == null) {
            result = departmentService.add(department);
        } else {
            result = departmentService.edit(department);
        }
        // выбираем представление для одной записи
        return departmentService.getOne(result.getDepartmentId());
    }

    /**
     * Удаление отделов
     * @param ids - список удаляемых объектов через запятую
     * @return - успех или неудача
     */
    @Operation(summary = ConstantForControllers.DELETE_OPERATION_SUMMARY,
            description = ConstantForControllers.DELETE_OPERATION_DESCRIPTION)
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    @Audit(kinds = {AuditKind.CALL_FOR_DELETE})
    @CheckPermission
    public String delete(@RequestBody int[] ids) {
        departmentService.deleteByIds(ids);
        return StandardResponse.SUCCESS;
    }

}
