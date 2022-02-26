package biz.gelicon.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Id;
import java.util.List;

@Schema(description = "Роли объекта \"Пользователь\"")
public class ProguserRoleDTO {
    @Id
    @Schema(description = "Идентификатор \"Пользователь\"")
    private Integer proguserId;

    @Schema(description = "Список идентификаторов ролей")
    private List<Integer> accessRoleIds;

    public ProguserRoleDTO() {
    }

    public Integer getProguserId() {
        return proguserId;
    }

    public void setProguserId(Integer proguserId) {
        this.proguserId = proguserId;
    }

    public List<Integer> getAccessRoleIds() {
        return accessRoleIds;
    }

    public void setAccessRoleIds(List<Integer> accessRoleIds) {
        this.accessRoleIds = accessRoleIds;
    }
}
