package com.project1.ms_transaction_service.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project1.ms_transaction_service.model.CreateWalletTransactionRequest;
import org.springframework.stereotype.Component;

@Component
public class CustomObjectMapper {
    public CreateWalletTransactionRequest stringToObject(String message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(message, CreateWalletTransactionRequest.class);
    }
}
