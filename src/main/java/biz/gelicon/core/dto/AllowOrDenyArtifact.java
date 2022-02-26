package biz.gelicon.core.dto;

public class AllowOrDenyArtifact extends AllowOrDeny {
    public Integer[] getArtifactIds() {
        return controlObjectIds;
    }
    public void setArtifactIds(Integer[] controlObjectIds) {
        this.controlObjectIds = controlObjectIds;
    }
}
