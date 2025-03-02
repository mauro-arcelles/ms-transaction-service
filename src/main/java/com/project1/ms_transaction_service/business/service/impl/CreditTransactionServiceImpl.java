package com.project1.ms_transaction_service.business.service.impl;

import com.project1.ms_transaction_service.business.adapter.CreditService;
import com.project1.ms_transaction_service.business.adapter.CustomerService;
import com.project1.ms_transaction_service.business.mapper.CreditTransactionMapper;
import com.project1.ms_transaction_service.business.service.CreditTransactionService;
import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.model.CreditPatchRequest;
import com.project1.ms_transaction_service.model.CreditPaymentTransactionRequest;
import com.project1.ms_transaction_service.model.CreditPaymentTransactionResponse;
import com.project1.ms_transaction_service.model.CreditResponse;
import com.project1.ms_transaction_service.model.entity.CreditTransaction;
import com.project1.ms_transaction_service.repository.CreditTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;

@Service
@Slf4j
public class CreditTransactionServiceImpl implements CreditTransactionService {

    @Autowired
    private CreditService creditService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CreditTransactionMapper creditTransactionMapper;

    @Autowired
    private CreditTransactionRepository creditTransactionRepository;

    @Override
    public Mono<CreditPaymentTransactionResponse> createCreditPaymentTransaction(Mono<CreditPaymentTransactionRequest> request) {
        return request.flatMap(this::validateAndGetCredit)
            .flatMap(this::validateCustomer)
            .flatMap(this::createCreditTransaction)
            .flatMap(this::saveCreditTransaction)
            .flatMap(this::updateCredit)
            .map(creditTransactionMapper::getCreditPaymentTransactionResponse);
    }

    @Override
    public Flux<CreditPaymentTransactionResponse> getCreditTransactionsByCreditId(String creditId) {
        return creditService.getCreditById(creditId)
            .flatMapMany(account ->
                creditTransactionRepository.findAllByCreditId(creditId)
                    .map(creditTransactionMapper::getCreditPaymentTransactionResponse)
            );
    }

    /**
     * Creates a credit transaction entity from validated credit and request.
     *
     * @param tuple Tuple containing credit response and request
     * @return Mono containing tuple of credit response and transaction entity
     */
    private Mono<Tuple2<CreditResponse, CreditTransaction>> createCreditTransaction(Tuple2<CreditResponse, CreditPaymentTransactionRequest> tuple) {
        CreditResponse creditResponse = tuple.getT1();
        CreditPaymentTransactionRequest request = tuple.getT2();
        CreditTransaction transaction = creditTransactionMapper.getCreditPaymentTransactionEntity(request);
        if (creditResponse.getAmountPaid() != null) {
            BigDecimal newAmountPaid = creditResponse.getAmountPaid().add(creditResponse.getMonthlyPayment());
            if (newAmountPaid.compareTo(creditResponse.getTotalAmount()) > 0) {
                throw new BadRequestException("Cannot complete transaction. CREDIT has been fully paid");
            }
        }
        return Mono.just(Tuples.of(creditResponse, transaction));
    }

    /**
     * Saves the credit transaction to repository.
     *
     * @param tuple Tuple containing credit response and transaction entity
     * @return Mono containing tuple of credit response and saved transaction
     */
    private Mono<Tuple2<CreditResponse, CreditTransaction>> saveCreditTransaction(Tuple2<CreditResponse, CreditTransaction> tuple) {
        return creditTransactionRepository.save(tuple.getT2())
            .map(savedTransaction -> Tuples.of(tuple.getT1(), savedTransaction));
    }

    /**
     * Updates the credit amount paid, nextPaymentDueDate and expectedPaymentToDate after transaction.
     *
     * @param tuple Tuple containing credit response and saved transaction
     * @return Mono containing the updated credit transaction
     */
    private Mono<CreditTransaction> updateCredit(Tuple2<CreditResponse, CreditTransaction> tuple) {
        CreditResponse creditResponse = tuple.getT1();
        CreditTransaction creditTransaction = tuple.getT2();

        if (creditResponse.getAmountPaid() != null) {
            CreditPatchRequest patchRequest = new CreditPatchRequest();
            BigDecimal newAmountPaid = creditResponse.getAmountPaid().add(creditResponse.getMonthlyPayment());
            patchRequest.setAmountPaid(newAmountPaid);
            if (creditResponse.getNextPaymentDueDate() != null) {
                patchRequest.setNextPaymentDueDate(creditResponse.getNextPaymentDueDate().plusMonths(1));
            }
            if (creditResponse.getExpectedPaymentToDate() != null) {
                patchRequest.setExpectedPaymentToDate(creditResponse.getExpectedPaymentToDate().add(creditResponse.getMonthlyPayment()));
            }
            return creditService.updateCreditById(creditTransaction.getCreditId(), patchRequest)
                .map(cr -> creditTransaction);
        }
        return Mono.just(creditTransaction);
    }

    /**
     * Validates credit existence and pairs it with the payment transaction request.
     *
     * @param request The credit payment transaction request containing the credit ID
     * @return A Mono containing a tuple of credit response and the original request
     * @throws com.project1.ms_transaction_service.exception.BadRequestException if credit not found
     */
    private Mono<Tuple2<CreditResponse, CreditPaymentTransactionRequest>> validateAndGetCredit(CreditPaymentTransactionRequest request) {
        return creditService.getCreditById(request.getCreditId())
            .map(creditResponse -> Tuples.of(creditResponse, request));
    }

    /**
     * Validates customer existence by ID
     *
     * @param tuple Contains credit response and payment request
     * @return Mono of validated tuple
     */
    private Mono<Tuple2<CreditResponse, CreditPaymentTransactionRequest>> validateCustomer(Tuple2<CreditResponse, CreditPaymentTransactionRequest> tuple) {
        CreditResponse creditResponse = tuple.getT1();
        CreditPaymentTransactionRequest request = tuple.getT2();
        return customerService.getCustomerById(request.getCustomerId())
            .map(customerResponse -> Tuples.of(creditResponse, request));
    }
}
