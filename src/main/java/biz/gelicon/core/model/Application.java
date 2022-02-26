package biz.gelicon.core.model;

import biz.gelicon.core.annotations.TableDescription;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/* Сущность сгенерирована 12.05.2021 12:25 */
@Table(
    name = "application",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"application_exe"}),
        @UniqueConstraint(columnNames = {"application_name"})
    }
)
@TableDescription("Модуль")
public class Application {
    public static final int TYPE_EXE_APP = 0;
    public static final int TYPE_USER_APP = 1;
    public static final int TYPE_WEB_APP = 2;
    public static final int TYPE_GELICON_CORE_APP = 3;

    @Id
    @Column(name = "application_id",nullable = false)
    public Integer applicationId;

    @Column(name = "application_type", nullable = false)
    @NotNull(message = "Поле \"Тип модуля\" не может быть пустым")
    private Integer applicationType;

    @Column(name = "application_code", nullable = true)
    @Size(max = 50, message = "Поле \"Код\" должно содержать не более {max} символов")
    private String applicationCode;

    @Column(name = "application_name", nullable = false)
    @Size(max = 50, message = "Поле \"Имя\" должно содержать не более {max} символов")
    @NotBlank(message = "Поле \"Имя\" не может быть пустым")
    private String applicationName;

    @Column(name = "application_exe", nullable = false)
    @Size(max = 255, message = "Поле \"Исполняемый файл\" должно содержать не более {max} символов")
    @NotBlank(message = "Поле \"Исполняемый файл\" не может быть пустым")
    private String applicationExe;

    @Column(name = "application_desc", nullable = true)
    @Size(max = 255, message = "Поле \"Описание\" должно содержать не более {max} символов")
    private String applicationDesc;

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


    public Application() {}

    public Application(
            Integer applicationId,
            Integer applicationType,
            String applicationCode,
            String applicationName,
            String applicationExe,
            String applicationDesc) {
        this.applicationId = applicationId;
        this.applicationType = applicationType;
        this.applicationCode = applicationCode;
        this.applicationName = applicationName;
        this.applicationExe = applicationExe;
        this.applicationDesc = applicationDesc;
    }
}

