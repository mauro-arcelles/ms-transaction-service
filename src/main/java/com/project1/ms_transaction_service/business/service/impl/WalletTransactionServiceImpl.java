package com.project1.ms_transaction_service.business.service.impl;

import com.project1.ms_transaction_service.business.mapper.WalletTransactionMapper;
import com.project1.ms_transaction_service.business.service.WalletTransactionService;
import com.project1.ms_transaction_service.model.CreateWalletTransactionRequest;
import com.project1.ms_transaction_service.repository.WalletTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class WalletTransactionServiceImpl implements WalletTransactionService {
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private WalletTransactionMapper walletTransactionMapper;

    @Override
    public Mono<Void> createWalletTransaction(Mono<CreateWalletTransactionRequest> request) {
        log.info("Creating wallet transaction");
        return request.map(req -> walletTransactionMapper.getWalletTransactionEntity(req))
            .flatMap(walletTransactionRepository::save)
            .then();
    }
}
