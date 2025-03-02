package com.project1.ms_transaction_service.business.service.impl;

import com.project1.ms_transaction_service.business.adapter.CreditCardService;
import com.project1.ms_transaction_service.business.adapter.CustomerService;
import com.project1.ms_transaction_service.business.mapper.CreditCardTransactionMapper;
import com.project1.ms_transaction_service.business.service.CreditCardTransactionService;
import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.exception.CreditCardCustomerMismatchException;
import com.project1.ms_transaction_service.model.*;
import com.project1.ms_transaction_service.model.entity.CreditCardTransaction;
import com.project1.ms_transaction_service.model.entity.CreditCardTransactionType;
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
    private CreditCardTransactionMapper creditCardTransactionMapper;

    @Autowired
    private CreditCardService creditCardService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CreditCardTransactionRepository creditCardTransactionRepository;

    @Override
    public Mono<CreditCardTransactionResponse> createCreditCardTransaction(Mono<CreditCardTransactionRequest> request) {
        return request
            .flatMap(this::validateAndGetCreditCard)
            .flatMap(this::validateCustomer)
            .flatMap(this::validateCreditCardUsageLimit)
            .flatMap(tuple -> {
                CreditCardTransactionRequest req = tuple.getT1();
                if (CreditCardTransactionType.USAGE.toString().equals(req.getType())) {
                    return processUsageTransaction(tuple);
                } else {
                    return processPaymentTransaction(tuple);
                }
            })
            .map(creditCardTransactionMapper::getCreditCardTransactionResponse);
    }

    @Override
    public Flux<CreditCardTransactionResponse> getCreditCardTransactionsById(String originAccountId) {
        return creditCardService.getCreditCardById(originAccountId)
            .flatMapMany(account ->
                creditCardTransactionRepository.findAllByCreditCardId(originAccountId)
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
        return creditCardService.getCreditCardById(request.getCreditCardId())
            .map(card -> Tuples.of(request, card));
    }

    /**
     * Validates customer based on transaction type:
     * - For PAYMENT: Verifies customer existence
     * - For USAGE: Validates if customer owns the credit card
     *
     * @param tuple Contains transaction request and credit card response
     * @return Mono of validated tuple
     * @throws CreditCardCustomerMismatchException if customer is not card owner for usage transaction
     */
    private Mono<Tuple2<CreditCardTransactionRequest, CreditCardResponse>> validateCustomer(
        Tuple2<CreditCardTransactionRequest, CreditCardResponse> tuple) {
        CreditCardTransactionRequest request = tuple.getT1();
        CreditCardResponse card = tuple.getT2();

        // if its payment transaction validate if the customer exists
        if (CreditCardTransactionType.PAYMENT.toString().equals(request.getType())) {
            return customerService.getCustomerById(request.getCustomerId())
                .flatMap(c -> Mono.just(Tuples.of(request, card)));
        }

        // if its usage transaction validate if the given customer is the owner of the account
        Optional.ofNullable(card.getCustomerId())
            .filter(id -> !id.equals(request.getCustomerId()))
            .ifPresent(id -> {
                throw new CreditCardCustomerMismatchException();
            });

        return Mono.just(Tuples.of(request, card));
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
            if (CreditCardTransactionType.USAGE.toString().equals(request.getType())) {
                BigDecimal newAmount = card.getUsedAmount().add(request.getAmount());
                if (newAmount.compareTo(card.getCreditLimit()) > 0) {
                    return Mono.error(new BadRequestException("Cannot complete the transaction. CREDIT CARD has insufficient funds"));
                }
            } else {
                if (request.getAmount().compareTo(card.getUsedAmount()) > 0) {
                    return Mono.error(
                        new BadRequestException("Cannot complete the transaction. Amount to pay is more than the actual CREDIT CARD used amount"));
                }
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
     * @param card          The credit card response object containing current card information
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
