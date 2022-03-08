package biz.gelicon.core.view;

import biz.gelicon.core.model.ApplicationRole;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Column;

@Schema(description = "Представление объекта 'Приложение для роли'")
public class ApplicationRoleView {

    @Schema(description = "Идентификатор 'Приложение для роли'")
    @Column(name = "applicationrole_id")
    private Integer applicationroleId;

    @Schema(description = "Идентификатор Роль")
    @Column(name = "accessrole_id")
    private Integer accessroleId;

    @Schema(description = "Идентификатор \"Приложение\"")
    @Column(name="application_id")
    public Integer applicationId;

    @Schema(description = "Код приложения")
    @Column(name="application_code")
    private String applicationCode;

    @Schema(description = "Наименование приложения")
    @Column(name="application_name")
    private String applicationName;

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public ApplicationRoleView() {}

    public String getApplicationCode() {
        return applicationCode;
    }

    public void setApplicationCode(String applicationCode) {
        this.applicationCode = applicationCode;
    }

    public ApplicationRoleView(Integer applicationroleId, Integer accessroleId,
            Integer applicationId, String applicationCode, String applicationName) {
        this.applicationroleId = applicationroleId;
        this.accessroleId = accessroleId;
        this.applicationId = applicationId;
        this.applicationCode = applicationCode;
        this.applicationName = applicationName;
    }

    public ApplicationRoleView(ApplicationRole entity) {
        this.applicationroleId = entity.getApplicationroleId();
        this.accessroleId = entity.getAccessroleId();
        this.applicationId = entity.getApplicationId();
    }

    public void setApplicationroleId(Integer applicationroleId) {
        this.applicationroleId = applicationroleId;
    }

    public void setAccessroleId(Integer accessroleId) {
        this.accessroleId = accessroleId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getApplicationroleId() {
        return applicationroleId;
    }

    public Integer getAccessroleId() {
        return accessroleId;
    }

    public Integer getApplicationId() {
        return applicationId;
    }

}

