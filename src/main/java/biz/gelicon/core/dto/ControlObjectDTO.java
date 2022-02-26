package biz.gelicon.core.dto;

import biz.gelicon.core.model.ControlObject;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Id;

/* Объект сгенерирован 26.04.2021 08:54 */
@Schema(description = "Объект \"Контролируемый объект\"")
public class ControlObjectDTO {

    @Id
    @Schema(description = "Идентификатор \"Контролируемый объект\"")
    private Integer controlObjectId;

    @Schema(description = "Наименование")
    private String controlObjectName;

    @Schema(description = "Идентификатор ресурса")
    private String controlObjectUrl;


    public ControlObjectDTO() {}

    public ControlObjectDTO(
            Integer controlObjectId,
            String controlObjectName,
            String controlObjectUrl) {
        this.controlObjectId = controlObjectId;
        this.controlObjectName = controlObjectName;
        this.controlObjectUrl = controlObjectUrl;
    }

    public ControlObjectDTO(ControlObject entity) {
        this.controlObjectId = entity.getControlObjectId();
        this.controlObjectName = entity.getControlObjectName();
        this.controlObjectUrl = entity.getControlObjectUrl();
    }

    public ControlObject toEntity() {
        return toEntity(new ControlObject());
    }

    public ControlObject toEntity(ControlObject entity) {
        entity.setControlObjectId(this.controlObjectId);
        entity.setControlObjectName(this.controlObjectName);
        entity.setControlObjectUrl(this.controlObjectUrl);
        return entity;
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


}

