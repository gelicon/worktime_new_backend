package biz.gelicon.core.dto;

import biz.gelicon.core.model.Edizm;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Id;

@Schema(description = "Сущность единицы измерения")
public class EdizmDTO {

    @Id
    @Schema(description = "Идентификатор")
    private Integer edizmId;

    @Schema(description = "Наименование")
    private String edizmName;

    @Schema(description = "Тип")
    private String edizmNotation;

    @Schema(description = "Флаг блокировки")
    private Integer edizmBlockFlag = 0;

    @Schema(description = "Код")
    private String edizmCode;

    public EdizmDTO() {
    }

    public EdizmDTO(Integer edizmId,
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

    public EdizmDTO(Edizm entity) {
        this.edizmId = entity.getEdizmId();
        this.edizmName = entity.getEdizmName();
        this.edizmNotation = entity.getEdizmNotation();
        this.edizmBlockFlag = entity.getEdizmBlockFlag();
        this.edizmCode = entity.getEdizmCode();
    }

    public Edizm toEntity() {
        return toEntity(new Edizm());
    }

    public Edizm toEntity(Edizm entity) {
        entity.setEdizmId(this.edizmId);
        entity.setEdizmName(this.edizmName);
        entity.setEdizmNotation(this.edizmNotation);
        entity.setEdizmBlockFlag(this.edizmBlockFlag);
        entity.setEdizmCode(this.edizmCode);

        return entity;
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
