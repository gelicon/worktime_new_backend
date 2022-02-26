package biz.gelicon.core.model;

import biz.gelicon.core.annotations.TableDescription;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/* Сущность сгенерирована 28.04.2021 18:49 */
@Table(name = "progusercredential")
@TableDescription("Учетная запись пользователя")
public class ProguserCredential {

    @Id
    @Column(name = "progusercredential_id",nullable = false)
    public Integer proguserCredentialId;

    @Column(name = "progusercredential_password", nullable = false)
    @Size(max = 128, message = "Поле \"Пароль\" должно содержать не более {max} символов")
    @NotBlank(message = "Поле \"progusercredential_password\" не может быть пустым")
    private String proguserCredentialPassword;

    @ManyToOne(targetEntity = Proguser.class)
    @Column(name = "proguser_id", nullable = false)
    private Integer proguserId;

    @ManyToOne(targetEntity = CapCode.class)
    @Column(name = "progusercredential_type", nullable = false)
    private Integer type;

    @Column(name = "progusercredential_lockflag", nullable = false)
    @NotNull(message = "Поле \"Флаг блокировки\" не может быть пустым")
    private Integer proguserCredentialLockflag;

    @Column(name = "progusercredential_tempflag", nullable = false)
    @NotNull(message = "Поле \"Флаг временного пароля\" не может быть пустым")
    private Integer proguserCredentialTempflag;

    public Integer getProguserCredentialId() {
        return proguserCredentialId;
    }

    public void setProguserCredentialId(Integer value) {
        this.proguserCredentialId = value;
    }

    public String getProguserCredentialPassword() {
        return proguserCredentialPassword;
    }

    public void setProguserCredentialPassword(String value) {
        this.proguserCredentialPassword = value;
    }

    public Integer getProguserId() {
        return proguserId;
    }

    public void setProguserId(Integer value) {
        this.proguserId = value;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer value) {
        this.type = value;
    }

    public Integer getProguserCredentialLockflag() {
        return proguserCredentialLockflag;
    }

    public void setProguserCredentialLockflag(Integer value) {
        this.proguserCredentialLockflag = value;
    }

    public Integer getProguserCredentialTempflag() {
        return proguserCredentialTempflag;
    }

    public void setProguserCredentialTempflag(Integer value) {
        this.proguserCredentialTempflag = value;
    }


    public ProguserCredential() {}

    public ProguserCredential(
            Integer proguserCredentialId,
            String proguserCredentialPassword,
            Integer proguserId,
            Integer type,
            Integer proguserCredentialLockflag,
            Integer proguserCredentialTempflag) {
        this.proguserCredentialId = proguserCredentialId;
        this.proguserCredentialPassword = proguserCredentialPassword;
        this.proguserId = proguserId;
        this.type = type;
        this.proguserCredentialLockflag = proguserCredentialLockflag;
        this.proguserCredentialTempflag = proguserCredentialTempflag;
    }
}

