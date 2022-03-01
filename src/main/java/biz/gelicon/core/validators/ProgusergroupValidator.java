package biz.gelicon.core.validators;

import biz.gelicon.core.model.ControlObject;
import biz.gelicon.core.model.Progusergroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.validation.ConstraintViolation;
import java.util.Set;

@Component
public class ProgusergroupValidator implements Validator {

    @Autowired
    private javax.validation.Validator validator;

    // Проверка на совпадение класса
    @Override
    public boolean supports(Class<?> aClass) {
        return Progusergroup.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        // вызов стандартного валидатора
        Set<ConstraintViolation<Object>> validates = validator.validate(target);
        Progusergroup progusergroup = (Progusergroup) target;

        if(progusergroup.getProgusergroupId() != null && progusergroup.getProgusergroupId() == 1) {
            errors.rejectValue("progusergroupId", "", "Эту группу запрещено модифицировать");
        }
        // Формируем список ошибок
        ValidateUtils.fillErrors(errors, validates);
    }
}

