package com.project1.ms_transaction_service.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class EnumNamePatternValidator implements ConstraintValidator<EnumNamePattern, String> {
    private Pattern pattern;

    @Override
    public void initialize(EnumNamePattern annotation) {
        pattern = Pattern.compile(annotation.regexp());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return pattern.matcher(value).matches();
    }
}
