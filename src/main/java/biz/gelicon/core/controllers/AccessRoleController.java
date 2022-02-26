package biz.gelicon.core.controllers;

import biz.gelicon.core.annotations.Audit;
import biz.gelicon.core.annotations.CheckAdminPermission;
import biz.gelicon.core.audit.AuditKind;
import biz.gelicon.core.config.Config;
import biz.gelicon.core.dto.AccessRoleDTO;
import biz.gelicon.core.model.AccessRole;
import biz.gelicon.core.model.Proguser;
import biz.gelicon.core.repository.ProgUserRepository;
import biz.gelicon.core.response.DataResponse;
import biz.gelicon.core.response.exceptions.NotFoundException;
import biz.gelicon.core.security.AuthenticationCashe;
import biz.gelicon.core.service.AccessRoleService;
import biz.gelicon.core.service.BaseService;
import biz.gelicon.core.utils.ConstantForControllers;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.utils.ProcessNamedFilter;
import biz.gelicon.core.view.AccessRoleView;
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
import java.util.stream.Collectors;

@RestController
@Tag(name = "Роли", description = "Контроллер для объектов \"Роль\" ")
@RequestMapping(value = "/v" + Config.CURRENT_VERSION + "/apps/admin/credential",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class AccessRoleController {

    @Schema(description = "Параметры выборки данных в таблицу")
    public static class GridDataOptionAccessRole extends GridDataOption {

        @Schema(description = "Фильтры для Роль:" +
                "<ul>" +
                "<li>onlyVisible - показывать только видимые роли" +
                "</ul>")
        @Override
        public Map<String, Object> getFilters() {
            return super.getFilters();
        }
    }

    @Autowired
    private AccessRoleService accessRoleService;
    @Autowired
    private ProgUserRepository progUserRepository;
    @Autowired
    AuthenticationCashe authenticationCashe;

    @Operation(summary = ConstantForControllers.GETLIST_OPERATION_SUMMARY,
            description = ConstantForControllers.GETLIST_OPERATION_DESCRIPTION)
    @CheckAdminPermission
    @RequestMapping(value = "accessrole/getlist", method = RequestMethod.POST)
    public DataResponse<AccessRoleView> getlist(
            @RequestBody GridDataOptionAccessRole gridDataOption) {
        // 1. быстрые фильтры уснанавливаются автоматически
        // 2. для именованных фильтров используется функция обратного вызова,
        // которая может добавить предикаты. Сами именнованные параметры в SQL устанавливаются также автоматически
        gridDataOption.setProcessNamedFilter(filters -> {
            return filters.stream()
                    .map(f -> {
                        switch (f.getName()) {
                            case "onlyVisible":
                                Integer onlyBlocksFlag = (Integer) f.getValue();
                                if (onlyBlocksFlag != null && onlyBlocksFlag.intValue() == 1) {
                                    return AccessRoleService.ALIAS_MAIN + ".accessrole_visible<>0";
                                }
                                return null;
                            default:
                                return null;
                        }
                    })
                    .filter(s -> s != null)
                    .collect(Collectors.joining(" and "));

        });
        List<AccessRoleView> result = accessRoleService.getMainList(gridDataOption);

        int total = 0;
        if (gridDataOption.getPagination().getPageSize() > 0) {
            total = accessRoleService.getMainCount(gridDataOption);
        }
        ;
        return BaseService.buildResponse(result, gridDataOption, total);
    }

    @Operation(summary = ConstantForControllers.GET_OPERATION_SUMMARY,
            description = ConstantForControllers.GET_OPERATION_DESCRIPTION)
    @CheckAdminPermission
    @RequestMapping(value = "accessrole/get", method = RequestMethod.POST)
    @Audit(kinds = {AuditKind.CALL_FOR_EDIT, AuditKind.CALL_FOR_ADD})
    public AccessRoleDTO get(@RequestBody(required = false) Integer id) {
        // для добавления
        if (id == null) {
            AccessRoleDTO dto = new AccessRoleDTO();
            dto.setAccessRoleVisible(1);
            return dto;
        } else {
            AccessRole entity = accessRoleService.findById(id);
            if (entity == null) {
                throw new NotFoundException(
                        String.format("Запись с идентификатором %s не найдена", id));
            }
            return new AccessRoleDTO(entity);
        }
    }

    @Operation(summary = ConstantForControllers.SAVE_OPERATION_SUMMARY,
            description = ConstantForControllers.SAVE_OPERATION_DESCRIPTION)
    @CheckAdminPermission
    @RequestMapping(value = "accessrole/save", method = RequestMethod.POST)
    @Audit(kinds = {AuditKind.CALL_FOR_SAVE_INSERT, AuditKind.CALL_FOR_SAVE_UPDATE})
    public AccessRoleView save(@RequestBody AccessRoleDTO accessRoleDTO) {
        AccessRole entity = accessRoleDTO.toEntity();
        AccessRole result;
        if (entity.getAccessRoleId() == null) {
            result = accessRoleService.add(entity);
        } else {
            result = accessRoleService.edit(entity);
        }
        clearAuthCasheForRole(result.getAccessRoleId());
        // выбираем представление для одной записи
        return accessRoleService.getOne(result.getAccessRoleId());

    }

    @Operation(summary = ConstantForControllers.DELETE_OPERATION_SUMMARY,
            description = ConstantForControllers.DELETE_OPERATION_DESCRIPTION)
    @CheckAdminPermission
    @RequestMapping(value = "accessrole/delete", method = RequestMethod.POST)
    @Audit(kinds = {AuditKind.CALL_FOR_DELETE})
    public String delete(@RequestBody int[] ids) {
        // сброс кэша
        for (int i = 0; i < ids.length; i++) {
            clearAuthCasheForRole(ids[i]);
        }
        accessRoleService.deleteByIds(ids);
        return "{\"status\": \"success\"}";
    }

    private void clearAuthCasheForRole(int id) {
        List<Proguser> list = progUserRepository.findByRole(id);
        list.forEach(pu -> {
            authenticationCashe.clearByUserName(pu.getProguserName());
        });
    }
}
