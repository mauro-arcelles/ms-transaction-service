package com.project1.ms_transaction_service.business.adapter;

import com.project1.ms_transaction_service.model.CreateBootcoinWalletResponse;
import com.project1.ms_transaction_service.model.GetExchangeRequestByTransactionIdResponse;
import com.project1.ms_transaction_service.model.UpdateBootcoinWalletRequest;
import com.project1.ms_transaction_service.model.UpdateExchangeRequestRequest;
import reactor.core.publisher.Mono;

public interface BootcoinService {
    Mono<GetExchangeRequestByTransactionIdResponse> getExchangeRequestByTransactionId(String transactionId);

    Mono<CreateBootcoinWalletResponse> getBootcoinWalletByUserId(String userId);

    Mono<Void> updateBootcoinWallet(String walletId, UpdateBootcoinWalletRequest request);

    Mono<Void> updateExchangeRequest(String exchangeRequestId, UpdateExchangeRequestRequest request);
}
