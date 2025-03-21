package com.project1.ms_transaction_service.business.service.impl;

import com.project1.ms_transaction_service.business.adapter.YankiService;
import com.project1.ms_transaction_service.business.mapper.WalletTransactionMapper;
import com.project1.ms_transaction_service.business.service.WalletTransactionService;
import com.project1.ms_transaction_service.model.CreateWalletTransactionRequest;
import com.project1.ms_transaction_service.model.GetYankiWalletResponse;
import com.project1.ms_transaction_service.model.UpdateYankiWalletRequest;
import com.project1.ms_transaction_service.model.entity.WalletTransactionType;
import com.project1.ms_transaction_service.repository.WalletTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@Slf4j
public class WalletTransactionServiceImpl implements WalletTransactionService {
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private WalletTransactionMapper walletTransactionMapper;

    @Autowired
    private YankiService yankiService;

    @Override
    public Mono<Void> createWalletTransaction(Mono<CreateWalletTransactionRequest> request) {
        log.info("Creating wallet transaction");
        return request.map(walletTransactionMapper::getWalletTransactionEntity)
            .flatMap(e ->
                Mono.zip(yankiService.getYankiWallet(e.getOriginWalletId()),
                        yankiService.getYankiWallet(e.getDestinationWalletId()))
                    .flatMap(tuple -> {
                        GetYankiWalletResponse originWallet = tuple.getT1();
                        GetYankiWalletResponse destinationWallet = tuple.getT2();

                        if (WalletTransactionType.TRANSFER.toString().equals(e.getType())) {
                            UpdateYankiWalletRequest updateOriginWalletRequest = new UpdateYankiWalletRequest();
                            if (originWallet.getBalance() != null) {
                                updateOriginWalletRequest.setBalance(originWallet.getBalance().subtract(e.getAmount()));
                            }

                            UpdateYankiWalletRequest updateDestinationWalletRequest = new UpdateYankiWalletRequest();
                            if (destinationWallet.getBalance() != null) {
                                updateDestinationWalletRequest.setBalance(destinationWallet.getBalance().add(e.getAmount()));
                            }

                            return Mono.zip(yankiService.updateYankiWallet(e.getOriginWalletId(), updateOriginWalletRequest),
                                    yankiService.updateYankiWallet(e.getDestinationWalletId(), updateDestinationWalletRequest))
                                .then(Mono.just(e));
                        } else {
                            UpdateYankiWalletRequest updateOriginWalletRequest = new UpdateYankiWalletRequest();
                            if (originWallet.getBalance() != null) {
                                updateOriginWalletRequest.setBalance(originWallet.getBalance().add(e.getAmount()));
                            }

                            return yankiService.updateYankiWallet(e.getOriginWalletId(), updateOriginWalletRequest)
                                .then(Mono.just(e));
                        }
                    }))
            .flatMap(walletTransactionRepository::save)
            .then();
    }
}
