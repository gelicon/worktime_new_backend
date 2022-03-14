package biz.gelicon.core.worktime.controllers.department;

import biz.gelicon.core.validators.ValidateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.validation.ConstraintViolation;
import java.util.Set;

@Component
public class DepartmentValidator implements Validator {

    @Autowired
    private javax.validation.Validator validator;

    // Проверка на совпадение класса
    @Override
    public boolean supports(Class<?> aClass) {
        return Department.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        // вызов стандартного валидатора
        Set<ConstraintViolation<Object>> validates = validator.validate(target);
        Department department = (Department) target;

        if (department.getDepartmentId() != null && department.getDepartmentId() == 1) {
            errors.rejectValue("departmentId", "", "Этот отдел запрещено модифицировать");
        }
        if (!(department.getDepartmentStatus() == 1 || department.getDepartmentStatus() == 0)) {
            errors.rejectValue("departmentId", "",
                    "Статус может быть либо 1 (Действующий) либо 0 (Закрытый)");
        }
        // Формируем список ошибок
        ValidateUtils.fillErrors(errors, validates);
    }
}

