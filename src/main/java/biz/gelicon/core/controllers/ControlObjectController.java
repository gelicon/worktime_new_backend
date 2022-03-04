package biz.gelicon.core.controllers;

import biz.gelicon.core.annotations.Audit;
import biz.gelicon.core.audit.AuditKind;
import biz.gelicon.core.config.Config;
import biz.gelicon.core.dto.AllowOrDenyControlObject;
import biz.gelicon.core.dto.ControlObjectDTO;
import biz.gelicon.core.jobs.JobDispatcher;
import biz.gelicon.core.model.ControlObject;
import biz.gelicon.core.response.DataResponse;
import biz.gelicon.core.response.StandardResponse;
import biz.gelicon.core.response.exceptions.NotFoundException;
import biz.gelicon.core.security.ACL;
import biz.gelicon.core.service.BaseService;
import biz.gelicon.core.service.ControlObjectService;
import biz.gelicon.core.utils.ConstantForControllers;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.view.ControlObjectView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Контролируемый объект", description = "Контроллер для объектов \"Контролируемый объект\" ")
@RequestMapping(value = "/v"+ Config.CURRENT_VERSION+"/apps/admin/credential",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class ControlObjectController {

    @Schema(description = "Параметры выборки данных в таблицу")
    public static class GridDataOptionControlObject extends GridDataOption {
        @Schema(description = "Фильтры для Контролируемый объект:" +
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
    private ControlObjectService controlObjectService;
    @Autowired
    private ACL acl;

    @Autowired()
    @Qualifier("jobACL")
    private JobDispatcher jobACLDispather;

    @Operation(summary = ConstantForControllers.GETLIST_OPERATION_SUMMARY,
            description = ConstantForControllers.GETLIST_OPERATION_DESCRIPTION)
    @RequestMapping(value = "controlobject/getlist", method = RequestMethod.POST)
    public DataResponse<ControlObjectView> getlist(@RequestBody GridDataOptionControlObject gridDataOption) {
        boolean accessroleFound = gridDataOption.getNamedFilters().stream().anyMatch(nf -> "accessRoleId".equals(nf.getName()));
        if(!accessroleFound)
            throw new RuntimeException("Требуется именованный фильтр accessRoleId");
        List<ControlObjectView> result = controlObjectService.getMainList(gridDataOption);
        int total = 0;
        if(gridDataOption.getPagination().getPageSize()>0) {
            total = controlObjectService.getMainCount(gridDataOption);
        }
        return BaseService.buildResponse(result,gridDataOption,total);
    }

    @Operation(summary = ConstantForControllers.GET_OPERATION_SUMMARY,
            description = ConstantForControllers.GET_OPERATION_DESCRIPTION)
    @RequestMapping(value = "controlobject/get", method = RequestMethod.POST)
    @Audit(kinds={AuditKind.CALL_FOR_EDIT,AuditKind.CALL_FOR_ADD})
    public ControlObjectDTO get(@RequestBody(required = false) Integer id) {
        // для добавления
        if(id==null) {
            return new ControlObjectDTO();
        } else {
            ControlObject entity = controlObjectService.findById(id);
            if(entity==null)
                throw new NotFoundException(String.format("Запись с идентификатором %s не найдена", id));
            return new ControlObjectDTO(entity);
        }
    }

    @Operation(summary = ConstantForControllers.SAVE_OPERATION_SUMMARY,
            description = ConstantForControllers.SAVE_OPERATION_DESCRIPTION)
    @RequestMapping(value = "controlobject/save", method = RequestMethod.POST)
    @Audit(kinds={AuditKind.CALL_FOR_SAVE_UPDATE,AuditKind.CALL_FOR_SAVE_INSERT})
    public ControlObjectView save(@RequestBody ControlObjectDTO controlObjectDTO) {
        ControlObject entity = controlObjectDTO.toEntity();
        ControlObject result;
        if(entity.getControlObjectId() == null) {
            result = controlObjectService.add(entity);
        } else {
            result = controlObjectService.edit(entity);
        }

        // выбираем представление для одной записи
        return controlObjectService.getOne(result.getControlObjectId());

    }

    @Operation(summary = ConstantForControllers.DELETE_OPERATION_SUMMARY,
            description = ConstantForControllers.DELETE_OPERATION_DESCRIPTION)
    @RequestMapping(value = "controlobject/delete", method = RequestMethod.POST)
    @Audit(kinds={AuditKind.CALL_FOR_DELETE})
    public String delete(@RequestBody int[] ids) {
        controlObjectService.deleteByIds(ids);
        return StandardResponse.SUCCESS;
    }

    @Operation(summary = "Дать доступ на контролируемые объекты для роли",
            description = "Дать доступ на контролируемые объекты, чьи идентификаторы переданы, для роли, чей идентификатор также передан.")
    @RequestMapping(value = "controlobject/allow", method = RequestMethod.POST)
    @ResponseBody
    @Audit(kinds={AuditKind.SECURITY_SYSTEM})
    public String allow(@RequestBody AllowOrDenyControlObject allowOrDeny) {
        controlObjectService.allow(allowOrDeny.getAccessRoleId(),allowOrDeny.getControlObjectIds());
        // перестраиваем ACL асинхронно
        jobACLDispather.pushJob(()->acl.buildAccessTable());
        return StandardResponse.SUCCESS;
    }

    @Operation(summary = "Отнять доступ на контролируемые объекты у роли",
            description = "Отнять доступ на контролируемые объекты, чьи идентификаторы переданы, у роли, чей идентификатор также передан.")
    @RequestMapping(value = "controlobject/deny", method = RequestMethod.POST)
    @ResponseBody
    @Audit(kinds={AuditKind.SECURITY_SYSTEM})
    public String deny(@RequestBody AllowOrDenyControlObject allowOrDeny) {
        controlObjectService.deny(allowOrDeny.getAccessRoleId(),allowOrDeny.getControlObjectIds());
        // перестраиваем ACL асинхронно
        jobACLDispather.pushJob(()->acl.buildAccessTable());
        return StandardResponse.SUCCESS;
    }


}
