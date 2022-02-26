package biz.gelicon.core.response.exceptions;

public class DeleteRecordException extends RuntimeException {
    public DeleteRecordException(String message, RuntimeException ex) {
        super(message,ex);
    }
}
