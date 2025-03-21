package com.project1.ms_transaction_service.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class CustomObjectMapper {
    public <T> T stringToObject(String message, Class<T> clazz) throws JsonProcessingException {
        return new ObjectMapper().readValue(message, clazz);
    }
}
