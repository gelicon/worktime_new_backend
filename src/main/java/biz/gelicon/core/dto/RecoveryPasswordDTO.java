package biz.gelicon.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Данные для запроса на восстановление пароля")
public class RecoveryPasswordDTO {
    @Schema(description = "Действующий email или логин пользователя")
    private String emailOrLogin;

    public RecoveryPasswordDTO() {
    }

    public RecoveryPasswordDTO(String emailOrLogin) {
        this.emailOrLogin = emailOrLogin;
    }

    public String getEmailOrLogin() {
        return emailOrLogin;
    }

    public void setEmailOrLogin(String emailOrLogin) {
        this.emailOrLogin = emailOrLogin;
    }
}
