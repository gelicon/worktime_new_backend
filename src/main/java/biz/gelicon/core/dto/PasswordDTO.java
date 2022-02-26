package biz.gelicon.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Данные для смены пароля")
public class PasswordDTO {
    @Schema(description = "Старый пароль")
    private String oldPassword;
    @Schema(description = "Новый пароль")
    private String newPassword;

    public PasswordDTO() {
    }

    public PasswordDTO(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

}
