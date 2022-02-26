package biz.gelicon.core.response.exceptions;

public class IncorrectTokenException extends RuntimeException {
    public IncorrectTokenException() {
        super("Неверный токен доступа");
    }
}
