package biz.gelicon.core.view;

import biz.gelicon.core.annotations.SQLExpression;
import biz.gelicon.core.model.ControlObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Column;

@Schema(description = "Представление объекта 'Контролируемый объект'")
public class ControlObjectView {

    @Schema(description = "Идентификатор 'Контролируемый объект'")
    @Column(name="controlobject_id")
    public Integer controlObjectId;

    @Schema(description = "Наименование 'Контролируемый объект'")
    @Column(name="controlobject_name")
    private String controlObjectName;

    @Schema(description = "url 'Контролируемый объект'")
    @Column(name="controlobject_url")
    private String controlObjectUrl;

    // данное поле появляется в результате соединения с ролью
    @JsonIgnore
    @Column(name="controlobjectrole_id", table = "controlobjectrole")
    private Integer controlobjectroleId;


    public ControlObjectView() {}

    public ControlObjectView(
            Integer controlObjectId,
            String controlObjectName,
            String controlObjectUrl) {
        this.controlObjectId = controlObjectId;
        this.controlObjectName = controlObjectName;
        this.controlObjectUrl = controlObjectUrl;
    }

    public ControlObjectView(ControlObject entity) {
        this.controlObjectId = entity.getControlObjectId();
        this.controlObjectName = entity.getControlObjectName();
        this.controlObjectUrl = entity.getControlObjectUrl();
    }

    public Integer getControlObjectId() {
        return controlObjectId;
    }

    public void setControlObjectId(Integer value) {
        this.controlObjectId = value;
    }

    public String getControlObjectName() {
        return controlObjectName;
    }

    public void setControlObjectName(String value) {
        this.controlObjectName = value;
    }

    public String getControlObjectUrl() {
        return controlObjectUrl;
    }

    public void setControlObjectUrl(String value) {
        this.controlObjectUrl = value;
    }

    public Integer getControlobjectroleId() {
        return controlobjectroleId;
    }

    public void setControlobjectroleId(Integer controlobjectroleId) {
        this.controlobjectroleId = controlobjectroleId;
    }

    @Schema(description = "Доступ")
    // в случае controlobjectrole_id is null будет 0, иначе 1
    @SQLExpression("coalesce(controlobjectrole_id-controlobjectrole_id+1,0)")
    public Integer getControlObjectRoleAccessFlag() {
        return controlobjectroleId!=null?1:0;
    }

    // сеттер тоже нужен для преобразований из json-ов
    public void setControlObjectRoleAccessFlag(Integer value) {}

}

