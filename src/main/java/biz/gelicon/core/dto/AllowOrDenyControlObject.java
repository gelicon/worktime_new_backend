package biz.gelicon.core.dto;

public class AllowOrDenyControlObject extends AllowOrDeny {
    public Integer[] getControlObjectIds() {
        return controlObjectIds;
    }
    public void setControlObjectIds(Integer[] controlObjectIds) {
        this.controlObjectIds = controlObjectIds;
    }
}
