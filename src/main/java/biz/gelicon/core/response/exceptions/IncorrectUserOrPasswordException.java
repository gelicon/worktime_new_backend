package biz.gelicon.core.response.exceptions;

public class IncorrectUserOrPasswordException extends RuntimeException {
    public IncorrectUserOrPasswordException() {
        super("Неверное имя пользователя или пароль");
    }

    public IncorrectUserOrPasswordException(String message) {
        super(message);
    }
}
