package biz.gelicon.core.response.exceptions;

public class HasTemporaryAuthenticationException extends RuntimeException {
    public HasTemporaryAuthenticationException() {
        super("У пользователя обнаружен временный пароль. Требуется поменять его на постоянный");
    }

    public HasTemporaryAuthenticationException(String message) {
        super(message);
    }
}
