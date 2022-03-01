package biz.gelicon.core.model;

import biz.gelicon.core.annotations.ColumnDescription;
import biz.gelicon.core.security.UserDetailsImpl;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

@Table(name = "proguser")
public class Proguser {
    public static String PROFILE_ATTR_SUBJECT = "subject";
    public static String PROFILE_ATTR_OWNER = "owner";
    /**
     * proguser_id для пользователя SYSDBA
     */
    public static final int SYSDBA_PROGUSER_ID = 1;
    // Активный пользователь
    public final static int USER_IS_ACTIVE = 1;
    // Заблокированный пользователь
    public final static int USER_IS_BLOCKED = 0;

    @Id
    @Column(name = "proguser_id")
    private Integer proguserId;

    @ManyToOne(targetEntity = CapCode.class)
    @NotNull(message = "Статус пользователя не может быть пустым")
    @Column(name = "proguser_status_id")
    private Integer statusId;

    @Column(name = "proguser_type", nullable = true)
    private Integer proguserType = 0;

    @NotNull(message = "Имя пользователя не может быть пустым")
    @Size(max = 50, message = "Имя пользователя должно содержать не более {max} символов")
    @ColumnDescription("Имя пользователя")
    @Column(name = "proguser_name")
    private String proguserName;

    @Size(max = 50, message = "Полное имя пользователя должно содержать не более {max} символов")
    @Column(name = "proguser_fullname")
    private String proguserFullName;

    public Proguser() {
    }

    public Proguser(Integer progUserId, Integer statusId,
                    Integer proguserType, String progUserName, String progUserFullName) {
        this.proguserId = progUserId;
        this.statusId = statusId;
        this.proguserType = proguserType;
        this.proguserName = progUserName;
        this.proguserFullName = progUserFullName;
    }

    public Integer getProguserId() {
        return proguserId;
    }

    public void setProguserId(Integer proguserId) {
        this.proguserId = proguserId;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public Integer getProguserType() {
        return proguserType;
    }

    public void setProguserType(Integer proguserType) {
        this.proguserType = proguserType;
    }

    public String getProguserName() {
        return proguserName;
    }

    public void setProguserName(String proguserName) {
        this.proguserName = proguserName;
    }

    public String getProguserFullName() {
        return proguserFullName;
    }

    public void setProguserFullName(String proguserFullName) {
        this.proguserFullName = proguserFullName;
    }

    public UserDetails toUserDetail() {
        return new UserDetailsImpl(this);
    }

    @Override
    public String toString() {
        return "Proguser{" +
                "proguserId=" + proguserId +
                ", statusId=" + statusId +
                ", proguserType=" + proguserType +
                ", proguserName='" + proguserName + '\'' +
                ", proguserFullName='" + proguserFullName + '\'' +
                '}';
    }
}
