package biz.gelicon.core.view;

import biz.gelicon.core.model.Proguser;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Column;
import java.util.List;

@Schema(description = "Представление объекта \"Роли пользователя\"")
public class ProguserAccessRoleView {
    @Schema(description = "Идентификатор \"Пользователь\"")
    @Column(name="proguser_id")
    public Integer proguserId;

    @Schema(description = "Имя")
    @Column(name="proguser_name")
    private String proguserName;

    @Schema(description = "Полное имя")
    @Column(name="proguser_fullname")
    private String proguserFullname;

    // Список доступных пользователю родей
    private List<Integer> accessRoleIds;

    public ProguserAccessRoleView() {
    }

    public ProguserAccessRoleView(Proguser entity) {
        this.proguserId = entity.getProguserId();
        this.proguserName = entity.getProguserName();
        this.proguserFullname = entity.getProguserFullName();
    }

    public Integer getProguserId() {
        return proguserId;
    }

    public void setProguserId(Integer proguserId) {
        this.proguserId = proguserId;
    }

    public String getProguserName() {
        return proguserName;
    }

    public void setProguserName(String proguserName) {
        this.proguserName = proguserName;
    }

    public String getProguserFullname() {
        return proguserFullname;
    }

    public void setProguserFullname(String proguserFullname) {
        this.proguserFullname = proguserFullname;
    }

    public List<Integer> getAccessRoleIds() {
        return accessRoleIds;
    }

    public void setAccessRoleIds(List<Integer> accessRoleIds) {
        this.accessRoleIds = accessRoleIds;
    }
}
