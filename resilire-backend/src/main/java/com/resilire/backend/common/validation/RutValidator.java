package com.resilire.backend.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RutValidator implements ConstraintValidator<ValidRut, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            // @NotBlank on the field already reports the "required" case.
            return true;
        }
        return RutUtil.isValid(value);
    }
}
