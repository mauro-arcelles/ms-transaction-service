package com.project1.ms_transaction_service.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
public class EnumNamePatternValidatorTest {

    private EnumNamePatternValidator validator;

    @MockBean
    private ConstraintValidatorContext context;

    @MockBean
    private EnumNamePattern annotation;

    @BeforeEach
    void setUp() {
        validator = new EnumNamePatternValidator();
        when(annotation.regexp()).thenReturn("PURCHASE|WITHDRAWAL");
        validator.initialize(annotation);
    }

    @Test
    void whenNullValue_thenValid() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void whenValidPattern_thenValid() {
        assertTrue(validator.isValid("PURCHASE", context));
    }

    @Test
    void whenInvalidPattern_thenInvalid() {
        assertFalse(validator.isValid("payment", context));
    }
}
