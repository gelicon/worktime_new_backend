package biz.gelicon.core.validators;

import biz.gelicon.core.model.ControlObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.validation.ConstraintViolation;
import java.util.Set;

@Component
public class ControlObjectValidator implements Validator {

    @Autowired
    private javax.validation.Validator validator;

    // Проверка на совпадение класса
    @Override
    public boolean supports(Class<?> aClass) {
        return ControlObject.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        // вызов стандартного валидатора
        Set<ConstraintViolation<Object>> validates = validator.validate(target);
        ControlObject controlObject = (ControlObject) target;


        if(controlObject.getControlObjectUrl()!=null && !(controlObject.getControlObjectUrl().startsWith("/"))) {
            errors.rejectValue("controlObjectUrl", "", "Идентификатор должен начинаться с символа \"/\"");
        }

        // Формируем список ошибок
        ValidateUtils.fillErrors(errors, validates);
    }
}

