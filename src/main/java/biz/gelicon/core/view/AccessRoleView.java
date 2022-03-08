package biz.gelicon.core.view;

import biz.gelicon.core.model.AccessRole;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.persistence.Column;

@Schema(description = "Представление объекта 'Роль'")
public class AccessRoleView {

    @Schema(description = "Идентификатор 'Роль'")
    @Column(name="accessrole_id")
    public Integer accessRoleId;

    @Schema(description = "Наименование 'Роль'")
    @Column(name="accessrole_name")
    private String accessRoleName;

    @Schema(description = "Описание 'Роль'")
    @Column(name="accessrole_note")
    private String accessRoleNote;

    @Schema(description = "Видимость 'Роль'")
    @Column(name="accessrole_visible")
    private Integer accessRoleVisible;

    public AccessRoleView() {}

    public AccessRoleView(Integer accessRoleId, String accessRoleName, String accessRoleNote,
            Integer accessRoleVisible, String applicationName) {
        this.accessRoleId = accessRoleId;
        this.accessRoleName = accessRoleName;
        this.accessRoleNote = accessRoleNote;
        this.accessRoleVisible = accessRoleVisible;
    }

    public AccessRoleView(AccessRole entity) {
        this.accessRoleId = entity.getAccessRoleId();
        this.accessRoleName = entity.getAccessRoleName();
        this.accessRoleNote = entity.getAccessRoleNote();
        this.accessRoleVisible = entity.getAccessRoleVisible();
    }

    public Integer getAccessRoleId() {
        return accessRoleId;
    }

    public void setAccessRoleId(Integer value) {
        this.accessRoleId = value;
    }

    public String getAccessRoleName() {
        return accessRoleName;
    }

    public void setAccessRoleName(String value) {
        this.accessRoleName = value;
    }

    public String getAccessRoleNote() {
        return accessRoleNote;
    }

    public void setAccessRoleNote(String value) {
        this.accessRoleNote = value;
    }

    public Integer getAccessRoleVisible() {
        return accessRoleVisible;
    }

    public void setAccessRoleVisible(Integer value) {
        this.accessRoleVisible = value;
    }


}

