package biz.gelicon.core.model;

import biz.gelicon.core.annotations.TableDescription;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/* Сущность сгенерирована 26.04.2021 08:54 */
@Table(name = "controlobject")
@TableDescription("Контролируемый объект")
public class ControlObject {

    @Id
    @Column(name = "controlobject_id",nullable = false)
    public Integer controlObjectId;

    @Column(name = "controlobject_name", nullable = false)
    @Size(max = 128, message = "Поле \"Наименование\" должно содержать не более {max} символов")
    @NotBlank(message = "Поле \"controlObjectName\" не может быть пустым")
    private String controlObjectName;

    @Column(name = "controlobject_url", nullable = false)
    @Size(max = 255, message = "Поле \"Идентификатор ресурса\" должно содержать не более {max} символов")
    @NotBlank(message = "Поле \"Идентификатор ресурса\" не может быть пустым")
    private String controlObjectUrl;

    public Integer getControlObjectId() {
        return controlObjectId;
    }

    public void setControlObjectId(Integer value) {
        this.controlObjectId = value;
    }

    public String getControlObjectName() {
        return controlObjectName;
    }

    public void setControlObjectName(String value) {
        this.controlObjectName = value;
    }

    public String getControlObjectUrl() {
        return controlObjectUrl;
    }

    public void setControlObjectUrl(String value) {
        this.controlObjectUrl = value;
    }


    public ControlObject() {}

    public ControlObject(
            Integer controlObjectId,
            String controlObjectName,
            String controlObjectUrl) {
        this.controlObjectId = controlObjectId;
        this.controlObjectName = controlObjectName;
        this.controlObjectUrl = controlObjectUrl;
    }
}

