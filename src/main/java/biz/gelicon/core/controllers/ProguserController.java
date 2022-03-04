package biz.gelicon.core.controllers;

import biz.gelicon.core.annotations.Audit;
import biz.gelicon.core.audit.AuditKind;
import biz.gelicon.core.config.Config;
import biz.gelicon.core.dto.NewProgUserPasswordDTO;
import biz.gelicon.core.dto.PasswordDTO;
import biz.gelicon.core.dto.ProguserRoleDTO;
import biz.gelicon.core.jobs.JobDispatcher;
import biz.gelicon.core.model.AccessRole;
import biz.gelicon.core.model.Proguser;
import biz.gelicon.core.repository.ProgUserRepository;
import biz.gelicon.core.response.DataResponse;
import biz.gelicon.core.response.StandardResponse;
import biz.gelicon.core.response.exceptions.IncorrectUserOrPasswordException;
import biz.gelicon.core.response.exceptions.NotFoundException;
import biz.gelicon.core.security.ACL;
import biz.gelicon.core.security.AuthenticationBean;
import biz.gelicon.core.security.AuthenticationCashe;
import biz.gelicon.core.security.CredentialProvider;
import biz.gelicon.core.security.CredentialProviderFactory;
import biz.gelicon.core.security.UserDetailsImpl;
import biz.gelicon.core.service.BaseService;
import biz.gelicon.core.service.ProguserService;
import biz.gelicon.core.utils.ConstantForControllers;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.utils.QueryUtils;
import biz.gelicon.core.view.ProguserAccessRoleView;
import biz.gelicon.core.view.ProguserView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@Tag(name = "Пользователи", description = "Контроллер для объектов \"Пользователь\" ")
@RequestMapping(value = "/v" + Config.CURRENT_VERSION + "/apps/admin/credential",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class ProguserController {

    @Schema(description = "Параметры выборки данных в таблицу")
    public static class GridDataOptionProguser extends GridDataOption {

        @Schema(description = "Фильтры для объекта Пользователь:" +
                "<ul>" +
                "<li>именованный фильтр status - фильтр по статусу: 1 - только активных, 0 - только заблокированных"
                +
                "<li>именованный фильтр type - фильтр по типу: 1 - только администраторы, 0 - только обычные"
                +
                "<li>быстрый фильтр по совпадению имени " +
                "<li>быстрый фильтр по вхождению имени " +
                "<li>\"filters\":{\"status\": 0, " +
                "<li>             \"type\": 1, " +
                "<li>              \"quick.proguserName.eq\":\"SYSDBA\"," +
                "<li>              \"quick.proguserName.like\":\"S\"}" +
                "</ul>")
        @Override
        public Map<String, Object> getFilters() {
            return super.getFilters();
        }

    }

    @Autowired
    private ProguserService proguserService;
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
    private ProgUserRepository progUserRepository;
    @Autowired
    private AuthenticationBean authenticationBean;

    @Operation(summary = ConstantForControllers.GETLIST_OPERATION_SUMMARY,
            description = ConstantForControllers.GETLIST_OPERATION_DESCRIPTION)
    @RequestMapping(value = "proguser/getlist", method = RequestMethod.POST)
    public DataResponse<ProguserView> getlist(@RequestBody GridDataOptionProguser gridDataOption) {
        // 1. быстрые фильтры уснанавливаются автоматически
        // 2. для именованных фильтров используется функция обратного вызова,
        // которая может добавить предикаты. Сами именнованные параметры в SQL устанавливаются также автоматически
        gridDataOption.setProcessNamedFilter(filters ->
                filters.stream()
                        .map(f -> {
                            switch (f.getName()) {
                                case "status":
                                    Integer status = (Integer) f.getValue();
                                    if (status != null) {
                                        return ProguserService.ALIAS_MAIN + ".proguser_status_id = "
                                                + status;
                                    }
                                    return null;
                                case "type":
                                    Integer type = (Integer) f.getValue();
                                    if (type != null) {
                                        return ProguserService.ALIAS_MAIN + ".proguser_type = "
                                                + type;
                                    }
                                    return null;
                                default:
                                    return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" and "))
        );

        List<ProguserView> result = proguserService.getMainList(gridDataOption);

        int total = 0;
        if (gridDataOption.getPagination().getPageSize() > 0) {
            total = proguserService.getMainCount(gridDataOption);
        }
        return BaseService.buildResponse(result, gridDataOption, total);
    }

    @Operation(summary = ConstantForControllers.GET_OPERATION_SUMMARY,
            description = ConstantForControllers.GET_OPERATION_DESCRIPTION)
    @RequestMapping(value = "proguser/get", method = RequestMethod.POST)
    @Audit(kinds = {AuditKind.CALL_FOR_EDIT, AuditKind.CALL_FOR_ADD})
    public Proguser get(@RequestBody(required = false) Integer id) {
        // для добавления
        if (id == null) {
            Proguser entity = new Proguser();
            return entity;
        } else {
            Proguser entity = proguserService.findById(id);
            if (entity == null) {
                throw new NotFoundException(
                        String.format("Пользователь с идентификатором %s не найден", id));
            }
            return entity;
        }
    }

    @Operation(summary = ConstantForControllers.SAVE_OPERATION_SUMMARY,
            description = ConstantForControllers.SAVE_OPERATION_DESCRIPTION)
    @RequestMapping(value = "proguser/save", method = RequestMethod.POST)
    @Audit(kinds = {AuditKind.CALL_FOR_SAVE_UPDATE, AuditKind.CALL_FOR_SAVE_INSERT})
    public ProguserView save(@RequestBody Proguser proguser) {
        if (proguser.getProguserId() == null) {
            proguser = proguserService.add(proguser);
        } else {
            // Пользователя 1 нельзя удалять
            if (proguser.getProguserId() == 1) {
                throw new RuntimeException("Этого пользователя нельзя изменять");
            }
            proguser = proguserService.edit(proguser);
        }
        // сброс кэша
        authenticationCashe.clearByUserName(proguser.getProguserName());

        // выбираем представление для одной записи
        return proguserService.getOne(proguser.getProguserId());

    }

    @Operation(summary = ConstantForControllers.DELETE_OPERATION_SUMMARY,
            description = ConstantForControllers.DELETE_OPERATION_DESCRIPTION)
    @RequestMapping(value = "proguser/delete", method = RequestMethod.POST)
    @Audit(kinds = {AuditKind.CALL_FOR_DELETE})
    public String delete(@RequestBody int[] ids) {
        // Пользователя 1 нельзя удалять
        for (int id : ids) {
            if (id == 1) {
                throw new RuntimeException("Этого пользователя нельзя удалять");
            }
        }
        // сброс кэша
        for (int i = 0; i < ids.length; i++) {
            ProguserView pu = proguserService.getOne(ids[i]);
            authenticationCashe.clearByUserName(pu.getProguserName());
        }
        proguserService.deleteByIds(ids);
        return StandardResponse.SUCCESS;
    }


    @Operation(summary = "Смена своего пароля. Необходимо знать старый пароль",
            description = "Позволяет поменять собственный пароль пользователя")
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "proguser/changepswd", method = RequestMethod.POST)
    @ResponseBody
    public String changePassword(@RequestBody
    @Parameter(description = "Новый и старый пароли") PasswordDTO dto,
            Authentication authentication) {
        Proguser pu = ((UserDetailsImpl) authentication.getPrincipal()).getProgUser();
        // сброс
        authenticationCashe.clearByUserName(pu.getProguserName());
        // получаем провайдера
        CredentialProvider<String> crprovider = credentialProviderFactory.getProvider(
                CredentialProvider.CredentialProviderType.AuthByPassword);
        if (!crprovider.checkAuthentication(pu.getProguserId(), dto.getOldPassword())) {
            throw new IncorrectUserOrPasswordException();
        }
        crprovider.updateAuthentication(pu.getProguserId(), dto.getNewPassword(), false);
        return StandardResponse.SUCCESS;
    }

    @Operation(summary = "Установка пароля у пользователя",
            description = "Позволяет установить новый пароль у пользователя, включая временный")
    @RequestMapping(value = "proguser/setpswd", method = RequestMethod.POST)
    @ResponseBody
    public String setPassword(@RequestBody
    @Parameter(description = "Новый пароль") NewProgUserPasswordDTO dto) {
        // получаем провайдера
        CredentialProvider<String> crprovider = credentialProviderFactory.getProvider(
                CredentialProvider.CredentialProviderType.AuthByPassword);
        if (!crprovider.updateAuthentication(dto.getProguserId(), dto.getNewPassword(),
                dto.getTempFlag() != 0)) {
            throw new RuntimeException("Не удалось установить пароль");
        }
        // сброс
        ProguserView pu = proguserService.getOne(dto.getProguserId());
        authenticationCashe.clearByUserName(pu.getProguserName());

        return StandardResponse.SUCCESS;
    }

    @Operation(summary = "Список пользователей по поисковой сроке",
            description = "Возвращает список пользователей в наименовании, или логине которых встречается поисковая строка")
    @RequestMapping(value = "proguser/find", method = RequestMethod.POST)
    public List<ProguserView> find(@RequestBody SimpleSearchOption options) {
        //Защита от коротких поисковых строк
        if (options.getSearch() != null && options.getSearch().length() < 4) {
            return new ArrayList<>();
        }
        GridDataOption qopt = new GridDataOption.Builder()
                .search(options.getSearch())
                .addSort("proguserName", Sort.Direction.ASC)
                .pagination(1, QueryUtils.UNLIMIT_PAGE_SIZE)
                .build();
        return proguserService.getListForFind(qopt);
    }

    public static class SimpleSearchOption {

        private String search;

        public String getSearch() {
            return search;
        }

        public void setSearch(String search) {
            this.search = search;
        }
    }

    @Operation(summary = "Список ролей пользователя для редактора ролей",
            description = "Возвращает список ролей указанного пользователя")
    @RequestMapping(value = "proguser/roles/get", method = RequestMethod.POST)
    public ProguserAccessRoleView getProguserAccessRoles(@RequestBody Integer id) {
        Proguser entity = proguserService.findById(id);
        if (entity == null) {
            throw new NotFoundException(
                    String.format("Пользователь с идентификатором %s не найден", id));
        }
        ProguserAccessRoleView parv = new ProguserAccessRoleView(entity);
        List<AccessRole> roles = proguserService.getRoleList(id);
        parv.setAccessRoleIds(roles.stream()
                .map(AccessRole::getAccessRoleId)
                .collect(Collectors.toList()));
        return parv;
    }

    @Operation(summary = "Сохранить список ролей пользователя, удалить ненужные",
            description = "Сохраняет список ролей указанного пользователя, удаляет ненужные")
    @RequestMapping(value = "proguser/roles/save", method = RequestMethod.POST)
    @Audit(kinds = {AuditKind.SECURITY_SYSTEM})
    public String accessrolesSave(@RequestBody ProguserRoleDTO proguserRoleDTO) {
        proguserService.saveRoles(proguserRoleDTO.getProguserId(),
                proguserRoleDTO.getAccessRoleIds());
        // перестраиваем ACL асинхронно
        jobACLDispather.pushJob(() -> acl.buildAccessTable());
        return StandardResponse.SUCCESS;
    }


}
