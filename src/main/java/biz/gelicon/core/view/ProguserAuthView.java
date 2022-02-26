package biz.gelicon.core.view;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Column;
import java.util.Date;

public class ProguserAuthView {
    @Schema(description = "Идентификатор")
    @Column(name = "proguserauth_id")
    private Integer proguserAuthId;

    @Schema(description = "Идентификатор пользователя")
    @Column(name = "proguser_id")
    private Integer proguserId;

    @Schema(description = "Дата создания")
    @Column(name = "proguserauth_datecreate")
    private Date proguserAuthDateCreate;

    @Schema(description = "Дата последнего обновления")
    @Column(name = "proguserauth_lastquery")
    private Date proguserAuthLastQuery;

    @Schema(description = "Дата закрытия")
    @Column(name = "proguserauth_dateend")
    private Date proguserAuthDateEnd;

    @Schema(description = "Токен")
    @Column(name = "proguserauth_token")
    private String proguserAuthToken;

    @Schema(description = "Идентификатор статуса")
    @Column(name="proguser_status_id", table = "proguser")
    private Integer statusId;

    @Schema(description = "Статус")
    @Column(name="proguser_status_display",table = "capcode")
    private String statusDisplay;

    @Schema(description = "Имя")
    @Column(name="proguser_name", table = "proguser")
    private String proguserName;

    public Integer getProguserAuthId() {
        return proguserAuthId;
    }

    public void setProguserAuthId(Integer proguserAuthId) {
        this.proguserAuthId = proguserAuthId;
    }

    public Integer getProguserId() {
        return proguserId;
    }

    public void setProguserId(Integer proguserId) {
        this.proguserId = proguserId;
    }

    public Date getProguserAuthDateCreate() {
        return proguserAuthDateCreate;
    }

    public void setProguserAuthDateCreate(Date proguserAuthDateCreate) {
        this.proguserAuthDateCreate = proguserAuthDateCreate;
    }

    public Date getProguserAuthLastQuery() {
        return proguserAuthLastQuery;
    }

    public void setProguserAuthLastQuery(Date proguserAuthLastQuery) {
        this.proguserAuthLastQuery = proguserAuthLastQuery;
    }

    public Date getProguserAuthDateEnd() {
        return proguserAuthDateEnd;
    }

    public void setProguserAuthDateEnd(Date proguserAuthDateEnd) {
        this.proguserAuthDateEnd = proguserAuthDateEnd;
    }

    public String getProguserAuthToken() {
        return proguserAuthToken;
    }

    public void setProguserAuthToken(String proguserAuthToken) {
        this.proguserAuthToken = proguserAuthToken;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public String getStatusDisplay() {
        return statusDisplay;
    }

    public void setStatusDisplay(String statusDisplay) {
        this.statusDisplay = statusDisplay;
    }

    public String getProguserName() {
        return proguserName;
    }

    public void setProguserName(String proguserName) {
        this.proguserName = proguserName;
    }

    @Schema(description = "Флаг активности на текущий момент")
    public Integer getActiveFlag() {
        return (proguserAuthDateEnd==null || new Date().before(proguserAuthDateEnd))?1:0;
    }


}
