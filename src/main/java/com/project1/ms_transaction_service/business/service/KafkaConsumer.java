package com.project1.ms_transaction_service.business.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project1.ms_transaction_service.config.CustomObjectMapper;
import com.project1.ms_transaction_service.model.BootcoinTransactionRequest;
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
    private CustomObjectMapper customObjectMapper;

    @Autowired
    private WalletTransactionService walletTransactionService;

    @Autowired
    private BootcoinTransactionService bootcoinTransactionService;

    @KafkaListener(topics = "${application.config.kafka.topic1.topic-name}", groupId = "${application.config.kafka.topic1.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory1")
    public void listenYankiTransactions(String message) throws JsonProcessingException {
        log.info("Received yanki transaction message: {}", message);
        CreateWalletTransactionRequest request = customObjectMapper.stringToObject(message, CreateWalletTransactionRequest.class);
        walletTransactionService.createWalletTransaction(Mono.just(request))
            .doOnSuccess(e -> log.info("Transaction created successfully"))
            .subscribe();
    }

    @KafkaListener(topics = "${application.config.kafka.topic2.topic-name}", groupId = "${application.config.kafka.topic2.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory2")
    public void listenBootcoinTransactions(String message) throws JsonProcessingException {
        log.info("Received bootcoin transaction message: {}", message);
        BootcoinTransactionRequest request = customObjectMapper.stringToObject(message, BootcoinTransactionRequest.class);
        bootcoinTransactionService.processBootcoinTransaction(request.getId())
            .doOnSuccess(e -> log.info("Bootcoin transaction proccesed successfully"))
            .subscribe();
    }
}
