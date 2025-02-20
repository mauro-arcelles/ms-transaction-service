package com.project1.ms_transaction_service.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.project1.ms_transaction_service.model.ResponseBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, List<String>>>> handleValidationErrors(WebExchangeBindException ex) {
        log.error("error", ex);
        Map<String, List<String>> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.groupingBy(
                FieldError::getField,
                Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
            ));
        return Mono.just(ResponseEntity.badRequest().body(errors));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<String>> handleGenericError(Exception ex) {
        log.error("error", ex);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Internal Server Error"));
    }

    @ExceptionHandler(BadRequestException.class)
    public Mono<ResponseEntity<ResponseBase>> handleBadRequestException(Exception ex) {
        log.error("error", ex);
        ResponseBase responseBase = new ResponseBase();
        responseBase.setMessage(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(responseBase));
    }

    @ExceptionHandler(CreditCardCustomerMismatchException.class)
    public Mono<ResponseEntity<ResponseBase>> handleCreditCardCustomerMismatchException(Exception ex) {
        log.error("error", ex);
        ResponseBase responseBase = new ResponseBase();
        responseBase.setMessage(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(responseBase));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<Map<String, List<String>>>> handleServerWebInputException(ServerWebInputException ex) {
        log.error("error", ex);
        Map<String, List<String>> errors = new HashMap<>();

        if (ex.getCause() instanceof DecodingException) {
            DecodingException decodingException = (DecodingException) ex.getCause();
            if (decodingException.getCause() instanceof InvalidFormatException) {
                InvalidFormatException invalidFormatException = (InvalidFormatException) decodingException.getCause();
                String fieldName = invalidFormatException.getPath().get(0).getFieldName();
                String targetType = invalidFormatException.getTargetType().getSimpleName();
                errors.put(fieldName, List.of("Must be a valid " + targetType.toLowerCase()));
            }
        } else {
            errors.put("errors", List.of(ex.getMessage()));
        }

        return Mono.just(ResponseEntity.badRequest().body(errors));
    }
}
