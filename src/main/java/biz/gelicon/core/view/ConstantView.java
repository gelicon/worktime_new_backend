package biz.gelicon.core.view;

import biz.gelicon.core.model.Constant;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Column;

/* Объект сгенерирован 11.06.2021 13:39 */
@Schema(description = "Представление объекта \"Константа\"")
public class ConstantView {

    @Schema(description = "Идентификатор \"Константа\"")
    @Column(name="constant_id")
    public Integer constantid;

    @Schema(description = "Группа констант ИД")
    @Column(name="constantgroup_id")
    private Integer constantgroupId;

    @Schema(description = "Имя группы констант")
    @Column(name="capclass_name",table = "capclass")
    private String constantgroupName;

    @Schema(description = "Тип")
    @Column(name="constant_type")
    private Integer constantType;


    public ConstantView() {}

    public ConstantView(
            Integer constantid,
            Integer constantgroupId,
            Integer constantType) {
        this.constantid = constantid;
        this.constantgroupId = constantgroupId;
        this.constantType = constantType;
    }

    public ConstantView(Constant entity) {
        this.constantid = entity.getArtifactId();
        this.constantgroupId = entity.getConstantgroupId();
        this.constantType = entity.getConstantType();
    }

    public Integer getConstantid() {
        return constantid;
    }

    public void setConstantid(Integer value) {
        this.constantid = value;
    }

    public Integer getConstantgroupId() {
        return constantgroupId;
    }

    public void setConstantgroupId(Integer value) {
        this.constantgroupId = value;
    }

    public Integer getConstantType() {
        return constantType;
    }

    public void setConstantType(Integer value) {
        this.constantType = value;
    }

    public String getConstantgroupName() {
        return constantgroupName;
    }

    public void setConstantgroupName(String constantgroupName) {
        this.constantgroupName = constantgroupName;
    }
}

