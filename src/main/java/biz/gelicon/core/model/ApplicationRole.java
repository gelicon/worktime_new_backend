package biz.gelicon.core.model;

import biz.gelicon.core.annotations.TableDescription;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Table(
    name = "applicationrole",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"accessrole_id", "application_id"})
    }
)
@TableDescription("Приложение роли")
public class ApplicationRole {

    @Id
    @Column(name = "applicationrole_id",nullable = false)
    public Integer applicationroleId;

    @ManyToOne(targetEntity = AccessRole.class)
    @Column(name = "accessrole_id", nullable = false)
    private Integer accessroleId;

    @ManyToOne(targetEntity = Application.class)
    @Column(name = "application_id", nullable = false)
    private Integer applicationId;

    public ApplicationRole() {
    }

    public ApplicationRole(Integer applicationroleId, Integer accessroleId,
            Integer applicationId) {
        this.applicationroleId = applicationroleId;
        this.accessroleId = accessroleId;
        this.applicationId = applicationId;
    }

    public void setApplicationroleId(Integer applicationroleId) {
        this.applicationroleId = applicationroleId;
    }

    public void setAccessroleId(Integer accessroleId) {
        this.accessroleId = accessroleId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getApplicationroleId() {
        return applicationroleId;
    }

    public Integer getAccessroleId() {
        return accessroleId;
    }

    public Integer getApplicationId() {
        return applicationId;
    }
}

