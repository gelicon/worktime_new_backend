package biz.gelicon.core.model;

import biz.gelicon.core.annotations.TableDescription;
import biz.gelicon.core.response.exceptions.TokenExpiredException;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "proguserauth")
@TableDescription("Токен доступа")
public class ProguserAuth {

    @Id
    @Column(name = "proguserauth_id")
    private Integer proguserAuthId;

    @Column(name = "proguser_id",nullable = false)
    private Integer proguserId;

    @Column(name = "proguserauth_datecreate",nullable = false)
    private Date proguserAuthDateCreate;

    @Column(name = "proguserauth_lastquery",nullable = false)
    private Date proguserAuthLastQuery;

    @Column(name = "proguserauth_dateend",nullable = true)
    private Date proguserAuthDateEnd;

    @Column(name = "proguserauth_token",nullable = true,length = 128)
    private String proguserAuthToken;

    public ProguserAuth() {
    }

    public ProguserAuth(Integer progUserAuthId, Integer progUserId, Date progUserAuthDateCreate, String progUserAuthToken) {
        this.proguserAuthId = progUserAuthId;
        this.proguserId = progUserId;
        this.proguserAuthDateCreate = progUserAuthDateCreate;
        this.proguserAuthLastQuery = this.proguserAuthDateCreate;
        this.proguserAuthToken = progUserAuthToken;
    }

    public ProguserAuth(Integer progUserAuthId, Integer progUserId, Date progUserAuthDateCreate, Date progUserAuthDateEnd, String progUserAuthToken) {
        this.proguserAuthId = progUserAuthId;
        this.proguserId = progUserId;
        this.proguserAuthDateCreate = progUserAuthDateCreate;
        this.proguserAuthLastQuery = this.proguserAuthDateCreate;
        this.proguserAuthDateEnd=progUserAuthDateEnd;
        this.proguserAuthToken = progUserAuthToken;
    }

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

    public void checkExpired() {
        if(proguserAuthDateEnd!=null && proguserAuthDateEnd.before(new Date())) {
            throw new TokenExpiredException();
        }
    }

}
