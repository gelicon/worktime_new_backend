package biz.gelicon.core.model;

import biz.gelicon.core.annotations.TableDescription;
import org.hibernate.validator.constraints.Range;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Table(name = "constant")
@TableDescription("Константа")
public class Constant {

    public enum ConstantType {
        INTEGER,
        BOOLEAN,
        STRING,
        DATE,
        FLOAT,
        MONEY;
        public int getConstantTypeId() {
            return this.ordinal()+1;
        }
    }

    @Id
    @Column(name = "constant_id",nullable = false)
    public Integer artifactId;

    @Column(name = "constantgroup_id", nullable = false)
    @NotNull(message = "Поле \"Группа константы\" не может быть пустым")
    private Integer constantgroupId;

    @Column(name = "constant_type", nullable = false)
    @NotNull(message = "Поле \"Тип\" не может быть пустым")
    @Range(min = 1,max = 6, message ="Поле \"Тип\" должно иметь значение между {min} и {max} (включительно)")
    private Integer constantType;

    public Constant() {}

    public Constant(
            Integer artifactId,
            Integer constantgroupId,
            Integer constantType) {
        this.artifactId = artifactId;
        this.constantgroupId = constantgroupId;
        this.constantType = constantType;
    }

    public Constant(Integer artifactId) {
        this.artifactId = artifactId;
    }

    public Integer getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(Integer artifactId) {
        this.artifactId = artifactId;
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

}

