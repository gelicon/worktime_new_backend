package biz.gelicon.core.view;

import biz.gelicon.core.model.Proguser;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.persistence.Column;

/* Объект сгенерирован 27.04.2021 09:20 */
@Schema(description = "Представление объекта \"Пользователь\"")
public class ProguserView {

    @Schema(description = "Идентификатор \"Пользователь\"")
    @Column(name="proguser_id")
    public Integer proguserId;

    @Schema(description = "Идентификатор статуса")
    @Column(name="proguser_status_id")
    private Integer statusId;

    @Schema(description = "Статус")
    @Column(name="proguser_status_display",table = "capcode")
    private String statusDisplay;

    @Schema(description = "Имя")
    @Column(name="proguser_name")
    private String proguserName;

    @Schema(description = "Полное имя")
    @Column(name="proguser_fullname")
    private String proguserFullname;

    @Schema(description = "Адрес электронной почты")
    @Column(name="proguserchannel_address",table = "proguserchannel")
    private String proguserchannelAddress;



    public ProguserView() {}

    public ProguserView(
            Integer proguserId,
            Integer progusergroupId,
            Integer statusId,
            Integer proguserType,
            String proguserName,
            String proguserFullname,
            String progusergroupName) {
        this.proguserId = proguserId;
        this.statusId = statusId;
        this.proguserName = proguserName;
        this.proguserFullname = proguserFullname;
    }

    public ProguserView(Proguser entity) {
        this.proguserId = entity.getProguserId();
        this.statusId = entity.getStatusId();
        this.proguserName = entity.getProguserName();
        this.proguserFullname = entity.getProguserFullName();
    }

    public Integer getProguserId() {
        return proguserId;
    }

    public void setProguserId(Integer value) {
        this.proguserId = value;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer value) {
        this.statusId = value;
    }


    public String getProguserName() {
        return proguserName;
    }

    public void setProguserName(String value) {
        this.proguserName = value;
    }

    public String getProguserFullname() {
        return proguserFullname;
    }

    public void setProguserFullname(String value) {
        this.proguserFullname = value;
    }

    public String getProguserchannelAddress() {
        return proguserchannelAddress;
    }

    public void setProguserchannelAddress(String proguserchannelAddress) {
        this.proguserchannelAddress = proguserchannelAddress;
    }

    public String getStatusDisplay() {
        return statusDisplay;
    }

    public void setStatusDisplay(String statusDisplay) {
        this.statusDisplay = statusDisplay;
    }
}

