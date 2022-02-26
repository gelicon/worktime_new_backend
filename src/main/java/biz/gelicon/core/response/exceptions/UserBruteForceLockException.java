package biz.gelicon.core.response.exceptions;

public class UserBruteForceLockException extends RuntimeException {
    public UserBruteForceLockException(int attempCount) {
        super(String.format("После %d попыток ввода пароля ваша учетная запись временно заблокирована",attempCount));
    }
}
