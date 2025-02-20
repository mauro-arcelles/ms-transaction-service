package com.project1.ms_transaction_service.business.service;

import com.project1.ms_transaction_service.business.adapter.CreditCardService;
import com.project1.ms_transaction_service.business.mapper.CreditCardTransactionMapper;
import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.exception.CreditCardCustomerMismatchException;
import com.project1.ms_transaction_service.model.CreditCardPatchRequest;
import com.project1.ms_transaction_service.model.CreditCardResponse;
import com.project1.ms_transaction_service.model.CreditCardUsageTransactionRequest;
import com.project1.ms_transaction_service.model.CreditCardUsageTransactionResponse;
import com.project1.ms_transaction_service.model.entity.CreditCardTransaction;
import com.project1.ms_transaction_service.repository.CreditCardTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    public Mono<CreditCardUsageTransactionResponse> createCreditCardUsageTransaction(Mono<CreditCardUsageTransactionRequest> request) {
        return request
            .flatMap(this::validateAndGetCreditCard)
            .flatMap(this::validateCreditCardLimit)
            .flatMap(this::processTransaction)
            .map(creditCardTransactionMapper::getCreditCardTransactionResponse);
    }

    /**
     * Validates that the credit card exists and belongs to the customer
     *
     * @param request The transaction request containing card and customer details
     * @return Tuple of request and card response if valid
     * @throws CreditCardCustomerMismatchException if card doesn't belong to customer
     */
    private Mono<Tuple2<CreditCardUsageTransactionRequest, CreditCardResponse>> validateAndGetCreditCard(CreditCardUsageTransactionRequest request) {
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
    private Mono<Tuple2<CreditCardUsageTransactionRequest, CreditCardResponse>> validateCreditCardLimit(
        Tuple2<CreditCardUsageTransactionRequest, CreditCardResponse> tuple) {
        CreditCardUsageTransactionRequest request = tuple.getT1();
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
     * Saves the transaction and updates the card's used amount
     *
     * @param tuple Contains the transaction request and card details
     * @return The saved credit card transaction
     */
    private Mono<CreditCardTransaction> processTransaction(Tuple2<CreditCardUsageTransactionRequest, CreditCardResponse> tuple) {
        CreditCardUsageTransactionRequest request = tuple.getT1();
        CreditCardResponse card = tuple.getT2();

        return creditCardTransactionRepository.save(creditCardTransactionMapper.getCreditCardTransactionEntity(request))
            .flatMap(transaction -> updateCreditCardAmount(card, request.getAmount())
                .thenReturn(transaction));
    }

    /**
     * Updates the used amount of a credit card by adding the specified amount.
     *
     * @param card   The credit card response object containing current card information
     * @param amount The amount to add to the card's used amount
     * @return A Mono containing the updated credit card response
     */
    private Mono<CreditCardResponse> updateCreditCardAmount(CreditCardResponse card, BigDecimal amount) {
        CreditCardPatchRequest patchRequest = new CreditCardPatchRequest();
        if (card.getUsedAmount() != null) {
            patchRequest.setUsedAmount(card.getUsedAmount().add(amount));
        }
        return creditCardService.updateCreditCard(card.getId(), patchRequest);
    }
}
