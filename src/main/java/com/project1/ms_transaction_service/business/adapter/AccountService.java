package com.project1.ms_transaction_service.business.adapter;

import com.project1.ms_transaction_service.model.AccountPatchRequest;
import com.project1.ms_transaction_service.model.AccountResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {
    Mono<AccountResponse> getAccountByAccountNumber(String accountNumber);

    Mono<AccountResponse> updateAccount(String id, AccountPatchRequest request);

    Flux<AccountResponse> getAccountsByCustomerId(String customerId);
}
