package com.project1.ms_transaction_service.business.adapter;

import com.project1.ms_transaction_service.model.CreditCardPatchRequest;
import com.project1.ms_transaction_service.model.CreditCardResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@SpringBootTest
class CreditCardServiceImplTest {

    @MockBean
    @Qualifier("creditWebClient")
    private WebClient creditWebClient;

    @Autowired
    private CreditCardServiceImpl creditCardService;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Test
    void getCreditCardByCardNumber_Success() {
        String cardNumber = "1234";
        CreditCardResponse response = new CreditCardResponse();

        when(creditWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/credit-card/by-card-number/{cardNumber}", cardNumber))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CreditCardResponse.class))
            .thenReturn(Mono.just(response));

        StepVerifier.create(creditCardService.getCreditCardByCardNumber(cardNumber))
            .expectNext(response)
            .verifyComplete();
    }

    @Test
    void updateCreditCard_Success() {
        String id = "123";
        CreditCardPatchRequest request = new CreditCardPatchRequest();
        CreditCardResponse response = new CreditCardResponse();

        when(creditWebClient.patch()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/credit-card/{id}", id)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(), eq(CreditCardPatchRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CreditCardResponse.class)).thenReturn(Mono.just(response));

        StepVerifier.create(creditCardService.updateCreditCard(id, request))
            .expectNext(response)
            .verifyComplete();
    }

    @Test
    void getCreditCardsByCustomerId_Success() {
        String customerId = "123";
        CreditCardResponse response = new CreditCardResponse();

        when(creditWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/credit-card/by-customer/{customerId}", customerId))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(CreditCardResponse.class))
            .thenReturn(Flux.just(response));

        StepVerifier.create(creditCardService.getCreditCardsByCustomerId(customerId))
            .expectNext(response)
            .verifyComplete();
    }

    @Test
    void getCreditCardById_Success() {
        String creditCardId = "123";
        CreditCardResponse response = new CreditCardResponse();

        when(creditWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/credit-card/{creditCardId}", creditCardId))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CreditCardResponse.class))
            .thenReturn(Mono.just(response));

        StepVerifier.create(creditCardService.getCreditCardById(creditCardId))
            .expectNext(response)
            .verifyComplete();
    }
}
