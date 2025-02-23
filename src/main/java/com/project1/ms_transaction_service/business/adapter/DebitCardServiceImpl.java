package com.project1.ms_transaction_service.business.adapter;

import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.exception.NotFoundException;
import com.project1.ms_transaction_service.model.AccountResponse;
import com.project1.ms_transaction_service.model.CreditResponse;
import com.project1.ms_transaction_service.model.DebitCardResponse;
import com.project1.ms_transaction_service.model.ResponseBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class DebitCardServiceImpl implements DebitCardService {

    @Autowired
    @Qualifier("accountWebClient")
    private WebClient accountWebClient;

    @Override
    public Mono<DebitCardResponse> getDebitCardById(String debitCardId) {
        return accountWebClient.get()
            .uri("/debit-card/{debitCardId}", debitCardId)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, response ->
                response.bodyToMono(ResponseBase.class)
                    .flatMap(error -> Mono.error(
                        response.statusCode().equals(HttpStatus.NOT_FOUND)
                            ? new NotFoundException(error.getMessage())
                            : new BadRequestException(error.getMessage())
                    ))
            )
            .bodyToMono(DebitCardResponse.class);
    }
}
