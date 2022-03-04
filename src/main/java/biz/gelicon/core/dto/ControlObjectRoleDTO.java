package biz.gelicon.core.dto;

import biz.gelicon.core.model.ControlObjectRole;
import biz.gelicon.core.model.Edizm;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Id;

@Schema(description = "Объект 'Доступ для роли на контролируемый объект'")
public class ControlObjectRoleDTO {

    @Id
    @Schema(description = "Идентификатор 'Доступ для роли на контролируемый объект'")
    private Integer controlObjectRoleId;

    @Schema(description = "Идентификатор 'Контролируемый объект'")
    private Integer controlObjectId;

    @Schema(description = "Идентификатор 'Роль'")
    public Integer accessRoleId;

    @Schema(description = "Идентификатор 'Действие'")
    public Integer sqlactionId;

    public ControlObjectRoleDTO() {
    }

    public ControlObjectRoleDTO(Integer controlObjectRoleId, Integer controlObjectId,
            Integer accessRoleId, Integer sqlactionId) {
        this.controlObjectRoleId = controlObjectRoleId;
        this.controlObjectId = controlObjectId;
        this.accessRoleId = accessRoleId;
        this.sqlactionId = sqlactionId;
    }

    public ControlObjectRoleDTO(ControlObjectRole entity) {
        this.controlObjectRoleId = entity.getControlobjectroleId();
        this.controlObjectId = entity.getControlobjectId();
        this.accessRoleId = entity.getAccessroleId();
        this.sqlactionId = entity.getSqlactionId();
    }

    public ControlObjectRole toEntity() {
        return toEntity(new ControlObjectRole());
    }

    public ControlObjectRole toEntity(ControlObjectRole entity) {
        entity.setControlobjectroleId(this.controlObjectRoleId);
        entity.setAccessroleId(this.accessRoleId);
        entity.setControlobjectId(this.controlObjectId);
        entity.setSqlactionId(this.sqlactionId);
        return entity;
    }

    public Integer getControlObjectRoleId() {
        return controlObjectRoleId;
    }

    public Integer getControlObjectId() {
        return controlObjectId;
    }

    public Integer getAccessRoleId() {
        return accessRoleId;
    }

    public Integer getSqlactionId() {
        return sqlactionId;
    }

    public void setControlObjectRoleId(Integer controlObjectRoleId) {
        this.controlObjectRoleId = controlObjectRoleId;
    }

    public void setControlObjectId(Integer controlObjectId) {
        this.controlObjectId = controlObjectId;
    }

    public void setAccessRoleId(Integer accessRoleId) {
        this.accessRoleId = accessRoleId;
    }

    public void setSqlactionId(Integer sqlactionId) {
        this.sqlactionId = sqlactionId;
    }
}

