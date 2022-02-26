package biz.gelicon.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Id;

@Schema(description = "Требуемые разрешения для роли")
abstract public class AllowOrDeny {
    @Id
    @Schema(description = "Идентификатор роли")
    private Integer accessRoleId;
    @Schema(description = "Идентификаторы объектов")
    protected Integer[] controlObjectIds;

    public Integer getAccessRoleId() {
        return accessRoleId;
    }
    public void setAccessRoleId(Integer accessRoleId) {
        this.accessRoleId = accessRoleId;
    }

}
