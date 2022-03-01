package biz.gelicon.core.view;

import biz.gelicon.core.model.Proguser;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Column;
import javax.persistence.Id;

@Schema(description = "Представление пользователя")
public class ProguserView {

    @Schema(description = "Идентификатор")
    @Id
    @Column(name="proguser_id")
    private Integer proguserId;

    @Schema(description = "Идентификатор статуса")
    @Column(name = "proguser_status_id")
    private Integer statusId;

    @Schema(description = "Наименование статуса")
    private String proguserStatusName;

    @Schema(description = "Тип")
    @Column(name = "proguser_type")
    private Integer proguserType;

    @Schema(description = "Наименование типа")
    @Column(name = "proguser_type__name")
    private String proguserTypeName;

    @Schema(description = "Имя")
    @Column(name = "proguser_name")
    private String proguserName;

    @Schema(description = "Полное имя")
    @Column(name = "proguser_fullname")
    private String proguserFullName;

    public ProguserView() {
    }

    public ProguserView(Integer proguserId,  Integer statusId,
            Integer proguserType, String proguserTypeName, String proguserName,
            String proguserFullName) {
        this.proguserId = proguserId;
        this.statusId = statusId;
        this.proguserStatusName = this.getProguserStatusName();
        this.proguserType = proguserType;
        this.proguserTypeName = this.getProguserTypeName();
        this.proguserName = proguserName;
        this.proguserFullName = proguserFullName;
    }

    public ProguserView(Proguser proguser) {
        this.proguserId = proguser.getProguserId();
        this.statusId = proguser.getStatusId();
        this.proguserStatusName = this.getProguserStatusName();
        this.proguserType = proguser.getProguserType();
        this.proguserTypeName = this.getProguserTypeName();
        this.proguserName = proguser.getProguserName();
        this.proguserFullName = proguser.getProguserFullName();
    }

    public void setProguserId(Integer proguserId) {
        this.proguserId = proguserId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public void setProguserStatusName(String proguserStatusName) {
        this.proguserStatusName = proguserStatusName;
    }

    public void setProguserType(Integer proguserType) {
        this.proguserType = proguserType;
    }

    public void setProguserTypeName(String proguserTypeName) {
        this.proguserTypeName = proguserTypeName;
    }

    public void setProguserName(String proguserName) {
        this.proguserName = proguserName;
    }

    public void setProguserFullName(String proguserFullName) {
        this.proguserFullName = proguserFullName;
    }

    public Integer getProguserId() {
        return proguserId;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public String getProguserStatusName() {
        return statusId == Proguser.USER_IS_ACTIVE ? "Активный" : "Заблокированный";
    }

    public Integer getProguserType() {
        return proguserType;
    }

    public String getProguserTypeName() {
        return proguserType == 1 ? "Администратор" : "Обычный";
    }

    public String getProguserName() {
        return proguserName;
    }

    public String getProguserFullName() {
        return proguserFullName;
    }
}
