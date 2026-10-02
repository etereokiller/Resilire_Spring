package com.resilire.backend.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RutValidator.class)
public @interface ValidRut {
    String message() default "validation.rut.invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
