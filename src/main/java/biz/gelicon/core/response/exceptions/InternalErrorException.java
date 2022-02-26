package biz.gelicon.core.response.exceptions;

public class InternalErrorException extends RuntimeException {
    public InternalErrorException(int code) {
        super(String.format("Внутренняя ошибка с кодом %d",code));
    }
}
