package com.project1.ms_transaction_service.business.service;

import com.project1.ms_transaction_service.business.adapter.AccountService;
import com.project1.ms_transaction_service.business.adapter.DebitCardService;
import com.project1.ms_transaction_service.business.mapper.AccountTransactionMapper;
import com.project1.ms_transaction_service.business.mapper.DebitCardTransactionMapper;
import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.model.*;
import com.project1.ms_transaction_service.model.entity.DebitCardTransaction;
import com.project1.ms_transaction_service.repository.DebitCardTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DebitCardTransactionServiceImpl implements DebitCardTransactionService {
    @Autowired
    private DebitCardTransactionRepository debitCardTransactionRepository;

    @Autowired
    private DebitCardService debitCardService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private DebitCardTransactionMapper debitCardTransactionMapper;

    @Autowired
    private AccountTransactionMapper accountTransactionMapper;

    @Override
    public Mono<DebitCardTransactionResponse> createDebitCardTransaction(Mono<DebitCardTransactionRequest> request) {
        // 1. Validate if the debit card exists and obtain it
        // 2. Obtain the accountId with the highest position of those associated with the debit card
        // 3. Obtain the account with the obtained accountId
        // 4. Verify if the account has ACTIVE status
        // 5. Verify if the account has sufficient balance
        return request.flatMap(this::validateAndGetCreditCard)
            .flatMap(tuple -> validateAndGetAccount(tuple, new ArrayList<>()))
            .flatMap(this::processTransaction)
            .map(tuple -> debitCardTransactionMapper.getDebitCardTransactionResponse(tuple.getT2()));
    }

    private Mono<Tuple2<DebitCardTransactionRequest, DebitCardResponse>> validateAndGetCreditCard(DebitCardTransactionRequest request) {
        return debitCardService.getDebitCardById(request.getDebitCardId())
            .map(card -> Tuples.of(request, card));
    }

    private Mono<Tuple2<DebitCardTransactionRequest, AccountResponse>> validateAndGetAccount(Tuple2<DebitCardTransactionRequest, DebitCardResponse> tuple,
                                                                                             List<String> triedAccounts) {
        DebitCardTransactionRequest request = tuple.getT1();
        DebitCardResponse debitCard = tuple.getT2();
        if (debitCard.getAssociations().isEmpty()) {
            throw new BadRequestException("Cannot process the transaction. No account associated with the debit card");
        }

        Optional<DebitCardAssociation> association = debitCard.getAssociations().stream()
            .filter(a -> triedAccounts.isEmpty() || !triedAccounts.contains(a.getAccountId()))
            .min(Comparator.comparingInt(DebitCardAssociation::getPosition));

        if (association.isEmpty()) {
            throw new BadRequestException("Cannot process the transaction. All associated accounts have insufficient funds");
        }

        String accountId = association.get().getAccountId();
        triedAccounts.add(accountId);

        return accountService.getAccountById(accountId)
            .flatMap(account -> {
                if (request.getAmount().compareTo(account.getBalance()) > 0) {
                    log.info("Account with id {} has sufficient balance. Will retry transaction with next associated account", account.getId());
                    return validateAndGetAccount(tuple, triedAccounts);
                }
                return Mono.just(Tuples.of(tuple.getT1(), account));
            });
    }

    private Mono<Tuple2<DebitCardTransactionRequest, DebitCardTransaction>> processTransaction(Tuple2<DebitCardTransactionRequest, AccountResponse> tuple) {
        DebitCardTransactionRequest request = tuple.getT1();
        AccountResponse account = tuple.getT2();

        DebitCardTransaction transaction = debitCardTransactionMapper.getDebitCardTransactionEntity(request);
        transaction.setAccountId(account.getId());
        transaction.setCustomerId(account.getCustomerId());

        return debitCardTransactionRepository.save(transaction)
            .flatMap(debitCardTransaction -> {
                if (account.getBalance() != null) {
                    BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
                    AccountPatchRequest accountPatchRequest = new AccountPatchRequest();
                    accountPatchRequest.setBalance(newBalance);
                    return accountService.updateAccount(account.getId(), accountPatchRequest)
                        .thenReturn(Tuples.of(request, debitCardTransaction));
                }
                return Mono.just(Tuples.of(request, debitCardTransaction));
            });
    }

}
