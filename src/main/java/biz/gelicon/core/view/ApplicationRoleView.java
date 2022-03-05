package biz.gelicon.core.view;

import biz.gelicon.core.annotations.SQLExpression;
import biz.gelicon.core.model.AccessRole;
import biz.gelicon.core.model.Application;
import biz.gelicon.core.model.ApplicationRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Column;
import javax.persistence.ManyToOne;

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

    public ApplicationRoleView() {}

    public ApplicationRoleView(
            Integer applicationroleId,
            Integer accessroleId,
            Integer applicationId) {
        this.applicationroleId = applicationroleId;
        this.accessroleId = accessroleId;
        this.applicationId = applicationId;
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

