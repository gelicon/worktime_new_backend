package biz.gelicon.core.controllers;

import biz.gelicon.core.annotations.Audit;
import biz.gelicon.core.annotations.CheckPermission;
import biz.gelicon.core.audit.AuditKind;
import biz.gelicon.core.config.Config;
import biz.gelicon.core.dto.EdizmDTO;
import biz.gelicon.core.model.Edizm;
import biz.gelicon.core.response.DataResponse;
import biz.gelicon.core.response.exceptions.NotFoundException;
import biz.gelicon.core.service.BaseService;
import biz.gelicon.core.service.EdizmService;
import biz.gelicon.core.utils.ConstantForControllers;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.view.EdizmView;
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
@Tag(name = "Единицы измерения", description = "Контроллер для справочника единиц измерения")
@RequestMapping(value = "/v"+ Config.CURRENT_VERSION+"/apps/refbooks/edizm",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class EdizmController {

    @Schema(description = "Параметры выборки единиц измерения в таблицу <br>"
            + "   \"NamedFilter\": {\n <br>"
            + "    \"onlyNotBlock\": 1\n <br>"
            + "  }\n")
    public static class GridDataOptionEdizm extends GridDataOption {
        @Schema(description = "Фильтры для Единиц измерения:" +
                "<ul>" +
                "<li>onlyNotBlock - показывать только не заблокированные единицы измерения" +
                "</ul>")
        @Override
        public Map<String, Object> getFilters() {
            return super.getFilters();
        }

    }

    @Autowired
    private EdizmService edizmService;

    @Operation(summary = ConstantForControllers.GETLIST_OPERATION_SUMMARY,
               description = ConstantForControllers.GETLIST_OPERATION_DESCRIPTION)
    @CheckPermission
    @RequestMapping(value = "edizm/getlist", method = RequestMethod.POST)
    public DataResponse<EdizmView> getlist(@RequestBody GridDataOptionEdizm gridDataOption) {
        // 1. быстрые фильтры уснанавливаются автоматически
        // 2. для именованных фильтров используется функция обратного вызова,
        // которая может добавить предикаты. Сами именнованные параметры в SQL устанавливаются также автоматически
        gridDataOption.setProcessNamedFilter(filters ->
                    filters.stream()
                    .map(f->{
                        switch (f.getName()) {
                            case "onlyNotBlock":
                                Integer onlyBlocksFlag = (Integer) f.getValue();
                                if(onlyBlocksFlag!=null && onlyBlocksFlag.intValue()==1) {
                                    // m0 - алиас главной таблицы
                                    return EdizmService.ALIAS_MAIN+".edizm_blockflag=0";
                                }
                                return null;
                            default:
                                return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(" and "))
        );


        List<EdizmView> result = edizmService.getMainList(gridDataOption);

        int total = 0;
        if(gridDataOption.getPagination().getPageSize()>0) {
            total = edizmService.getMainCount(gridDataOption);
        }
        return BaseService.buildResponse(result,gridDataOption,total);
    }

    @Operation(summary = ConstantForControllers.GET_OPERATION_SUMMARY,
            description = ConstantForControllers.GET_OPERATION_DESCRIPTION)
    @CheckPermission
    @RequestMapping(value = "edizm/get", method = RequestMethod.POST)
    @Audit(kinds={AuditKind.CALL_FOR_EDIT,AuditKind.CALL_FOR_ADD})
    public EdizmDTO get(@RequestBody(required = false) Integer id) {
        // для добавления
        if(id==null) {
            return new EdizmDTO();
        } else {
            Edizm entity = edizmService.findById(id);
            if(entity==null)
                throw new NotFoundException(String.format("Запись с идентификатором %s не найдена", id));
            return new EdizmDTO(entity);
        }
    }

    @Operation(summary = ConstantForControllers.SAVE_OPERATION_SUMMARY,
            description = ConstantForControllers.SAVE_OPERATION_DESCRIPTION)
    @CheckPermission
    @RequestMapping(value = "edizm/save", method = RequestMethod.POST)
    @Audit(kinds={AuditKind.CALL_FOR_SAVE_INSERT,AuditKind.CALL_FOR_SAVE_UPDATE})
    public EdizmView save(@RequestBody EdizmDTO edizmDTO) {
        Edizm entity = edizmDTO.toEntity();
        Edizm result;
        if(entity.getEdizmId()==null) {
            result = edizmService.add(entity);
        } else {
            result = edizmService.edit(entity);
        }

        // выбираем представление для одной записи
        return edizmService.getOne(result.getEdizmId());

    }

    @Operation(summary = ConstantForControllers.DELETE_OPERATION_SUMMARY,
            description = ConstantForControllers.DELETE_OPERATION_DESCRIPTION)
    @CheckPermission
    @RequestMapping(value = "edizm/delete", method = RequestMethod.POST)
    @Audit(kinds=AuditKind.CALL_FOR_DELETE)
    public String delete(@RequestBody int[] ids) {
        edizmService.deleteByIds(ids);
        return "{\"status\": \"success\"}";
    }
}