package com.project1.ms_transaction_service.business.adapter;

import com.project1.ms_transaction_service.model.GetYankiWalletResponse;
import com.project1.ms_transaction_service.model.UpdateYankiWalletRequest;
import reactor.core.publisher.Mono;

public interface YankiService {
    Mono<GetYankiWalletResponse> getYankiWallet(String id);

    Mono<Void> updateYankiWallet(String id, UpdateYankiWalletRequest request);
}
