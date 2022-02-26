package biz.gelicon.core.model;

import biz.gelicon.core.annotations.TableDescription;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/* Сущность сгенерирована 25.04.2021 12:56 */
@Table(name = "accessrole")
@TableDescription("Роль")
public class AccessRole {

    @Id
    @Column(name = "accessrole_id",nullable = false)
    public Integer accessRoleId;

    @Column(name = "accessrole_name", nullable = false)
    @Size(max = 30, message = "Поле \"Имя роли\" должно содержать не более {max} символов")
    @NotBlank(message = "Поле \"Наименование роли\" не может быть пустым")
    private String accessRoleName;

    @Column(name = "accessrole_note", nullable = true)
    @Size(max = 255, message = "Поле \"Описание роли\" должно содержать не более {max} символов")
    private String accessRoleNote;

    @Column(name = "accessrole_visible", nullable = false)
    @NotNull(message = "Поле \"Видимость роли\" не может быть пустым")
    private Integer accessRoleVisible;

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


    public AccessRole() {}

    public AccessRole(
            Integer accessRoleId,
            String accessRoleName,
            String accessRoleNote,
            Integer accessRoleVisible) {
        this.accessRoleId = accessRoleId;
        this.accessRoleName = accessRoleName;
        this.accessRoleNote = accessRoleNote;
        this.accessRoleVisible = accessRoleVisible;
    }
}

