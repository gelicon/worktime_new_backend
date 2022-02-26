package biz.gelicon.core.view;

import javax.persistence.Column;

public class ObjectRoleView {

    @Column(name="controlobject_id")
    public Integer controlObjectId;

    @Column(name="controlobject_url")
    public String controlObjectUri;

    @Column(name="sqlaction_id")
    public Integer sqlActionId;

    @Column(name="accessrole_id")
    public Integer accessRoleId;

    public ObjectRoleView() {
    }

    public ObjectRoleView(Integer controlObjectId, String controlObjectUri, Integer sqlActionId, Integer accessRoleId) {
        this.controlObjectId = controlObjectId;
        this.controlObjectUri = controlObjectUri;
        this.sqlActionId = sqlActionId;
        this.accessRoleId = accessRoleId;
    }

    public Integer getControlObjectId() {
        return controlObjectId;
    }

    public void setControlObjectId(Integer controlObjectId) {
        this.controlObjectId = controlObjectId;
    }

    public String getControlObjectUri() {
        return controlObjectUri;
    }

    public void setControlObjectUri(String controlObjectUri) {
        this.controlObjectUri = controlObjectUri;
    }

    public Integer getSqlActionId() {
        return sqlActionId;
    }

    public void setSqlActionId(Integer sqlActionId) {
        this.sqlActionId = sqlActionId;
    }

    public Integer getAccessRoleId() {
        return accessRoleId;
    }

    public void setAccessRoleId(Integer accessRoleId) {
        this.accessRoleId = accessRoleId;
    }

}
