package biz.gelicon.core.dto;

import biz.gelicon.core.model.AccessRole;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Id;

/* Объект сгенерирован 25.04.2021 12:56 */
@Schema(description = "Объект \"Роль\"")
public class AccessRoleDTO {

    @Id
    @Schema(description = "Идентификатор \"Роль\"")
    private Integer accessRoleId;

    @Schema(description = "Наименование")
    private String accessRoleName;

    @Schema(description = "Описание")
    private String accessRoleNote;

    @Schema(description = "Видимость")
    private Integer accessRoleVisible;


    public AccessRoleDTO() {}

    public AccessRoleDTO(
            Integer accessRoleId,
            String accessRoleName,
            String accessRoleNote,
            Integer accessRoleVisible) {
        this.accessRoleId = accessRoleId;
        this.accessRoleName = accessRoleName;
        this.accessRoleNote = accessRoleNote;
        this.accessRoleVisible = accessRoleVisible;
    }

    public AccessRoleDTO(AccessRole entity) {
        this.accessRoleId = entity.getAccessRoleId();
        this.accessRoleName = entity.getAccessRoleName();
        this.accessRoleNote = entity.getAccessRoleNote();
        this.accessRoleVisible = entity.getAccessRoleVisible();
    }

    public AccessRole toEntity() {
        return toEntity(new AccessRole());
    }

    public AccessRole toEntity(AccessRole entity) {
        entity.setAccessRoleId(this.accessRoleId);
        entity.setAccessRoleName(this.accessRoleName);
        entity.setAccessRoleNote(this.accessRoleNote);
        entity.setAccessRoleVisible(this.accessRoleVisible);
        return entity;
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

