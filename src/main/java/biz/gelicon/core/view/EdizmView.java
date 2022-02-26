package biz.gelicon.core.view;

import biz.gelicon.core.model.Edizm;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Column;
import javax.persistence.Id;

@Schema(description = "Представление сущности единицы измерения")
public class EdizmView {

    @Schema(description = "Идентификатор")
    @Id
    @Column(name="edizm_id")
    private Integer edizmId;

    @Schema(description = "Наименование")
    @Column(name="edizm_name")
    private String edizmName;

    @Schema(description = "Тип")
    @Column(name="edizm_notation")
    private String edizmNotation;

    @Schema(description = "Флаг блокировки")
    @Column(name="edizm_blockflag")
    private Integer edizmBlockFlag;

    @Schema(description = "Код")
    @Column(name="edizm_code")
    private String edizmCode;

    public EdizmView() {
    }

    public EdizmView(Integer edizmId,
            String edizmName,
            String edizmNotation,
            Integer edizmBlockFlag,
            String edizmCode) {
        this.edizmId = edizmId;
        this.edizmName = edizmName;
        this.edizmNotation = edizmNotation;
        this.edizmBlockFlag = edizmBlockFlag;
        this.edizmCode = edizmCode;
    }

    public EdizmView(Edizm edizm) {
        this.edizmId = edizm.getEdizmId();
        this.edizmName = edizm.getEdizmName();
        this.edizmNotation = edizm.getEdizmNotation();
        this.edizmBlockFlag = edizm.getEdizmBlockFlag();
        this.edizmCode = edizm.getEdizmCode();
    }

    public Integer getEdizmId() {
        return edizmId;
    }

    public void setEdizmId(Integer edizmId) {
        this.edizmId = edizmId;
    }

    public String getEdizmName() {
        return edizmName;
    }

    public void setEdizmName(String edizmName) {
        this.edizmName = edizmName;
    }

    public String getEdizmNotation() {
        return edizmNotation;
    }

    public void setEdizmNotation(String edizmNotation) {
        this.edizmNotation = edizmNotation;
    }

    public Integer getEdizmBlockFlag() {
        return edizmBlockFlag;
    }

    public void setEdizmBlockFlag(Integer edizmBlockFlag) {
        this.edizmBlockFlag = edizmBlockFlag;
    }

    public String getEdizmCode() {
        return edizmCode;
    }

    public void setEdizmCode(String edizmCode) {
        this.edizmCode = edizmCode;
    }
}
