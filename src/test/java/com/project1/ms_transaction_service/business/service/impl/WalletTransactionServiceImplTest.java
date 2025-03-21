package com.project1.ms_transaction_service.business.service.impl;

import com.project1.ms_transaction_service.business.adapter.YankiService;
import com.project1.ms_transaction_service.business.mapper.WalletTransactionMapper;
import com.project1.ms_transaction_service.model.CreateWalletTransactionRequest;
import com.project1.ms_transaction_service.model.GetYankiWalletResponse;
import com.project1.ms_transaction_service.model.entity.WalletTransaction;
import com.project1.ms_transaction_service.repository.WalletTransactionRepository;
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
class WalletTransactionServiceImplTest {

    @Autowired
    private WalletTransactionServiceImpl walletTransactionService;

    @MockBean
    private WalletTransactionRepository walletTransactionRepository;

    @MockBean
    private WalletTransactionMapper walletTransactionMapper;

    @MockBean
    private YankiService yankiService;

    @Test
    void createWalletTransaction_Transfer_Success() {
        // Arrange
        CreateWalletTransactionRequest request = new CreateWalletTransactionRequest();
        request.setType("TRANSFER");
        request.setAmount(new BigDecimal("100"));
        request.setOriginWalletId("123");
        request.setDestinationWalletId("456");

        WalletTransaction transaction = new WalletTransaction();
        transaction.setType("TRANSFER");
        transaction.setAmount(new BigDecimal("100"));
        transaction.setOriginWalletId("123");
        transaction.setDestinationWalletId("456");

        GetYankiWalletResponse originWallet = new GetYankiWalletResponse();
        originWallet.setId("123");
        originWallet.setBalance(new BigDecimal("500"));

        GetYankiWalletResponse destinationWallet = new GetYankiWalletResponse();
        destinationWallet.setId("456");
        destinationWallet.setBalance(new BigDecimal("200"));

        when(walletTransactionMapper.getWalletTransactionEntity(any())).thenReturn(transaction);
        when(yankiService.getYankiWallet("123")).thenReturn(Mono.just(originWallet));
        when(yankiService.getYankiWallet("456")).thenReturn(Mono.just(destinationWallet));
        when(yankiService.updateYankiWallet(anyString(), any())).thenReturn(Mono.empty());
        when(walletTransactionRepository.save(any())).thenReturn(Mono.just(transaction));

        // Act & Assert
        StepVerifier.create(walletTransactionService.createWalletTransaction(Mono.just(request)))
            .verifyComplete();

        // Verify that update calls were made with correct amounts
        verify(yankiService).updateYankiWallet(eq("123"), argThat(req ->
            req.getBalance().compareTo(new BigDecimal("400")) == 0));
        verify(yankiService).updateYankiWallet(eq("456"), argThat(req ->
            req.getBalance().compareTo(new BigDecimal("300")) == 0));
        verify(walletTransactionRepository).save(transaction);
    }

    @Test
    void createWalletTransaction_Deposit_Success() {
        // Arrange
        CreateWalletTransactionRequest request = new CreateWalletTransactionRequest();
        request.setType("DEPOSIT");
        request.setAmount(new BigDecimal("100"));
        request.setOriginWalletId("123");
        request.setDestinationWalletId("456");

        WalletTransaction transaction = new WalletTransaction();
        transaction.setType("DEPOSIT");
        transaction.setAmount(new BigDecimal("100"));
        transaction.setOriginWalletId("123");
        transaction.setDestinationWalletId("456");

        GetYankiWalletResponse originWallet = new GetYankiWalletResponse();
        originWallet.setId("123");
        originWallet.setBalance(new BigDecimal("500"));

        GetYankiWalletResponse destinationWallet = new GetYankiWalletResponse();
        destinationWallet.setId("456");
        destinationWallet.setBalance(new BigDecimal("200"));

        when(walletTransactionMapper.getWalletTransactionEntity(any())).thenReturn(transaction);
        when(yankiService.getYankiWallet("123")).thenReturn(Mono.just(originWallet));
        when(yankiService.getYankiWallet("456")).thenReturn(Mono.just(destinationWallet));
        when(yankiService.updateYankiWallet(anyString(), any())).thenReturn(Mono.empty());
        when(walletTransactionRepository.save(any())).thenReturn(Mono.just(transaction));

        // Act & Assert
        StepVerifier.create(walletTransactionService.createWalletTransaction(Mono.just(request)))
            .verifyComplete();

        // Verify that only origin wallet was updated with added amount
        verify(yankiService).updateYankiWallet(eq("123"), argThat(req ->
            req.getBalance().compareTo(new BigDecimal("600")) == 0));
        verify(yankiService, never()).updateYankiWallet(eq("456"), any());
        verify(walletTransactionRepository).save(transaction);
    }

    @Test
    void createWalletTransaction_NullBalances_Success() {
        // Arrange
        CreateWalletTransactionRequest request = new CreateWalletTransactionRequest();
        request.setType("TRANSFER");
        request.setAmount(new BigDecimal("100"));
        request.setOriginWalletId("123");
        request.setDestinationWalletId("456");

        WalletTransaction transaction = new WalletTransaction();
        transaction.setType("TRANSFER");
        transaction.setAmount(new BigDecimal("100"));
        transaction.setOriginWalletId("123");
        transaction.setDestinationWalletId("456");

        GetYankiWalletResponse originWallet = new GetYankiWalletResponse();
        originWallet.setId("123");
        originWallet.setBalance(null); // Null balance

        GetYankiWalletResponse destinationWallet = new GetYankiWalletResponse();
        destinationWallet.setId("456");
        destinationWallet.setBalance(null); // Null balance

        when(walletTransactionMapper.getWalletTransactionEntity(any())).thenReturn(transaction);
        when(yankiService.getYankiWallet("123")).thenReturn(Mono.just(originWallet));
        when(yankiService.getYankiWallet("456")).thenReturn(Mono.just(destinationWallet));
        when(yankiService.updateYankiWallet(anyString(), any())).thenReturn(Mono.empty());
        when(walletTransactionRepository.save(any())).thenReturn(Mono.just(transaction));

        // Act & Assert
        StepVerifier.create(walletTransactionService.createWalletTransaction(Mono.just(request)))
            .verifyComplete();

        // Verify updates were called even with null balances
        verify(yankiService).updateYankiWallet(eq("123"), any());
        verify(yankiService).updateYankiWallet(eq("456"), any());
        verify(walletTransactionRepository).save(transaction);
    }

    @Test
    void createWalletTransaction_ExceptionFromYankiService() {
        // Arrange
        CreateWalletTransactionRequest request = new CreateWalletTransactionRequest();
        request.setType("TRANSFER");
        request.setAmount(new BigDecimal("100"));
        request.setOriginWalletId("123");
        request.setDestinationWalletId("456");

        WalletTransaction transaction = new WalletTransaction();
        transaction.setType("TRANSFER");
        transaction.setAmount(new BigDecimal("100"));
        transaction.setOriginWalletId("123");
        transaction.setDestinationWalletId("456");

        when(walletTransactionMapper.getWalletTransactionEntity(any())).thenReturn(transaction);
        when(yankiService.getYankiWallet("123")).thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        // Act & Assert
        StepVerifier.create(walletTransactionService.createWalletTransaction(Mono.just(request)))
            .expectError(RuntimeException.class)
            .verify();

        verify(walletTransactionRepository, never()).save(any());
    }
}
