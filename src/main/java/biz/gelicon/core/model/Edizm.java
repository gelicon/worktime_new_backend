package biz.gelicon.core.model;

import biz.gelicon.core.annotations.TableDescription;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Table(name = "edizm")
@TableDescription("Единица измерения")
public class Edizm {

    @Id
    @Column(name = "edizm_id", nullable = false, columnDefinition ="Идентификатор \"Единица измерения\"")
    private Integer edizmId;

    @Size(max = 50, message = "Наименование должно содержать не более {max} символов")
    @Column(name = "edizm_name", columnDefinition = "Наименование")
    private String edizmName;

    @NotBlank(message = "Краткое наименование не может быть пустым")
    @Size(max = 15, message = "Краткое наименование должно содержать не более {max} символов")
    @Column(name = "edizm_notation", nullable = false, columnDefinition = "Тип")
    private String edizmNotation;

    @NotNull(message = "Флаг блокировки не может быть пустым")
    @Max(value = 1, message = "Флаг блокировки должен быть равен 0 или 1")
    @Min(value = 0, message = "Флаг блокировки должен быть равен 0 или 1")
    @Column(name = "edizm_blockflag", nullable = false, columnDefinition = "Флаг блокировки")
    private Integer edizmBlockFlag;

    @NotBlank(message = "Код не может быть пустым")
    @Size(max = 20, message = "Код должен содержать не более {max} символов")
    @Column(name = "edizm_code", nullable = false, columnDefinition = "Код")
    private String edizmCode;

    public Edizm() {
    }

    public Edizm(Integer id,
            String name,
            String notation,
            Integer blockFlag,
            String code) {
        this.edizmId = id;
        this.edizmName = name;
        this.edizmNotation = notation;
        this.edizmBlockFlag = blockFlag;
        this.edizmCode = code;
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
