package biz.gelicon.core.model;

import biz.gelicon.core.annotations.TableDescription;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Table(
    name = "proguserrole",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"proguser_id", "accessrole_id"})
    }
)
@TableDescription("Роль пользователя")
public class ProguserRole {

    @Id
    @Column(name = "proguserrole_id",nullable = false)
    public Integer proguserroleId;

    @ManyToOne(targetEntity = Proguser.class)
    @Column(name = "proguser_id", nullable = false)
    private Integer proguserId;

    @ManyToOne(targetEntity = AccessRole.class)
    @Column(name = "accessrole_id", nullable = false)
    private Integer accessroleId;

    public Integer getProguserroleId() {
        return proguserroleId;
    }

    public Integer getProguserId() {
        return proguserId;
    }

    public Integer getAccessroleId() {
        return accessroleId;
    }

    public void setProguserroleId(Integer proguserroleId) {
        this.proguserroleId = proguserroleId;
    }

    public void setProguserId(Integer proguserId) {
        this.proguserId = proguserId;
    }

    public void setAccessroleId(Integer accessroleId) {
        this.accessroleId = accessroleId;
    }

    public ProguserRole() {}

    public ProguserRole(Integer proguserroleId, Integer proguserId, Integer accessroleId) {
        this.proguserroleId = proguserroleId;
        this.proguserId = proguserId;
        this.accessroleId = accessroleId;
    }
}

