package biz.gelicon.core.response.exceptions;

public class ReportResultNotFoundException extends RuntimeException {
    public ReportResultNotFoundException(){
        super("Результат выполнения задания подготовки печатной формы не найден");
    }
}
