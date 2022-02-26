package biz.gelicon.core.dto;

import biz.gelicon.core.model.Proguser;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Column;
import javax.persistence.Id;

/* Объект сгенерирован 27.04.2021 09:20 */
@Schema(description = "Объект \"Пользователь\"")
public class ProguserDTO {

    @Id
    @Schema(description = "Идентификатор \"Пользователь\"")
    private Integer proguserId;

    @Schema(description = "Статус")
    private Integer statusId;

    @Schema(description = "Статус")
    @Column(name="proguser_status_display")
    private String statusDisplay;

    @Schema(description = "Имя")
    private String proguserName;

    @Schema(description = "Полное имя")
    private String proguserFullname;

    @Schema(description = "Адрес электронной почты")
    @Column(name="proguserchannel_address")
    private String proguserchannelAddress;

    @Schema(description = "ОАУ с которым связан пользователь")
    private SelectDisplayData<Integer> subject;

    @Schema(description = "Сотрудник с которым связан пользователь")
    private SelectDisplayData<Integer> worker;

    public ProguserDTO() {}

    public ProguserDTO(
            Integer proguserId,
            Integer statusId,
            String proguserName,
            String proguserFullname) {
        this.proguserId = proguserId;
        this.statusId = statusId;
        this.proguserName = proguserName;
        this.proguserFullname = proguserFullname;
    }

    public ProguserDTO(Proguser entity) {
        this.proguserId = entity.getProguserId();
        this.statusId = entity.getStatusId();
        this.proguserName = entity.getProguserName();
        this.proguserFullname = entity.getProguserFullName();
    }

    public Proguser toEntity() {
        return toEntity(new Proguser());
    }

    public Proguser toEntity(Proguser entity) {
        entity.setProguserId(this.proguserId);
        entity.setStatusId(this.statusId);
        entity.setProguserName(this.proguserName);
        entity.setProguserFullName(this.proguserFullname);
        return entity;
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

    public SelectDisplayData<Integer> getSubject() {
        return subject;
    }

    public void setSubject(SelectDisplayData<Integer> subject) {
        this.subject = subject;
    }

    public SelectDisplayData<Integer> getWorker() {
        return worker;
    }

    public void setWorker(SelectDisplayData<Integer> worker) {
        this.worker = worker;
    }
}

