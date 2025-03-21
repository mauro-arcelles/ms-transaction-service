package com.project1.ms_transaction_service.business.service.impl;

import com.project1.ms_transaction_service.business.adapter.BootcoinService;
import com.project1.ms_transaction_service.business.adapter.YankiService;
import com.project1.ms_transaction_service.business.service.BootcoinTransactionService;
import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.model.CreateBootcoinWalletResponse;
import com.project1.ms_transaction_service.model.UpdateBootcoinWalletRequest;
import com.project1.ms_transaction_service.model.UpdateExchangeRequestRequest;
import com.project1.ms_transaction_service.model.UpdateYankiWalletRequest;
import com.project1.ms_transaction_service.model.entity.ExchangeRequestStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
public class BootcoinTransactionServiceImpl implements BootcoinTransactionService {
    @Autowired
    private BootcoinService bootcoinService;

    @Autowired
    private YankiService yankiService;

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
                                log.error("Insufficient bootcoins balance in request accepter wallet");
                                UpdateExchangeRequestRequest updateExchangeRequestRequest = new UpdateExchangeRequestRequest();
                                updateExchangeRequestRequest.status(ExchangeRequestStatus.REJECTED.toString());
                                updateExchangeRequestRequest.setMessage("Insufficient bootcoins in request accepter wallet");

                                return bootcoinService.updateExchangeRequest(exchangeRequest.getId(), updateExchangeRequestRequest)
                                    .doOnError(e -> log.error("Error updating exchange request"));
                            }

                            // update payment method balance
                            if (exchangeRequest.getPaymentMethod() != null) {
                                if (exchangeRequest.getPaymentMethod().equals("YANKI")) {
                                    return yankiService.getYankiWalletByUserId(exchangeRequest.getRequestOwnerUserId())
                                        .flatMap(yankiWallet -> {
                                            BigDecimal totalAmount =
                                                exchangeRequest.getAmount().multiply(BigDecimal.valueOf(exchangeRequest.getBuyRate()));

                                            if (yankiWallet.getBalance().compareTo(totalAmount) < 0) {
                                                log.info("Insufficient balance in the yanki wallet of the request owner");
                                                UpdateExchangeRequestRequest updateExchangeRequestRequest = new UpdateExchangeRequestRequest();
                                                updateExchangeRequestRequest.status(ExchangeRequestStatus.REJECTED.toString());
                                                updateExchangeRequestRequest.setMessage("Insufficient balance in the yanki wallet of the request owner");

                                                return bootcoinService.updateExchangeRequest(exchangeRequest.getId(), updateExchangeRequestRequest)
                                                    .doOnError(t -> log.error("Error updating exchange request", t));
                                            }

                                            BigDecimal newOwnerYankiWalletBalance = yankiWallet.getBalance().subtract(totalAmount);
                                            UpdateYankiWalletRequest updateOwnerYankiWalletRequest = new UpdateYankiWalletRequest();
                                            updateOwnerYankiWalletRequest.balance(newOwnerYankiWalletBalance);

                                            return yankiService.updateYankiWallet(yankiWallet.getId(), updateOwnerYankiWalletRequest)
                                                .doOnError(e -> log.error("Error updating yanki wallet"))
                                                .then(Mono.just(exchangeRequest))
                                                .onErrorResume(e -> Mono.error(new BadRequestException("Error updating yanki wallet")));
                                        })
                                        .flatMap(e -> {
                                            BigDecimal newOwnerWalletBalance = requestOwnerWallet.getBalance().add(exchangeRequest.getAmount());
                                            BigDecimal newAccepterWalletBalance = requestAccepterWallet.getBalance().subtract(exchangeRequest.getAmount());

                                            UpdateBootcoinWalletRequest updateOwnerWalletRequest = new UpdateBootcoinWalletRequest();
                                            updateOwnerWalletRequest.balance(newOwnerWalletBalance);

                                            UpdateBootcoinWalletRequest updateAccepterWalletRequest = new UpdateBootcoinWalletRequest();
                                            updateAccepterWalletRequest.balance(newAccepterWalletBalance);

                                            log.info("Updating bootcoin wallets");

                                            return Mono.zip(bootcoinService.updateBootcoinWallet(requestOwnerWallet.getId(), updateOwnerWalletRequest),
                                                    bootcoinService.updateBootcoinWallet(requestAccepterWallet.getId(), updateAccepterWalletRequest))
                                                .doOnSuccess(__ -> {
                                                    log.info("Bootcoin wallets updated successfully");
                                                    UpdateExchangeRequestRequest updateExchangeRequestRequest = new UpdateExchangeRequestRequest();
                                                    updateExchangeRequestRequest.status(ExchangeRequestStatus.APPROVED.toString());
                                                    updateExchangeRequestRequest.setMessage("Bootcoin wallets updated successfully");

                                                    bootcoinService.updateExchangeRequest(exchangeRequest.getId(), updateExchangeRequestRequest)
                                                        .doOnError(t -> log.error("Error updating exchange request", t));
                                                })
                                                .doOnError(__ -> {
                                                    log.error("Bootcoin wallets update failed", __);
                                                    UpdateExchangeRequestRequest updateExchangeRequestRequest = new UpdateExchangeRequestRequest();
                                                    updateExchangeRequestRequest.status(ExchangeRequestStatus.REJECTED.toString());
                                                    updateExchangeRequestRequest.setMessage("Bootcoin wallets update failed");

                                                    bootcoinService.updateExchangeRequest(exchangeRequest.getId(), updateExchangeRequestRequest)
                                                        .doOnError(t -> log.error("Error updating exchange request", t));
                                                });
                                        });
                                }
                            }
                        }

                        log.error("Error updating bootcoin wallet");

                        return Mono.error(new BadRequestException("Wallet balance is null"));
                    });
            })
            .then();
    }
}
