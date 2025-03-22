package com.project1.ms_transaction_service.business.service.impl;

import com.project1.ms_transaction_service.business.adapter.BootcoinService;
import com.project1.ms_transaction_service.business.adapter.YankiService;
import com.project1.ms_transaction_service.business.service.BootcoinTransactionService;
import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.model.*;
import com.project1.ms_transaction_service.model.entity.ExchangeRequestStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.math.BigDecimal;

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
                return Mono.zip(
                        bootcoinService.getBootcoinWalletByUserId(exchangeRequest.getRequestOwnerUserId()),
                        bootcoinService.getBootcoinWalletByUserId(exchangeRequest.getRequestAccepterUserId())
                    )
                    .flatMap(tuple -> {
                        CreateBootcoinWalletResponse requestOwnerWallet = tuple.getT1();
                        CreateBootcoinWalletResponse requestAccepterWallet = tuple.getT2();

                        // verify payment method within exchangeRequest to discount the amount in the respective payment method
                        Mono<Void> bootcoinService1 = verifyPaymenthMethodBalance(exchangeRequest, requestAccepterWallet);
                        if (bootcoinService1 != null) return bootcoinService1;

                        // update payment method balance
                        return updatePaymentMethodBalance(exchangeRequest, requestOwnerWallet, requestAccepterWallet);
                    });
            })
            .then();
    }

    private Mono<Tuple2<Void, Void>> updatePaymentMethodBalance(GetExchangeRequestByTransactionIdResponse exchangeRequest,
                                                                CreateBootcoinWalletResponse requestOwnerWallet,
                                                                CreateBootcoinWalletResponse requestAccepterWallet) {
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
                .flatMap(e -> updateBootcoinWallets(requestOwnerWallet, requestAccepterWallet, exchangeRequest));
        }
        return Mono.empty();
    }

    private Mono<Void> verifyPaymenthMethodBalance(GetExchangeRequestByTransactionIdResponse exchangeRequest,
                                                   CreateBootcoinWalletResponse requestAccepterWallet) {
        if (exchangeRequest.getAmount().compareTo(requestAccepterWallet.getBalance()) > 0) {
            log.error("Insufficient bootcoins balance in request accepter wallet");
            UpdateExchangeRequestRequest updateExchangeRequestRequest = new UpdateExchangeRequestRequest();
            updateExchangeRequestRequest.status(ExchangeRequestStatus.REJECTED.toString());
            updateExchangeRequestRequest.setMessage("Insufficient bootcoins in request accepter wallet");

            return bootcoinService.updateExchangeRequest(exchangeRequest.getId(), updateExchangeRequestRequest)
                .doOnError(e -> log.error("Error updating exchange request"));
        }
        return null;
    }

    public Mono<Tuple2<Void, Void>> updateBootcoinWallets(CreateBootcoinWalletResponse requestOwnerWallet, CreateBootcoinWalletResponse requestAccepterWallet,
                                                          GetExchangeRequestByTransactionIdResponse exchangeRequest) {
        BigDecimal newOwnerWalletBalance = requestOwnerWallet.getBalance().add(exchangeRequest.getAmount());
        BigDecimal newAccepterWalletBalance = requestAccepterWallet.getBalance().subtract(exchangeRequest.getAmount());

        UpdateBootcoinWalletRequest updateOwnerWalletRequest = new UpdateBootcoinWalletRequest();
        updateOwnerWalletRequest.balance(newOwnerWalletBalance);

        UpdateBootcoinWalletRequest updateAccepterWalletRequest = new UpdateBootcoinWalletRequest();
        updateAccepterWalletRequest.balance(newAccepterWalletBalance);

        log.info("Updating bootcoin wallets");

        return Mono.zip(
                bootcoinService.updateBootcoinWallet(requestOwnerWallet.getId(), updateOwnerWalletRequest)
                    .doOnSubscribe(__ -> log.info("Subscribing to update owner wallet"))
                    .doOnSuccess(__ -> log.info("Owner wallet updated successfully"))
                    .doOnError(e -> log.error("Error updating owner wallet", e)),
                bootcoinService.updateBootcoinWallet(requestAccepterWallet.getId(), updateAccepterWalletRequest)
                    .doOnSubscribe(__ -> log.info("Subscribing to update accepter wallet"))
                    .doOnSuccess(__ -> log.info("Accepter wallet updated successfully"))
                    .doOnError(e -> log.error("Error updating accepter wallet", e))
            )
            .doOnSubscribe(__ -> log.info("Subscribing to zip operation"))
            .doOnSuccess(__ -> log.info("Zip operation completed successfully"))
            .doOnError(e -> log.error("Error in zip operation", e))
            .doOnSuccess(tuple -> {
                log.info("Processing after successful wallet updates");
                UpdateExchangeRequestRequest updateExchangeRequestRequest = new UpdateExchangeRequestRequest();
                updateExchangeRequestRequest.status(ExchangeRequestStatus.APPROVED.toString());
                updateExchangeRequestRequest.setMessage("Bootcoin wallets updated successfully");

                bootcoinService.updateExchangeRequest(exchangeRequest.getId(), updateExchangeRequestRequest)
                    .doOnSubscribe(__ -> log.info("Subscribing to update exchange request"))
                    .doOnSuccess(e -> log.info("Exchange request updated successfully"))
                    .doOnError(t -> log.error("Error updating exchange request", t))
                    .subscribe();
            })
            .doOnError(error -> {
                log.error("Processing after wallet update failure", error);
                UpdateExchangeRequestRequest updateExchangeRequestRequest = new UpdateExchangeRequestRequest();
                updateExchangeRequestRequest.status(ExchangeRequestStatus.REJECTED.toString());
                updateExchangeRequestRequest.setMessage("Bootcoin wallets update failed");

                bootcoinService.updateExchangeRequest(exchangeRequest.getId(), updateExchangeRequestRequest)
                    .doOnSubscribe(__ -> log.info("Subscribing to update exchange request (error case)"))
                    .doOnSuccess(e -> log.info("Exchange request updated successfully (error case)"))
                    .doOnError(t -> log.error("Error updating exchange request (error case)", t))
                    .subscribe();
            });
    }
}
