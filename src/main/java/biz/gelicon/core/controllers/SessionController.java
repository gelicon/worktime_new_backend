package biz.gelicon.core.controllers;

import biz.gelicon.core.annotations.Audit;
import biz.gelicon.core.audit.AuditKind;
import biz.gelicon.core.config.Config;
import biz.gelicon.core.jobs.JobDispatcher;
import biz.gelicon.core.repository.ProgUserAuthRepository;
import biz.gelicon.core.response.DataResponse;
import biz.gelicon.core.security.ACL;
import biz.gelicon.core.security.AuthenticationCashe;
import biz.gelicon.core.service.BaseService;
import biz.gelicon.core.service.SessionService;
import biz.gelicon.core.utils.ConstantForControllers;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.view.ProguserAuthView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Сессии", description = "Контроллер для сессий")
@RequestMapping(value = "/v"+ Config.CURRENT_VERSION+"/apps/admin/audit",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class SessionController {
    private static final Logger logger = LoggerFactory.getLogger(SessionController.class);

    @Autowired
    private ACL acl;
    @Autowired()
    @Qualifier("jobACL")
    private JobDispatcher jobACLDispather;

    @Schema(description = "Параметры выборки данных в таблицу")
    public static class GridDataOptionSession extends GridDataOption {
        @Schema(description = "Фильтры для объекта Сессия:" +
                "<ul>" +
                "<li>status - фильтр по статусу: 0 - все, 1 - только действительные, 2 - только закрытые" +
                "<li>proguserId - фильтр по пользователю" +
                "</ul>")
        @Override
        public Map<String, Object> getFilters() {
            return super.getFilters();
        }

    }

    @Autowired
    private ProgUserAuthRepository progUserAuthRepository;
    @Autowired
    private SessionService sessionService;


    @Operation(summary = ConstantForControllers.GETLIST_OPERATION_SUMMARY,
            description = ConstantForControllers.GETLIST_OPERATION_DESCRIPTION)
    @RequestMapping(value = "session/getlist", method = RequestMethod.POST)
    public DataResponse<ProguserAuthView> getlist(@RequestBody GridDataOptionSession gridDataOption) {
        boolean statusFound = gridDataOption.getNamedFilters().stream().anyMatch(nf -> "status".equals(nf.getName()));
        if(!statusFound) {
            gridDataOption.getNamedFilters().add(new GridDataOption.NamedFilter("status","0"));
        }
        boolean proguserIdFound = gridDataOption.getNamedFilters().stream().anyMatch(nf -> "proguserId".equals(nf.getName()));
        if(!proguserIdFound) {
            gridDataOption.getNamedFilters().add(new GridDataOption.NamedFilter("proguserId",0));
        }
        List<ProguserAuthView> result = sessionService.getMainList(gridDataOption);
        int total = 0;
        if(gridDataOption.getPagination().getPageSize()>0) {
            total = sessionService.getMainCount(gridDataOption);
        }
        return BaseService.buildResponse(result,gridDataOption,total);
    }

    @Operation(summary = "Закрытие сессий",
            description = "Закрытие сессий с текущего момента")
    @RequestMapping(value = "session/close", method = RequestMethod.POST)
    @Audit(kinds= AuditKind.CALL_FOR_DELETE)
    public String close(@RequestBody int[] ids, @RequestHeader(name="Authorization") String token) {
        sessionService.closeSessions(ids, new Date(),token.split(" ")[1].trim());
        // перестраиваем ACL асинхронно
        jobACLDispather.pushJob(()->acl.buildAccessTable());

        return "{\"status\": \"success\"}";
    }

}
