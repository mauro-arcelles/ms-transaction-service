package com.project1.ms_transaction_service.business.service;

import com.project1.ms_transaction_service.business.adapter.CreditService;
import com.project1.ms_transaction_service.business.mapper.CreditTransactionMapper;
import com.project1.ms_transaction_service.model.CreditPatchRequest;
import com.project1.ms_transaction_service.model.CreditPaymentTransactionRequest;
import com.project1.ms_transaction_service.model.CreditPaymentTransactionResponse;
import com.project1.ms_transaction_service.model.CreditResponse;
import com.project1.ms_transaction_service.model.entity.CreditTransaction;
import com.project1.ms_transaction_service.repository.CreditTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;

@Service
@Slf4j
public class CreditTransactionServiceImpl implements CreditTransactionService {

    @Autowired
    CreditService creditService;

    @Autowired
    CreditTransactionMapper creditTransactionMapper;

    @Autowired
    CreditTransactionRepository creditTransactionRepository;

    @Override
    public Mono<CreditPaymentTransactionResponse> createCreditPaymentTransaction(Mono<CreditPaymentTransactionRequest> request) {
        return request
                .flatMap(this::validateAndGetCredit)
                .flatMap(this::createCreditTransaction)
                .flatMap(this::saveCreditTransaction)
                .flatMap(this::updateCreditAmount)
                .map(creditTransactionMapper::getCreditPaymentTransactionResponse);
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
     * Updates the credit amount paid after transaction.
     *
     * @param tuple Tuple containing credit response and saved transaction
     * @return Mono containing the updated credit transaction
     */
    private Mono<CreditTransaction> updateCreditAmount(Tuple2<CreditResponse, CreditTransaction> tuple) {
        CreditResponse creditResponse = tuple.getT1();
        CreditTransaction creditTransaction = tuple.getT2();

        if (creditResponse.getAmountPaid() != null) {
            CreditPatchRequest patchRequest = new CreditPatchRequest();
            BigDecimal newAmountPaid = creditResponse.getAmountPaid().add(creditResponse.getMonthlyPayment());
            patchRequest.setAmountPaid(newAmountPaid);

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
}
