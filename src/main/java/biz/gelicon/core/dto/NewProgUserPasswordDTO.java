package biz.gelicon.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Id;

@Schema(description = "Данные для установки пароля")
public class NewProgUserPasswordDTO {
    @Id
    @Schema(description = "Идентификатор пользователя")
    private Integer proguserId;
    @Schema(description = "Новый пароль")
    private String newPassword;
    @Schema(description = "Флаг временного пароля")
    private Integer tempFlag;

    public NewProgUserPasswordDTO() {
    }

    public NewProgUserPasswordDTO(Integer proguserId, String newPassword, Integer tempFlag) {
        this.proguserId = proguserId;
        this.newPassword = newPassword;
        this.tempFlag = tempFlag;
    }

    public Integer getProguserId() {
        return proguserId;
    }

    public void setProguserId(Integer proguserId) {
        this.proguserId = proguserId;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public Integer getTempFlag() {
        return tempFlag;
    }

    public void setTempFlag(Integer tempFlag) {
        this.tempFlag = tempFlag;
    }
}
