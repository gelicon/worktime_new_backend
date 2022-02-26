package biz.gelicon.core.response.exceptions;

public class BadPagingException extends RuntimeException {
    public BadPagingException(String errorMessage) {
        super(errorMessage);
    }
}
