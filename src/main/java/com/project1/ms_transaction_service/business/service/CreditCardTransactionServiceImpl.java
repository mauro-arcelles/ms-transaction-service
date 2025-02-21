package com.project1.ms_transaction_service.business.service;

import com.project1.ms_transaction_service.business.adapter.CreditCardService;
import com.project1.ms_transaction_service.business.mapper.CreditCardTransactionMapper;
import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.exception.CreditCardCustomerMismatchException;
import com.project1.ms_transaction_service.model.*;
import com.project1.ms_transaction_service.model.entity.CreditCardTransaction;
import com.project1.ms_transaction_service.repository.CreditCardTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
public class CreditCardTransactionServiceImpl implements CreditCardTransactionService {

    @Autowired
    CreditCardTransactionMapper creditCardTransactionMapper;

    @Autowired
    CreditCardService creditCardService;

    @Autowired
    CreditCardTransactionRepository creditCardTransactionRepository;

    @Override
    public Mono<CreditCardTransactionResponse> createCreditCardUsageTransaction(Mono<CreditCardTransactionRequest> request) {
        return request
            .flatMap(this::validateAndGetCreditCard)
            .flatMap(this::validateCreditCardUsageLimit)
            .flatMap(this::processUsageTransaction)
            .map(creditCardTransactionMapper::getCreditCardTransactionResponse);
    }

    @Override
    public Mono<CreditCardTransactionResponse> createCreditCardPaymentTransaction(Mono<CreditCardTransactionRequest> request) {
        return request
            .flatMap(this::validateAndGetCreditCard)
            .flatMap(this::validateCreditCardPaymentLimit)
            .flatMap(this::processPaymentTransaction)
            .map(creditCardTransactionMapper::getCreditCardTransactionResponse);
    }

    @Override
    public Flux<CreditCardTransactionResponse> getCreditCardTransactionsByCardNumber(String originAccountNumber) {
        return creditCardService.getCreditCardByCardNumber(originAccountNumber)
            .flatMapMany(account ->
                creditCardTransactionRepository.findAllByCreditCard(originAccountNumber)
                    .map(creditCardTransactionMapper::getCreditCardTransactionResponse)
            );
    }

    /**
     * Validates that the credit card exists and belongs to the customer
     *
     * @param request The transaction request containing card and customer details
     * @return Tuple of request and card response if valid
     * @throws CreditCardCustomerMismatchException if card doesn't belong to customer
     */
    private Mono<Tuple2<CreditCardTransactionRequest, CreditCardResponse>> validateAndGetCreditCard(CreditCardTransactionRequest request) {
        return creditCardService.getCreditCardByCardNumber(request.getCreditCard())
            .filter(card -> Optional.ofNullable(card.getCustomerId())
                .map(id -> id.equals(request.getCustomerId()))
                .orElse(false))
            .switchIfEmpty(Mono.error(new CreditCardCustomerMismatchException()))
            .map(card -> Tuples.of(request, card));
    }

    /**
     * Validates if the transaction amount is within the available credit limit
     *
     * @param tuple Contains the transaction request and card details
     * @return The input tuple if validation passes
     * @throws BadRequestException if insufficient funds available
     */
    private Mono<Tuple2<CreditCardTransactionRequest, CreditCardResponse>> validateCreditCardUsageLimit(
        Tuple2<CreditCardTransactionRequest, CreditCardResponse> tuple) {
        CreditCardTransactionRequest request = tuple.getT1();
        CreditCardResponse card = tuple.getT2();

        if (card.getUsedAmount() != null) {
            BigDecimal newAmount = card.getUsedAmount().add(request.getAmount());
            if (newAmount.compareTo(card.getCreditLimit()) > 0) {
                return Mono.error(new BadRequestException("Cannot complete the transaction. CREDIT CARD has reached its total amount limit"));
            }
        }

        return Mono.just(tuple);
    }

    /**
     * Validates if the transaction amount is within the available used amount
     *
     * @param tuple Contains the transaction request and card details
     * @return The input tuple if validation passes
     * @throws BadRequestException if the amount to pay is more than the used amount
     */
    private Mono<Tuple2<CreditCardTransactionRequest, CreditCardResponse>> validateCreditCardPaymentLimit(
        Tuple2<CreditCardTransactionRequest, CreditCardResponse> tuple) {
        CreditCardTransactionRequest request = tuple.getT1();
        CreditCardResponse card = tuple.getT2();

        if (card.getUsedAmount() != null) {
            if (request.getAmount().compareTo(card.getUsedAmount()) > 0) {
                return Mono.error(new BadRequestException("Cannot complete the transaction. Amount to pay is more than the actual CREDIT CARD used amount"));
            }
        }

        return Mono.just(tuple);
    }

    /**
     * Saves the transaction and updates the card's used amount
     *
     * @param tuple Contains the transaction request and card details
     * @return The saved credit card transaction
     */
    private Mono<CreditCardTransaction> processUsageTransaction(Tuple2<CreditCardTransactionRequest, CreditCardResponse> tuple) {
        CreditCardTransactionRequest request = tuple.getT1();
        CreditCardResponse card = tuple.getT2();

        return creditCardTransactionRepository.save(creditCardTransactionMapper.getCreditCardUsageTransactionEntity(request))
            .flatMap(transaction -> {
                BigDecimal newAmount = card.getUsedAmount();
                if (newAmount != null) {
                    newAmount = card.getUsedAmount().add(request.getAmount());
                }
                return updateCreditCardAmount(card, newAmount).thenReturn(transaction);
            });
    }

    /**
     * Saves the transaction and updates the card's used amount
     *
     * @param tuple Contains the transaction request and card details
     * @return The saved credit card transaction
     */
    private Mono<CreditCardTransaction> processPaymentTransaction(Tuple2<CreditCardTransactionRequest, CreditCardResponse> tuple) {
        CreditCardTransactionRequest request = tuple.getT1();
        CreditCardResponse card = tuple.getT2();

        return creditCardTransactionRepository.save(creditCardTransactionMapper.getCreditCardPaymentTransactionEntity(request))
            .flatMap(transaction -> {
                BigDecimal newAmount = card.getUsedAmount();
                if (newAmount != null) {
                    newAmount = card.getUsedAmount().subtract(request.getAmount());
                }
                return updateCreditCardAmount(card, newAmount).thenReturn(transaction);
            });
    }

    /**
     * Updates the used amount of a credit card by adding the specified amount.
     *
     * @param card   The credit card response object containing current card information
     * @param newUsedAmount The new used amount of the credit card
     * @return A Mono containing the updated credit card response
     */
    private Mono<CreditCardResponse> updateCreditCardAmount(CreditCardResponse card, BigDecimal newUsedAmount) {
        CreditCardPatchRequest patchRequest = new CreditCardPatchRequest();
        if (card.getUsedAmount() != null) {
            patchRequest.setUsedAmount(newUsedAmount);
        }
        return creditCardService.updateCreditCard(card.getId(), patchRequest);
    }
}
