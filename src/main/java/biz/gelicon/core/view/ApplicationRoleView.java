package biz.gelicon.core.view;

import biz.gelicon.core.annotations.SQLExpression;
import biz.gelicon.core.model.Application;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Column;

/* Объект сгенерирован 12.05.2021 12:25 */
@Schema(description = "Представление объекта \"Приложение\"")
public class ApplicationRoleView {

    @Schema(description = "Идентификатор \"Приложение\"")
    @Column(name="application_id")
    public Integer applicationId;

    @Schema(description = "Тип приложения")
    @Column(name="application_type")
    private Integer applicationType;

    @Schema(description = "Код приложения")
    @Column(name="application_code")
    private String applicationCode;

    @Schema(description = "Наименование приложения")
    @Column(name="application_name")
    private String applicationName;

    @Schema(description = "Имя исполняемого модуля")
    @Column(name="application_exe")
    private String applicationExe;

    @Schema(description = "Описание")
    @Column(name="application_desc")
    private String applicationDesc;

    // данное поле появляется в результате соединения с ролью
    @JsonIgnore
    @Column(name="applicationrole_id", table = "applicationrole")
    private Integer applicationroleId;

    public ApplicationRoleView() {}

    public ApplicationRoleView(
            Integer applicationId,
            Integer applicationType,
            String applicationCode,
            String applicationName,
            String applicationExe,
            byte[] applicationBlob,
            String applicationDesc) {
        this.applicationId = applicationId;
        this.applicationType = applicationType;
        this.applicationCode = applicationCode;
        this.applicationName = applicationName;
        this.applicationExe = applicationExe;
        this.applicationDesc = applicationDesc;
    }

    public ApplicationRoleView(Application entity) {
        this.applicationId = entity.getApplicationId();
        this.applicationType = entity.getApplicationType();
        this.applicationCode = entity.getApplicationCode();
        this.applicationName = entity.getApplicationName();
        this.applicationExe = entity.getApplicationExe();
        this.applicationDesc = entity.getApplicationDesc();
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer value) {
        this.applicationId = value;
    }

    public Integer getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(Integer value) {
        this.applicationType = value;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public void setApplicationCode(String value) {
        this.applicationCode = value;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String value) {
        this.applicationName = value;
    }

    public String getApplicationExe() {
        return applicationExe;
    }

    public void setApplicationExe(String value) {
        this.applicationExe = value;
    }


    public String getApplicationDesc() {
        return applicationDesc;
    }

    public void setApplicationDesc(String value) {
        this.applicationDesc = value;
    }

    public Integer getApplicationroleId() {
        return applicationroleId;
    }

    public void setApplicationroleId(Integer applicationroleId) {
        this.applicationroleId = applicationroleId;
    }

    @Schema(description = "Доступ")
    // в случае applicationrole_id is null будет 0, иначе 1
    @SQLExpression("coalesce(applicationrole_id-applicationrole_id+1,0)")
    public Integer getApplicationRoleAccessFlag() {
        return applicationroleId!=null?1:0;
    }

}

