package biz.gelicon.core.controllers;

import biz.gelicon.core.annotations.Audit;
import biz.gelicon.core.annotations.CheckPermission;
import biz.gelicon.core.audit.AuditKind;
import biz.gelicon.core.config.Config;
import biz.gelicon.core.dto.AllowOrDenyApplication;
import biz.gelicon.core.model.Proguser;
import biz.gelicon.core.response.DataResponse;
import biz.gelicon.core.response.StandardResponse;
import biz.gelicon.core.security.UserDetailsImpl;
import biz.gelicon.core.service.ApplicationRoleService;
import biz.gelicon.core.service.ApplicationService;
import biz.gelicon.core.service.BaseService;
import biz.gelicon.core.service.ProguserService;
import biz.gelicon.core.utils.ConstantForControllers;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.view.ApplicationRoleView;
import biz.gelicon.core.view.ApplicationView;
import biz.gelicon.core.view.ApplicationWithAccessRoleView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@Tag(name = "Роль в модуле", description = "Контроллер для объектов \"Роль в модуле\" ")
@RequestMapping(value = "/v" + Config.CURRENT_VERSION + "/apps/admin/credential",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class ApplicationRoleController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationRoleController.class);

    @Schema(description = "Параметры выборки данных в главную таблицу")
    public static class GridDataOptionApplicationRole extends GridDataOption {

        @Schema(description = "Фильтры для \"Доступ к модулям\":" +
                "<ul>" +
                "<li> accessroleId - при установленном идентификаторе, в колонке 'Доступ', " +
                "указывается 1 для связанных с данной ролью объектов " +
                "</ul>")
        @Override
        public Map<String, Object> getFilters() {
            return super.getFilters();
        }

    }

    @Autowired
    private ApplicationRoleService applicationRoleService;

    @Operation(summary = ConstantForControllers.GETLIST_OPERATION_SUMMARY,
            description = ConstantForControllers.GETLIST_OPERATION_DESCRIPTION)
    @CheckPermission
    @RequestMapping(value = "applicationrole/getlist", method = RequestMethod.POST)
    public DataResponse<ApplicationWithAccessRoleView> getlist(
            @RequestBody GridDataOptionApplicationRole gridDataOption) {
        gridDataOption.setProcessNamedFilter(filters ->
                filters.stream()
                        .map(f -> {
                            if ("ACCESSROLEID".equals(f.getName().toUpperCase())) {
                                Integer accessRoleId = (Integer) f.getValue();
                                if (accessRoleId != null) {
                                    return ApplicationRoleService.ALIAS_MAIN
                                            + ".accessrole_id = "
                                            + accessRoleId;
                                }
                                return null;
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" and "))
        );
        //List<ApplicationRoleView> result = applicationRoleService.getMainList(gridDataOption);
        List<ApplicationWithAccessRoleView> result = applicationRoleService.getMainListForRole(gridDataOption);

        int total = 0;
        if (gridDataOption.getPagination().getPageSize() > 0) {
            //total = applicationRoleService.getMainCount(gridDataOption);
            total = applicationRoleService.getMainCountForRole(gridDataOption);
        }
        return BaseService.buildResponse(result, gridDataOption, total);
    }

    @Operation(summary = "Дать доступ на объект \"Доступ к модулям\" для роли",
            description = "Дать доступ на объект \"Доступ к модулям\", чьи идентификаторы переданы, для роли, чей идентификатор также передан.")
    @RequestMapping(value = "applicationrole/allow", method = RequestMethod.POST)
    @ResponseBody
    @Audit(kinds = {AuditKind.SECURITY_SYSTEM})
    public String allow(@RequestBody AllowOrDenyApplication allowOrDeny) {
        applicationRoleService.allow(allowOrDeny.getAccessRoleId(),
                allowOrDeny.getApplicationIds());
        return StandardResponse.SUCCESS;
    }

    @Operation(summary = "Отнять доступ на объект \"Доступ к модулям\" у роли",
            description = "Отнять доступ на объект \"Доступ к модулям\", чьи идентификаторы переданы, у роли, чей идентификатор также передан.")
    @RequestMapping(value = "applicationrole/deny", method = RequestMethod.POST)
    @ResponseBody
    @Audit(kinds = {AuditKind.SECURITY_SYSTEM})
    public String deny(@RequestBody AllowOrDenyApplication allowOrDeny) {
        applicationRoleService.deny(allowOrDeny.getAccessRoleId(), allowOrDeny.getApplicationIds());
        return StandardResponse.SUCCESS;
    }

    @Operation(summary = "Список объектов \"Модуль\", доступных текущему пользователю",
            description = "Возвращает список объектов \"Модуль\", доступных текущему пользователю. "
                    +
                    "Метод работает только для авторизированных пользователей")
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "applicationrole/accesslist", method = RequestMethod.POST)
    public DataResponse<ApplicationView> getAccessList(Authentication authentication) {
        Proguser pu = ((UserDetailsImpl) authentication.getPrincipal()).getProgUser();
        List<ApplicationView> result = applicationRoleService.getAccessList(pu.getProguserId());
        return BaseService.buildResponse(result);
    }

}
