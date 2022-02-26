package biz.gelicon.core.model;

import biz.gelicon.core.annotations.TableDescription;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/* Сущность сгенерирована 30.04.2021 11:43 */
@Table(
    name = "capcodetype",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"capcodetype_code"}),
        @UniqueConstraint(columnNames = {"capcodetype_name"})
    }
)
@TableDescription("Тип кодификатора")
public class CapCodeType {
    // Типы статуса пользователя
    public final static int USER_STATUS = 13;
    // Типы аутентификации
    public final static int AUTHENTICATION_TYPE = 2148;

    @Id
    @Column(name = "capcodetype_id",nullable = false)
    public Integer capCodeTypeId;

    @Column(name = "capcodetype_code", nullable = false)
    @Size(max = 10, message = "Поле \"Код\" должно содержать не более {max} символов")
    @NotBlank(message = "Поле \"capcodetype_code\" не может быть пустым")
    private String capCodeTypeCode;

    @Column(name = "capcodetype_name", nullable = false)
    @Size(max = 50, message = "Поле \"Имя\" должно содержать не более {max} символов")
    @NotBlank(message = "Поле \"capcodetype_name\" не может быть пустым")
    private String capCodeTypeName;

    @Column(name = "capcodetype_text", nullable = true)
    private byte[] capCodeTypeText;

    public Integer getCapCodeTypeId() {
        return capCodeTypeId;
    }

    public void setCapCodeTypeId(Integer value) {
        this.capCodeTypeId = value;
    }

    public String getCapCodeTypeCode() {
        return capCodeTypeCode;
    }

    public void setCapCodeTypeCode(String value) {
        this.capCodeTypeCode = value;
    }

    public String getCapCodeTypeName() {
        return capCodeTypeName;
    }

    public void setCapCodeTypeName(String value) {
        this.capCodeTypeName = value;
    }

    public byte[] getCapCodeTypeText() {
        return capCodeTypeText;
    }

    public void setCapCodeTypeText(byte[] value) {
        this.capCodeTypeText = value;
    }


    public CapCodeType() {}

    public CapCodeType(
            Integer capCodeTypeId,
            String capCodeTypeCode,
            String capCodeTypeName,
            byte[] capCodeTypeText) {
        this.capCodeTypeId = capCodeTypeId;
        this.capCodeTypeCode = capCodeTypeCode;
        this.capCodeTypeName = capCodeTypeName;
        this.capCodeTypeText = capCodeTypeText;
    }
}

