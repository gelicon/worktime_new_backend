package biz.gelicon.core.view;

import biz.gelicon.core.model.CapCodeType;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.persistence.Column;

/* Объект сгенерирован 30.04.2021 11:43 */
@Schema(description = "Представление объекта \"Тип кодификатора\"")
public class CapCodeTypeView {

    @Schema(description = "Идентификатор \"Тип кодификатора\"")
    @Column(name="capcodetype_id")
    public Integer capCodeTypeId;

    @Schema(description = "Код")
    @Column(name="capcodetype_code")
    private String capCodeTypeCode;

    @Schema(description = "Наименование")
    @Column(name="capcodetype_name")
    private String capCodeTypeName;

    @Schema(description = "Текст")
    @Column(name="capcodetype_text")
    private byte[] capCodeTypeText;


    public CapCodeTypeView() {}

    public CapCodeTypeView(
            Integer capCodeTypeId,
            String capCodeTypeCode,
            String capCodeTypeName,
            byte[] capCodeTypeText) {
        this.capCodeTypeId = capCodeTypeId;
        this.capCodeTypeCode = capCodeTypeCode;
        this.capCodeTypeName = capCodeTypeName;
        this.capCodeTypeText = capCodeTypeText;
    }

    public CapCodeTypeView(CapCodeType entity) {
        this.capCodeTypeId = entity.getCapCodeTypeId();
        this.capCodeTypeCode = entity.getCapCodeTypeCode();
        this.capCodeTypeName = entity.getCapCodeTypeName();
        this.capCodeTypeText = entity.getCapCodeTypeText();
    }

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


}

