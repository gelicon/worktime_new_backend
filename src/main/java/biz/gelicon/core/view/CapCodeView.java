package biz.gelicon.core.view;

import biz.gelicon.core.model.CapCode;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Column;

/* Объект сгенерирован 30.04.2021 11:08 */
@Schema(description = "Представление объекта \"Кодификатор\"")
public class CapCodeView {

    @Schema(description = "Идентификатор \"Кодификатор\"")
    @Column(name="capcode_id")
    public Integer capCodeId;

    @Schema(description = "Тип кодификатора ИД")
    @Column(name="capcodetype_id")
    private Integer capCodeTypeId;

    @Schema(description = "Код")
    @Column(name="capcode_code")
    private String capCodeCode;

    @Schema(description = "Наименование")
    @Column(name="capcode_name")
    private String capCodeName;

    @Schema(description = "Код сортировки")
    @Column(name="capcode_sortcode")
    private String capCodeSortcode;

    @Schema(description = "Текст")
    @Column(name="capcode_text")
    private byte[] capCodeText;

    public CapCodeView() {}

    public CapCodeView(
            Integer capCodeId,
            Integer capcodetypeId,
            String capCodeCode,
            String capCodeName,
            String capCodeSortcode,
            byte[] capCodeText) {
        this.capCodeId = capCodeId;
        this.capCodeTypeId = capcodetypeId;
        this.capCodeCode = capCodeCode;
        this.capCodeName = capCodeName;
        this.capCodeSortcode = capCodeSortcode;
        this.capCodeText = capCodeText;
    }

    public CapCodeView(CapCode entity) {
        this.capCodeId = entity.getCapCodeId();
        this.capCodeTypeId = entity.getCapcodetypeId();
        this.capCodeCode = entity.getCapCodeCode();
        this.capCodeName = entity.getCapCodeName();
        this.capCodeSortcode = entity.getCapCodeSortcode();
        this.capCodeText = entity.getCapCodeText();
    }

    public Integer getCapCodeId() {
        return capCodeId;
    }

    public void setCapCodeId(Integer value) {
        this.capCodeId = value;
    }

    public Integer getCapCodeTypeId() {
        return capCodeTypeId;
    }

    public void setCapCodeTypeId(Integer value) {
        this.capCodeTypeId = value;
    }

    public String getCapCodeCode() {
        return capCodeCode;
    }

    public void setCapCodeCode(String value) {
        this.capCodeCode = value;
    }

    public String getCapCodeName() {
        return capCodeName;
    }

    public void setCapCodeName(String value) {
        this.capCodeName = value;
    }

    public String getCapCodeSortcode() {
        return capCodeSortcode;
    }

    public void setCapCodeSortcode(String value) {
        this.capCodeSortcode = value;
    }

    public byte[] getCapCodeText() {
        return capCodeText;
    }

    public void setCapCodeText(byte[] value) {
        this.capCodeText = value;
    }


}

