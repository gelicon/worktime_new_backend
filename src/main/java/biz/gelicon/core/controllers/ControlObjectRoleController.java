package biz.gelicon.core.controllers;

import biz.gelicon.core.annotations.Audit;
import biz.gelicon.core.annotations.CheckPermission;
import biz.gelicon.core.audit.AuditKind;
import biz.gelicon.core.config.Config;
import biz.gelicon.core.dto.ControlObjectRoleDTO;
import biz.gelicon.core.model.ControlObjectRole;
import biz.gelicon.core.model.Edizm;
import biz.gelicon.core.response.DataResponse;
import biz.gelicon.core.response.exceptions.NotFoundException;
import biz.gelicon.core.service.BaseService;
import biz.gelicon.core.service.ControlObjectRoleService;
import biz.gelicon.core.utils.ConstantForControllers;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.view.ControlObjectRoleView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@Tag(name = "Доступ для роли на объекты", description = "Контроллер для доступа для роли на объекты")
@RequestMapping(value = "/v" + Config.CURRENT_VERSION + "/apps/admin/credential",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class ControlObjectRoleController {

    @Schema(description = "Параметры выборки единиц измерения в таблицу")
    public static class GridDataOptionControlObjectRole extends GridDataOption {

        @Schema(description = "Фильтры для Доступ для роли на объекты:" +
                "<ul>" +
                "<li>sqlaction - по sqlaction, например для EXECUTE:" +
                "<li>,\"filters\": {\"sqlaction\": 9}" +
                "<li>accessrole - по accessrole, например для 1:" +
                "<li>,\"filters\": {\"accessrole\": 1}" +
                "</ul>")
        @Override
        public Map<String, Object> getFilters() {
            return super.getFilters();
        }

    }

    @Autowired
    private ControlObjectRoleService controlObjectRoleService;

    @Operation(summary = ConstantForControllers.GETLIST_OPERATION_SUMMARY,
            description = ConstantForControllers.GETLIST_OPERATION_DESCRIPTION)
    @CheckPermission
    @RequestMapping(value = "controlobjectrole/getlist", method = RequestMethod.POST)
    public DataResponse<ControlObjectRoleView> getlist(
            @RequestBody GridDataOptionControlObjectRole gridDataOption) {
        // 1. быстрые фильтры уснанавливаются автоматически
        // 2. для именованных фильтров используется функция обратного вызова,
        // которая может добавить предикаты. Сами именнованные параметры в SQL устанавливаются также автоматически
        gridDataOption.setProcessNamedFilter(filters ->
                filters.stream()
                        .map(f -> {
                            switch (f.getName()) {
                                case "sqlaction":
                                    Integer sqlactionId = (Integer) f.getValue();
                                    if (sqlactionId != null) {
                                        return ControlObjectRoleService.ALIAS_MAIN
                                                + ".sqlaction_id = " + sqlactionId;
                                    }
                                    return null;
                                case "accessrole":
                                    Integer accessroleId = (Integer) f.getValue();
                                    if (accessroleId != null) {
                                        return ControlObjectRoleService.ALIAS_MAIN
                                                + ".accessrole_id = " + accessroleId;
                                    }
                                    return null;
                                default:
                                    return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" and "))
        );

        List<ControlObjectRoleView> result = controlObjectRoleService.getMainList(gridDataOption);

        int total = 0;
        if (gridDataOption.getPagination().getPageSize() > 0) {
            total = controlObjectRoleService.getMainCount(gridDataOption);
        }
        return BaseService.buildResponse(result, gridDataOption, total);
    }

    @Operation(summary = ConstantForControllers.GET_OPERATION_SUMMARY,
            description = ConstantForControllers.GET_OPERATION_DESCRIPTION)
    @CheckPermission
    @RequestMapping(value = "controlobjectrole/get", method = RequestMethod.POST)
    @Audit(kinds = {AuditKind.CALL_FOR_EDIT, AuditKind.CALL_FOR_ADD})
    public ControlObjectRoleDTO get(@RequestBody(required = false) Integer id) {
        // для добавления
        if (id == null) {
            return new ControlObjectRoleDTO();
        } else {
            ControlObjectRole entity = controlObjectRoleService.findById(id);
            if (entity == null) {
                throw new NotFoundException(
                        String.format("Запись с идентификатором %s не найдена", id));
            }
            return new ControlObjectRoleDTO(entity);
        }
    }

    @Operation(summary = ConstantForControllers.SAVE_OPERATION_SUMMARY,
            description = ConstantForControllers.SAVE_OPERATION_DESCRIPTION)
    @CheckPermission
    @RequestMapping(value = "controlobjectrole/save", method = RequestMethod.POST)
    @Audit(kinds = {AuditKind.CALL_FOR_SAVE_INSERT, AuditKind.CALL_FOR_SAVE_UPDATE})
    public ControlObjectRoleView save(@RequestBody ControlObjectRoleDTO controlObjectRoleDTO) {
        ControlObjectRole entity = controlObjectRoleDTO.toEntity();
        ControlObjectRole result;
        if (entity.getControlobjectroleId() == null) {
            result = controlObjectRoleService.add(entity);
        } else {
            result = controlObjectRoleService.edit(entity);
        }

        // выбираем представление для одной записи
        return controlObjectRoleService.getOne(result.getControlobjectroleId());

    }

    @Operation(summary = ConstantForControllers.DELETE_OPERATION_SUMMARY,
            description = ConstantForControllers.DELETE_OPERATION_DESCRIPTION)
    @CheckPermission
    @RequestMapping(value = "controlobjectrole/delete", method = RequestMethod.POST)
    @Audit(kinds = AuditKind.CALL_FOR_DELETE)
    public String delete(@RequestBody int[] ids) {
        controlObjectRoleService.deleteByIds(ids);
        return "{\"status\": \"success\"}";
    }
}