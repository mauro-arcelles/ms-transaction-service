package com.project1.ms_transaction_service.business.service.impl;

import com.project1.ms_transaction_service.business.adapter.BootcoinService;
import com.project1.ms_transaction_service.business.service.BootcoinTransactionService;
import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.model.CreateBootcoinWalletResponse;
import com.project1.ms_transaction_service.model.UpdateBootcoinWalletRequest;
import com.project1.ms_transaction_service.model.UpdateExchangeRequestRequest;
import com.project1.ms_transaction_service.model.entity.ExchangeRequestStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@Slf4j
public class BootcoinTransactionServiceImpl implements BootcoinTransactionService {
    @Autowired
    private BootcoinService bootcoinService;

    @Override
    public Mono<Void> processBootcoinTransaction(String transactionId) {
        return bootcoinService.getExchangeRequestByTransactionId(transactionId)
            .doOnSuccess(e -> log.info("Exchange request found {} for transactionId: {}", e, transactionId))
            .flatMap(exchangeRequest -> {
                return Mono.zip(bootcoinService.getBootcoinWalletByUserId(exchangeRequest.getRequestOwnerUserId()),
                        bootcoinService.getBootcoinWalletByUserId(exchangeRequest.getRequestAccepterUserId()))
                    .flatMap(tuple -> {
                        CreateBootcoinWalletResponse requestOwnerWallet = tuple.getT1();
                        CreateBootcoinWalletResponse requestAccepterWallet = tuple.getT2();

                        // verify payment method within exchangeRequest to discount the amount in the respective payment method
                        if (requestOwnerWallet.getBalance() != null && requestAccepterWallet.getBalance() != null) {
                            if (exchangeRequest.getAmount().compareTo(requestAccepterWallet.getBalance()) > 0) {
                                log.error("Insufficient balance in wallet of the request accepter");
                                UpdateExchangeRequestRequest updateExchangeRequestRequest = new UpdateExchangeRequestRequest();
                                updateExchangeRequestRequest.status(ExchangeRequestStatus.REJECTED.toString());
                                updateExchangeRequestRequest.setMessage("Insufficient balance in request accepter wallet");

                                return bootcoinService.updateExchangeRequest(exchangeRequest.getId(), updateExchangeRequestRequest)
                                    .doOnError(e -> log.error("Error updating exchange request"));
                            }

                            // update payment method balance

                            BigDecimal newOwnerWalletBalance = requestOwnerWallet.getBalance().add(exchangeRequest.getAmount());
                            BigDecimal newAccepterWalletBalance = requestAccepterWallet.getBalance().subtract(exchangeRequest.getAmount());

                            UpdateBootcoinWalletRequest updateOwnerWalletRequest = new UpdateBootcoinWalletRequest();
                            updateOwnerWalletRequest.balance(newOwnerWalletBalance);

                            UpdateBootcoinWalletRequest updateAccepterWalletRequest = new UpdateBootcoinWalletRequest();
                            updateAccepterWalletRequest.balance(newAccepterWalletBalance);

                            log.info("Updating bootcoin wallets");

                            return Mono.zip(bootcoinService.updateBootcoinWallet(requestOwnerWallet.getId(), updateOwnerWalletRequest),
                                    bootcoinService.updateBootcoinWallet(requestAccepterWallet.getId(), updateAccepterWalletRequest))
                                .doOnSuccess(e -> log.info("Bootcoin wallets updated successfully"))
                                .doOnError(e -> {
                                    log.error("Bootcoin wallets update failed", e);
                                    UpdateExchangeRequestRequest updateExchangeRequestRequest = new UpdateExchangeRequestRequest();
                                    updateExchangeRequestRequest.status(ExchangeRequestStatus.REJECTED.toString());
                                    updateExchangeRequestRequest.setMessage("Bootcoin wallets update failed");

                                    bootcoinService.updateExchangeRequest(exchangeRequest.getId(), updateExchangeRequestRequest)
                                        .doOnError(t -> log.error("Error updating exchange request", t));
                                });
                        }

                        log.error("Error updating bootcoin wallet");

                        return Mono.error(new BadRequestException("Wallet balance is null"));
                    });
            })
            .then();
    }
}
