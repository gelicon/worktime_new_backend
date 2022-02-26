package biz.gelicon.core.reports;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@Schema(description = "Отчет с параметрами")
public class ReportAndParams {
    @Schema(description = "Код отчета",example = "USR-LST")
    private String code;
    @Schema(description = "Параметры отчета") //todo не смог сделать человеческий пример
    private Map<String,Object> params;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
