package biz.gelicon.core.response.exceptions;

public class TokenExpiredException  extends RuntimeException {
    public TokenExpiredException() {
        super("Срок действия токена истек");
    }
}
