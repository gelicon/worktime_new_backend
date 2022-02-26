package biz.gelicon.core.security;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Учетные данные для идентификации")
public class UserCredential {
    @Schema(description = "Логин пользователя",example = "SYSDBA")
    private String userName;
    @Schema(description = "Пароль пользователя",example = "masterkey")
    private String password;

    public UserCredential() {
    }

    public UserCredential(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
