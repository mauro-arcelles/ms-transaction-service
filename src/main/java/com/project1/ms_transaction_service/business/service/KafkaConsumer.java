package com.project1.ms_transaction_service.business.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project1.ms_transaction_service.config.CustomObjectMapper;
import com.project1.ms_transaction_service.model.CreateWalletTransactionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class KafkaConsumer {
    @Autowired
    private WalletTransactionService walletTransactionService;

    @Autowired
    private CustomObjectMapper customObjectMapper;

    @KafkaListener(topics = "${application.config.kafka.topic-name}", groupId = "${application.config.kafka.consumer.group-id}")
    public void listen(String message) throws JsonProcessingException {
        log.info("Received message: {}", message);
        CreateWalletTransactionRequest request = customObjectMapper.stringToObject(message);
        walletTransactionService.createWalletTransaction(Mono.just(request))
            .doOnSuccess(e -> log.info("Transaction created successfully"))
            .subscribe();
    }
}
