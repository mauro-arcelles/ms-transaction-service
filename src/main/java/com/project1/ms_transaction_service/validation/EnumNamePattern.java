package com.project1.ms_transaction_service.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumNamePatternValidator.class)
public @interface EnumNamePattern {
    String regexp();
    String message() default "Invalid type value. Should be one of: {regexp}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
