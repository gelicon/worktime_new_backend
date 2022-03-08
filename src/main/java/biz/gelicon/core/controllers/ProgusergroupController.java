package biz.gelicon.core.controllers;

import biz.gelicon.core.annotations.Audit;
import biz.gelicon.core.annotations.CheckPermission;
import biz.gelicon.core.audit.AuditKind;
import biz.gelicon.core.config.Config;
import biz.gelicon.core.jobs.JobDispatcher;
import biz.gelicon.core.model.Progusergroup;
import biz.gelicon.core.repository.CapCodeRepository;
import biz.gelicon.core.repository.ProgUserGroupRepository;
import biz.gelicon.core.response.DataResponse;
import biz.gelicon.core.response.StandardResponse;
import biz.gelicon.core.response.exceptions.NotFoundException;
import biz.gelicon.core.security.ACL;
import biz.gelicon.core.security.AuthenticationBean;
import biz.gelicon.core.security.AuthenticationCashe;
import biz.gelicon.core.security.CredentialProviderFactory;
import biz.gelicon.core.service.BaseService;
import biz.gelicon.core.service.ProgusergroupService;
import biz.gelicon.core.utils.ConstantForControllers;
import biz.gelicon.core.utils.GridDataOption;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Группы пользователей", description = "Контроллер для объектов 'Группы пользователей'")
@RequestMapping(value = "/v" + Config.CURRENT_VERSION + "/apps/admin/credential",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class ProgusergroupController {

    @Autowired
    private ProgusergroupService progusergroupService;
    @Autowired
    private CredentialProviderFactory credentialProviderFactory;
    @Autowired
    AuthenticationCashe authenticationCashe;
    @Autowired
    private ACL acl;
    @Autowired()
    @Qualifier("jobACL")
    private JobDispatcher jobACLDispather;
    @Autowired
    private CapCodeRepository capCodeRepository;
    @Autowired
    private ProgUserGroupRepository progUserGroupRepository;
    @Autowired
    private AuthenticationBean authenticationBean;

    @Operation(summary = ConstantForControllers.GETLIST_OPERATION_SUMMARY,
            description = ConstantForControllers.GETLIST_OPERATION_DESCRIPTION)
    @RequestMapping(value = "progusergroup/getlist", method = RequestMethod.POST)
    @CheckPermission
    public DataResponse<Progusergroup> getlist(@RequestBody GridDataOption gridDataOption) {
        // Фильтров нет
        List<Progusergroup> result = progusergroupService.getMainList(gridDataOption);

        int total = 0;
        if (gridDataOption.getPagination().getPageSize() > 0) {
            total = progusergroupService.getMainCount(gridDataOption);
        }
        return BaseService.buildResponse(result, gridDataOption, total);
    }

    @Operation(summary = ConstantForControllers.GET_OPERATION_SUMMARY,
            description = ConstantForControllers.GET_OPERATION_DESCRIPTION)
    @RequestMapping(value = "progusergroup/get", method = RequestMethod.POST)
    @Audit(kinds = {AuditKind.CALL_FOR_EDIT, AuditKind.CALL_FOR_ADD})
    @CheckPermission
    public Progusergroup get(@RequestBody(required = false) Integer id) {
        // для добавления
        if (id == null) {
            return new Progusergroup();
        } else {
            Progusergroup entity = progusergroupService.findById(id);
            if (entity == null) {
                throw new NotFoundException(
                        String.format("Группа пользователей с идентификатором %s не найдена", id));
            }
            return entity;
        }
    }

    @Operation(summary = ConstantForControllers.SAVE_OPERATION_SUMMARY,
            description = ConstantForControllers.SAVE_OPERATION_DESCRIPTION)
    @RequestMapping(value = "progusergroup/save", method = RequestMethod.POST)
    @Audit(kinds = {AuditKind.CALL_FOR_SAVE_UPDATE, AuditKind.CALL_FOR_SAVE_INSERT})
    @CheckPermission
    public Progusergroup save(@RequestBody Progusergroup progusergroup) {
        Progusergroup entity = progusergroup;
        Progusergroup result;
        if (entity.getProgusergroupId() == null) {
            result = progusergroupService.add(entity);
        } else {
            result = progusergroupService.edit(entity);
        }
        // выбираем представление для одной записи
        return progusergroupService.getOne(result.getProgusergroupId());

    }

    @Operation(summary = ConstantForControllers.DELETE_OPERATION_SUMMARY,
            description = ConstantForControllers.DELETE_OPERATION_DESCRIPTION)
    @RequestMapping(value = "progusergroup/delete", method = RequestMethod.POST)
    @Audit(kinds = {AuditKind.CALL_FOR_DELETE})
    @CheckPermission
    public String delete(@RequestBody int[] ids) {
        progusergroupService.deleteByIds(ids);
        return StandardResponse.SUCCESS;
    }
    
}
