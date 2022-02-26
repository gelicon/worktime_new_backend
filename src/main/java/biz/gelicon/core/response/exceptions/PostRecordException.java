package biz.gelicon.core.response.exceptions;

import org.springframework.validation.BindingResult;

public class PostRecordException extends RuntimeException {

    private final Class<?> modelCls;
    private BindingResult bindingResult;
    private static final String errText = "Ошибка модификации данных";

    public PostRecordException(
            BindingResult bindingResult,
            Class<?> modelCls,
            Throwable err
    ) {
        super(errText, err);
        this.modelCls = modelCls;
        this.bindingResult = bindingResult;
    }

    public BindingResult getBindingResult() {
        return bindingResult;
    }

    public void setBindingResult(BindingResult bindingResult) {
        this.bindingResult = bindingResult;
    }

    public Class<?> getModelCls() {
        return modelCls;
    }
}
