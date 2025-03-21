package com.project1.ms_transaction_service.business.service.impl;

import com.project1.ms_transaction_service.business.adapter.BootcoinService;
import com.project1.ms_transaction_service.business.adapter.YankiService;
import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.model.*;
import com.project1.ms_transaction_service.model.entity.ExchangeRequestStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class BootcoinTransactionServiceImplTest {

    @Autowired
    private BootcoinTransactionServiceImpl bootcoinTransactionService;

    @MockBean
    private BootcoinService bootcoinService;

    @MockBean
    private YankiService yankiService;

    @Test
    void processBootcoinTransaction_Success() {
        String transactionId = "tx123";

        GetExchangeRequestByTransactionIdResponse exchangeRequest = new GetExchangeRequestByTransactionIdResponse();
        exchangeRequest.setId(transactionId);
        exchangeRequest.setRequestOwnerUserId("user1");
        exchangeRequest.setRequestAccepterUserId("user2");
        exchangeRequest.setPaymentMethod("YANKI");
        exchangeRequest.setAmount(new BigDecimal("5"));
        exchangeRequest.setBuyRate(100.0);

        CreateBootcoinWalletResponse ownerBootcoinWallet = new CreateBootcoinWalletResponse();
        ownerBootcoinWallet.setId("wallet1");
        ownerBootcoinWallet.setBalance(new BigDecimal("10"));

        CreateBootcoinWalletResponse accepterBootcoinWallet = new CreateBootcoinWalletResponse();
        accepterBootcoinWallet.setId("wallet2");
        accepterBootcoinWallet.setBalance(new BigDecimal("20"));

        GetYankiWalletResponse ownerYankiWallet = new GetYankiWalletResponse();
        ownerYankiWallet.setId("yankiWallet1");
        ownerYankiWallet.setUserId("user1");
        ownerYankiWallet.setBalance(new BigDecimal("1000"));

        when(bootcoinService.getExchangeRequestByTransactionId(transactionId))
            .thenReturn(Mono.just(exchangeRequest));
        when(bootcoinService.getBootcoinWalletByUserId("user1"))
            .thenReturn(Mono.just(ownerBootcoinWallet));
        when(bootcoinService.getBootcoinWalletByUserId("user2"))
            .thenReturn(Mono.just(accepterBootcoinWallet));
        when(yankiService.getYankiWalletByUserId("user1"))
            .thenReturn(Mono.just(ownerYankiWallet));
        when(yankiService.updateYankiWallet(anyString(), any()))
            .thenReturn(Mono.empty());
        when(bootcoinService.updateBootcoinWallet(anyString(), any()))
            .thenReturn(Mono.empty());
        when(bootcoinService.updateExchangeRequest(anyString(), any()))
            .thenReturn(Mono.empty());

        StepVerifier.create(bootcoinTransactionService.processBootcoinTransaction(transactionId))
            .verifyComplete();

        verify(bootcoinService).getExchangeRequestByTransactionId(transactionId);
        verify(bootcoinService).getBootcoinWalletByUserId("user1");
        verify(bootcoinService).getBootcoinWalletByUserId("user2");
        verify(yankiService).getYankiWalletByUserId("user1");
        verify(yankiService).updateYankiWallet(eq("yankiWallet1"), any());
        verify(bootcoinService).updateBootcoinWallet(eq("wallet1"), argThat(req ->
            req.getBalance().compareTo(new BigDecimal("15")) == 0));
        verify(bootcoinService).updateBootcoinWallet(eq("wallet2"), argThat(req ->
            req.getBalance().compareTo(new BigDecimal("15")) == 0));
        verify(bootcoinService).updateExchangeRequest(eq(transactionId), argThat(req ->
            req.getStatus().equals(ExchangeRequestStatus.APPROVED.toString())));
    }

    @Test
    void processBootcoinTransaction_Success2() {
        String transactionId = "tx123";

        GetExchangeRequestByTransactionIdResponse exchangeRequest = new GetExchangeRequestByTransactionIdResponse();
        exchangeRequest.setId(transactionId);
        exchangeRequest.setRequestOwnerUserId("user1");
        exchangeRequest.setRequestAccepterUserId("user2");
        exchangeRequest.setPaymentMethod("TRANSFER");
        exchangeRequest.setAmount(new BigDecimal("5"));
        exchangeRequest.setBuyRate(100.0);

        CreateBootcoinWalletResponse ownerBootcoinWallet = new CreateBootcoinWalletResponse();
        ownerBootcoinWallet.setId("wallet1");
        ownerBootcoinWallet.setBalance(new BigDecimal("10"));

        CreateBootcoinWalletResponse accepterBootcoinWallet = new CreateBootcoinWalletResponse();
        accepterBootcoinWallet.setId("wallet2");
        accepterBootcoinWallet.setBalance(new BigDecimal("20"));

        GetYankiWalletResponse ownerYankiWallet = new GetYankiWalletResponse();
        ownerYankiWallet.setId("yankiWallet1");
        ownerYankiWallet.setUserId("user1");
        ownerYankiWallet.setBalance(new BigDecimal("1000"));

        when(bootcoinService.getExchangeRequestByTransactionId(transactionId))
            .thenReturn(Mono.just(exchangeRequest));
        when(bootcoinService.getBootcoinWalletByUserId("user1"))
            .thenReturn(Mono.just(ownerBootcoinWallet));
        when(bootcoinService.getBootcoinWalletByUserId("user2"))
            .thenReturn(Mono.just(accepterBootcoinWallet));
        when(yankiService.getYankiWalletByUserId("user1"))
            .thenReturn(Mono.just(ownerYankiWallet));
        when(yankiService.updateYankiWallet(anyString(), any()))
            .thenReturn(Mono.empty());
        when(bootcoinService.updateBootcoinWallet(anyString(), any()))
            .thenReturn(Mono.empty());
        when(bootcoinService.updateExchangeRequest(anyString(), any()))
            .thenReturn(Mono.empty());

        StepVerifier.create(bootcoinTransactionService.processBootcoinTransaction(transactionId))
            .verifyComplete();

        verify(bootcoinService).getExchangeRequestByTransactionId(transactionId);
        verify(bootcoinService).getBootcoinWalletByUserId("user1");
        verify(bootcoinService).getBootcoinWalletByUserId("user2");
    }

    @Test
    void processBootcoinTransaction_InsufficientBootcoinBalance() {
        String transactionId = "tx123";

        GetExchangeRequestByTransactionIdResponse exchangeRequest = new GetExchangeRequestByTransactionIdResponse();
        exchangeRequest.setId(transactionId);
        exchangeRequest.setRequestOwnerUserId("user1");
        exchangeRequest.setRequestAccepterUserId("user2");
        exchangeRequest.setPaymentMethod("YANKI");
        exchangeRequest.setAmount(new BigDecimal("30"));
        exchangeRequest.setBuyRate(100.0);

        CreateBootcoinWalletResponse ownerBootcoinWallet = new CreateBootcoinWalletResponse();
        ownerBootcoinWallet.setId("wallet1");
        ownerBootcoinWallet.setBalance(new BigDecimal("10"));

        CreateBootcoinWalletResponse accepterBootcoinWallet = new CreateBootcoinWalletResponse();
        accepterBootcoinWallet.setId("wallet2");
        accepterBootcoinWallet.setBalance(new BigDecimal("20"));

        when(bootcoinService.getExchangeRequestByTransactionId(transactionId))
            .thenReturn(Mono.just(exchangeRequest));
        when(bootcoinService.getBootcoinWalletByUserId("user1"))
            .thenReturn(Mono.just(ownerBootcoinWallet));
        when(bootcoinService.getBootcoinWalletByUserId("user2"))
            .thenReturn(Mono.just(accepterBootcoinWallet));
        when(bootcoinService.updateExchangeRequest(anyString(), any()))
            .thenReturn(Mono.empty());

        StepVerifier.create(bootcoinTransactionService.processBootcoinTransaction(transactionId))
            .verifyComplete();

        verify(bootcoinService).updateExchangeRequest(eq(transactionId), argThat(req ->
            req.getStatus().equals(ExchangeRequestStatus.REJECTED.toString()) &&
                req.getMessage().contains("Insufficient bootcoins")));
        verify(yankiService, never()).getYankiWalletByUserId(anyString());
        verify(bootcoinService, never()).updateBootcoinWallet(anyString(), any());
    }

    @Test
    void processBootcoinTransaction_InsufficientYankiBalance() {
        String transactionId = "tx123";

        GetExchangeRequestByTransactionIdResponse exchangeRequest = new GetExchangeRequestByTransactionIdResponse();
        exchangeRequest.setId(transactionId);
        exchangeRequest.setRequestOwnerUserId("user1");
        exchangeRequest.setRequestAccepterUserId("user2");
        exchangeRequest.setPaymentMethod("YANKI");
        exchangeRequest.setAmount(new BigDecimal("5"));
        exchangeRequest.setBuyRate(1000.0);

        CreateBootcoinWalletResponse ownerBootcoinWallet = new CreateBootcoinWalletResponse();
        ownerBootcoinWallet.setId("wallet1");
        ownerBootcoinWallet.setBalance(new BigDecimal("10"));

        CreateBootcoinWalletResponse accepterBootcoinWallet = new CreateBootcoinWalletResponse();
        accepterBootcoinWallet.setId("wallet2");
        accepterBootcoinWallet.setBalance(new BigDecimal("20"));

        GetYankiWalletResponse ownerYankiWallet = new GetYankiWalletResponse();
        ownerYankiWallet.setId("yankiWallet1");
        ownerYankiWallet.setUserId("user1");
        ownerYankiWallet.setBalance(new BigDecimal("1000"));

        when(bootcoinService.getExchangeRequestByTransactionId(transactionId))
            .thenReturn(Mono.just(exchangeRequest));
        when(bootcoinService.getBootcoinWalletByUserId("user1"))
            .thenReturn(Mono.just(ownerBootcoinWallet));
        when(bootcoinService.getBootcoinWalletByUserId("user2"))
            .thenReturn(Mono.just(accepterBootcoinWallet));
        when(yankiService.getYankiWalletByUserId("user1"))
            .thenReturn(Mono.just(ownerYankiWallet));
        when(bootcoinService.updateExchangeRequest(anyString(), any()))
            .thenReturn(Mono.empty());

        StepVerifier.create(bootcoinTransactionService.processBootcoinTransaction(transactionId))
            .verifyComplete();

        verify(bootcoinService).updateExchangeRequest(eq(transactionId), argThat(req ->
            req.getStatus().equals(ExchangeRequestStatus.REJECTED.toString()) &&
                req.getMessage().contains("Insufficient balance in the yanki wallet")));
        verify(yankiService, never()).updateYankiWallet(anyString(), any());
        verify(bootcoinService, never()).updateBootcoinWallet(anyString(), any());
    }

    @Test
    void processBootcoinTransaction_ErrorUpdatingYankiWallet() {
        String transactionId = "tx123";

        GetExchangeRequestByTransactionIdResponse exchangeRequest = new GetExchangeRequestByTransactionIdResponse();
        exchangeRequest.setId(transactionId);
        exchangeRequest.setRequestOwnerUserId("user1");
        exchangeRequest.setRequestAccepterUserId("user2");
        exchangeRequest.setPaymentMethod("YANKI");
        exchangeRequest.setAmount(new BigDecimal("5"));
        exchangeRequest.setBuyRate(100.0);

        CreateBootcoinWalletResponse ownerBootcoinWallet = new CreateBootcoinWalletResponse();
        ownerBootcoinWallet.setId("wallet1");
        ownerBootcoinWallet.setBalance(new BigDecimal("10"));

        CreateBootcoinWalletResponse accepterBootcoinWallet = new CreateBootcoinWalletResponse();
        accepterBootcoinWallet.setId("wallet2");
        accepterBootcoinWallet.setBalance(new BigDecimal("20"));

        GetYankiWalletResponse ownerYankiWallet = new GetYankiWalletResponse();
        ownerYankiWallet.setId("yankiWallet1");
        ownerYankiWallet.setUserId("user1");
        ownerYankiWallet.setBalance(new BigDecimal("1000"));

        when(bootcoinService.getExchangeRequestByTransactionId(transactionId))
            .thenReturn(Mono.just(exchangeRequest));
        when(bootcoinService.getBootcoinWalletByUserId("user1"))
            .thenReturn(Mono.just(ownerBootcoinWallet));
        when(bootcoinService.getBootcoinWalletByUserId("user2"))
            .thenReturn(Mono.just(accepterBootcoinWallet));
        when(yankiService.getYankiWalletByUserId("user1"))
            .thenReturn(Mono.just(ownerYankiWallet));
        when(yankiService.updateYankiWallet(anyString(), any()))
            .thenReturn(Mono.error(new RuntimeException("Error updating Yanki wallet")));

        StepVerifier.create(bootcoinTransactionService.processBootcoinTransaction(transactionId))
            .expectError(BadRequestException.class)
            .verify();

        verify(bootcoinService, never()).updateBootcoinWallet(anyString(), any());
    }

    @Test
    void processBootcoinTransaction_ErrorUpdatingBootcoinWallets() {
        String transactionId = "tx123";

        GetExchangeRequestByTransactionIdResponse exchangeRequest = new GetExchangeRequestByTransactionIdResponse();
        exchangeRequest.setId(transactionId);
        exchangeRequest.setRequestOwnerUserId("user1");
        exchangeRequest.setRequestAccepterUserId("user2");
        exchangeRequest.setPaymentMethod("YANKI");
        exchangeRequest.setAmount(new BigDecimal("5"));
        exchangeRequest.setBuyRate(100.0);

        CreateBootcoinWalletResponse ownerBootcoinWallet = new CreateBootcoinWalletResponse();
        ownerBootcoinWallet.setId("wallet1");
        ownerBootcoinWallet.setBalance(new BigDecimal("10"));

        CreateBootcoinWalletResponse accepterBootcoinWallet = new CreateBootcoinWalletResponse();
        accepterBootcoinWallet.setId("wallet2");
        accepterBootcoinWallet.setBalance(new BigDecimal("20"));

        GetYankiWalletResponse ownerYankiWallet = new GetYankiWalletResponse();
        ownerYankiWallet.setId("yankiWallet1");
        ownerYankiWallet.setUserId("user1");
        ownerYankiWallet.setBalance(new BigDecimal("1000"));

        when(bootcoinService.getExchangeRequestByTransactionId(transactionId))
            .thenReturn(Mono.just(exchangeRequest));
        when(bootcoinService.getBootcoinWalletByUserId("user1"))
            .thenReturn(Mono.just(ownerBootcoinWallet));
        when(bootcoinService.getBootcoinWalletByUserId("user2"))
            .thenReturn(Mono.just(accepterBootcoinWallet));
        when(yankiService.getYankiWalletByUserId("user1"))
            .thenReturn(Mono.just(ownerYankiWallet));
        when(yankiService.updateYankiWallet(anyString(), any()))
            .thenReturn(Mono.empty());
        when(bootcoinService.updateBootcoinWallet(anyString(), any()))
            .thenReturn(Mono.error(new BadRequestException("Error updating Bootcoin wallet")));
        when(bootcoinService.updateExchangeRequest(anyString(), any()))
            .thenReturn(Mono.empty());

        StepVerifier.create(bootcoinTransactionService.processBootcoinTransaction(transactionId))
            .expectError(BadRequestException.class)
            .verify();

        verify(bootcoinService).updateExchangeRequest(eq(transactionId), argThat(req ->
            req.getStatus().equals(ExchangeRequestStatus.REJECTED.toString()) &&
                req.getMessage().equals("Bootcoin wallets update failed")));
    }

    @Test
    void processBootcoinTransaction_ExchangeRequestNotFound() {
        String transactionId = "nonexistent";

        when(bootcoinService.getExchangeRequestByTransactionId(transactionId))
            .thenReturn(Mono.empty());

        StepVerifier.create(bootcoinTransactionService.processBootcoinTransaction(transactionId))
            .verifyComplete();

        verify(bootcoinService, never()).getBootcoinWalletByUserId(anyString());
        verify(yankiService, never()).getYankiWalletByUserId(anyString());
        verify(bootcoinService, never()).updateExchangeRequest(anyString(), any());
    }
}
