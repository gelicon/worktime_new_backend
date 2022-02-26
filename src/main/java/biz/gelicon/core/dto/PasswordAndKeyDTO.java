package biz.gelicon.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Данные для смены пароля")
public class PasswordAndKeyDTO {
    @Schema(description = "Временный ключ, в котором зашифрован пользователь")
    private String key;
    @Schema(description = "Новый пароль")
    private String newPassword;

    public PasswordAndKeyDTO() {
    }

    public PasswordAndKeyDTO(String key, String newPassword) {
        this.key = key;
        this.newPassword = newPassword;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
