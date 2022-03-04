package biz.gelicon.core.view;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Column;
import javax.validation.constraints.Size;

@Schema(description = "Представление объекта 'Доступ для роли на контролируемый объект'")
public class ControlObjectRoleView {

    @Schema(description = "Идентификатор 'Доступ для роли на контролируемый объект'")
    @Column(name="controlobjectrole_id")
    public Integer controlObjectRoleId;

    @Schema(description = "Идентификатор 'Контролируемый объект'")
    @Column(name="controlobject_id")
    public Integer controlObjectId;

    @Schema(description = "Идентификатор 'Роль'")
    @Column(name="accessrole_id")
    public Integer accessRoleId;

    @Schema(description = "Идентификатор 'Действие'")
    @Column(name="sqlaction_id")
    public Integer sqlactionId;

    @Schema(description = "Наименование 'Контролируемый объект'")
    @Column(name="controlobject_name")
    private String controlObjectName;

    @Schema(description = "url 'Контролируемый объект'")
    @Column(name="controlobject_url")
    private String controlObjectUrl;

    @Schema(description = "Наименование 'Роль'")
    @Column(name="accessrole_name")
    private String accessRoleName;

    @Schema(description = "Описание 'Роль'")
    @Column(name="accessrole_note")
    private String accessRoleNote;

    @Schema(description = "Наименование 'Действие'")
    @Column(name = "sqlaction_note")
    private String sqlactionNote;

    public ControlObjectRoleView() {}

    public ControlObjectRoleView(Integer controlObjectRoleId, Integer controlObjectId,
            Integer accessRoleId, Integer sqlactionId) {
        this.controlObjectRoleId = controlObjectRoleId;
        this.controlObjectId = controlObjectId;
        this.accessRoleId = accessRoleId;
        this.sqlactionId = sqlactionId;
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

    public String getControlObjectName() {
        return controlObjectName;
    }

    public String getControlObjectUrl() {
        return controlObjectUrl;
    }

    public String getAccessRoleName() {
        return accessRoleName;
    }

    public String getAccessRoleNote() {
        return accessRoleNote;
    }

    public String getSqlactionNote() {
        return sqlactionNote;
    }

    public void setControlObjectName(String controlObjectName) {
        this.controlObjectName = controlObjectName;
    }

    public void setControlObjectUrl(String controlObjectUrl) {
        this.controlObjectUrl = controlObjectUrl;
    }

    public void setAccessRoleName(String accessRoleName) {
        this.accessRoleName = accessRoleName;
    }

    public void setAccessRoleNote(String accessRoleNote) {
        this.accessRoleNote = accessRoleNote;
    }

    public void setSqlactionNote(String sqlactionNote) {
        this.sqlactionNote = sqlactionNote;
    }
}

