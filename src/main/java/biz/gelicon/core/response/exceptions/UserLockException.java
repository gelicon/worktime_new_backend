package biz.gelicon.core.response.exceptions;

public class UserLockException extends RuntimeException {
    public UserLockException() {
        super("Пользователь заблокирован или находится в архиве");
    }
}
