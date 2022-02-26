package biz.gelicon.core.response.exceptions;

public class FetchQueryException extends RuntimeException {
    public FetchQueryException(String errorMessage, Throwable err){
        super(errorMessage, err);
    }

}
