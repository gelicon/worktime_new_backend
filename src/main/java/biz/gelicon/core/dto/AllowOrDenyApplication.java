package biz.gelicon.core.dto;

public class AllowOrDenyApplication extends AllowOrDeny {
    public Integer[] getApplicationIds() {
        return controlObjectIds;
    }
    public void setApplicationIds(Integer[] controlObjectIds) {
        this.controlObjectIds = controlObjectIds;
    }
}
