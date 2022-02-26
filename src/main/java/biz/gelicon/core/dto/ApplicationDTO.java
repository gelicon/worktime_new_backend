package biz.gelicon.core.dto;

import biz.gelicon.core.model.Application;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Id;
import java.util.Date;

/* Объект сгенерирован 12.05.2021 12:25 */
@Schema(description = "Объект \"Приложение\"")
public class ApplicationDTO {

    @Id
    @Schema(description = "Идентификатор \"Приложение\"")
    private Integer applicationId;

    @Schema(description = "Тип приложения")
    private Integer applicationType;

    @Schema(description = "Код приложения")
    private String applicationCode;

    @Schema(description = "Наименование приложения")
    private String applicationName;

    @Schema(description = "Имя исполняемого модуля")
    private String applicationExe;

    @Schema(description = "Описание")
    private String applicationDesc;


    public ApplicationDTO() {}

    public ApplicationDTO(
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

    public ApplicationDTO(Application entity) {
        this.applicationId = entity.getApplicationId();
        this.applicationType = entity.getApplicationType();
        this.applicationCode = entity.getApplicationCode();
        this.applicationName = entity.getApplicationName();
        this.applicationExe = entity.getApplicationExe();
        this.applicationDesc = entity.getApplicationDesc();
    }

    public Application toEntity() {
        return toEntity(new Application());
    }

    public Application toEntity(Application entity) {
        entity.setApplicationId(this.applicationId);
        entity.setApplicationType(this.applicationType);
        entity.setApplicationCode(this.applicationCode);
        entity.setApplicationName(this.applicationName);
        entity.setApplicationExe(this.applicationExe);
        entity.setApplicationDesc(this.applicationDesc);
        return entity;
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


}

