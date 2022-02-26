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
}

