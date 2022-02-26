package biz.gelicon.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Данные для смены пароля")
public class PasswordAndUserDTO extends PasswordDTO{

    @Schema(description = "Имя пользователя")
    private String userName;

    public PasswordAndUserDTO() {
    }

    public PasswordAndUserDTO(String userName, String oldPassword, String newPassword) {
        super(oldPassword, newPassword);
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
