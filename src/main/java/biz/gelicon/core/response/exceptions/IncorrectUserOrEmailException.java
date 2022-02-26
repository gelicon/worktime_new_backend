package biz.gelicon.core.response.exceptions;

public class IncorrectUserOrEmailException extends IncorrectUserOrPasswordException {
    public IncorrectUserOrEmailException() {
        super("Неверное имя пользователя или незарегистрированный email адрес");
    }
}

