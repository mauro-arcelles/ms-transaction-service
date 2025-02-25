package com.project1.ms_transaction_service.business.mapper;

import com.project1.ms_transaction_service.model.AccountPatchRequest;
import com.project1.ms_transaction_service.model.AccountResponse;
import com.project1.ms_transaction_service.model.AccountTransactionRequest;
import com.project1.ms_transaction_service.model.AccountTransactionResponse;
import com.project1.ms_transaction_service.model.entity.AccountTransaction;
import com.project1.ms_transaction_service.model.entity.AccountTransactionType;
import com.project1.ms_transaction_service.model.entity.AccountType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccountTransactionMapperTest {

    @Autowired
    private AccountTransactionMapper mapper;

    @Test
    void getAccountTransactionResponse_ShouldMapAllFields() {
        AccountTransaction transaction = new AccountTransaction();
        transaction.setId("1");
        transaction.setOriginAccountNumber("123");
        transaction.setDestinationAccountNumber("456");
        transaction.setType(AccountTransactionType.TRANSFER);
        transaction.setAmount(BigDecimal.TEN);
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription("Test");

        AccountTransactionResponse response = mapper.getAccountTransactionResponse(transaction);

        assertEquals(transaction.getId(), response.getId());
        assertEquals(transaction.getOriginAccountNumber(), response.getOriginAccountNumber());
        assertEquals(transaction.getDestinationAccountNumber(), response.getDestinationAccountNumber());
        assertEquals(transaction.getType().toString(), response.getType());
        assertEquals(transaction.getAmount(), response.getAmount());
        assertEquals(transaction.getDate(), response.getDate());
        assertEquals(transaction.getDescription(), response.getDescription());
    }

    @Test
    void getAccountTransactionEntity_WhenTransfer_ShouldSetDestinationAccount() {
        AccountTransactionRequest request = new AccountTransactionRequest();
        request.setOriginAccountNumber("123");
        request.setDestinationAccountNumber("456");
        request.setType(AccountTransactionType.TRANSFER.toString());
        request.setAmount(BigDecimal.TEN);
        request.setDescription("Test");

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setAccountType(AccountType.CHECKING.toString());

        AccountTransaction transaction = mapper.getAccountTransactionEntity(request, accountResponse);

        assertEquals(request.getDestinationAccountNumber(), transaction.getDestinationAccountNumber());
    }

    @Test
    void getAccountTransactionEntity_WhenSavingsAccountExceedsMovements_ShouldCalculateCommission() {
        AccountTransactionRequest request = new AccountTransactionRequest();
        request.setType(AccountTransactionType.DEPOSIT.toString());
        request.setAmount(BigDecimal.valueOf(100));

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setAccountType(AccountType.SAVINGS.toString());
        accountResponse.setMonthlyMovements(5);
        accountResponse.setMaxMonthlyMovementsNoFee(4);
        accountResponse.setTransactionCommissionFeePercentage(BigDecimal.valueOf(2));

        AccountTransaction transaction = mapper.getAccountTransactionEntity(request, accountResponse);

        assertEquals(0, BigDecimal.valueOf(2).compareTo(transaction.getCommissionFeePercentage()));
        assertEquals(0, BigDecimal.valueOf(2).compareTo(transaction.getCommissionFee()));
    }

    @Test
    void shouldApplyCommissionFee_WhenTransferAndSavings_ShouldNotCalculateCommission() {
        AccountTransactionRequest request = new AccountTransactionRequest();
        request.setType(AccountTransactionType.TRANSFER.toString());

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setAccountType(AccountType.SAVINGS.toString());
        accountResponse.setMonthlyMovements(5);
        accountResponse.setMaxMonthlyMovementsNoFee(4);
        accountResponse.setTransactionCommissionFeePercentage(BigDecimal.valueOf(2));

        AccountTransaction transaction = mapper.getAccountTransactionEntity(request, accountResponse);

        assertNull(transaction.getCommissionFeePercentage());
        assertNull(transaction.getCommissionFee());
    }

    @Test
    void shouldApplyCommissionFee_WhenSavingsButNotExceededMovements_ShouldNotCalculateCommission() {
        AccountTransactionRequest request = new AccountTransactionRequest();
        request.setType(AccountTransactionType.DEPOSIT.toString());
        request.setAmount(BigDecimal.valueOf(100));

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setAccountType(AccountType.SAVINGS.toString());
        accountResponse.setMonthlyMovements(3);
        accountResponse.setMaxMonthlyMovementsNoFee(4);
        accountResponse.setTransactionCommissionFeePercentage(BigDecimal.valueOf(2));

        AccountTransaction transaction = mapper.getAccountTransactionEntity(request, accountResponse);

        assertNull(transaction.getCommissionFeePercentage());
        assertNull(transaction.getCommissionFee());
    }

    @Test
    void shouldApplyCommissionFee_WhenSavingsWithNullMovements_ShouldNotCalculateCommission() {
        AccountTransactionRequest request = new AccountTransactionRequest();
        request.setType(AccountTransactionType.DEPOSIT.toString());
        request.setAmount(BigDecimal.valueOf(100));

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setAccountType(AccountType.SAVINGS.toString());

        AccountTransaction transaction = mapper.getAccountTransactionEntity(request, accountResponse);

        assertNull(transaction.getCommissionFeePercentage());
        assertNull(transaction.getCommissionFee());
    }

    @Test
    void getAccountPatchRequest_ForDeposit_ShouldCalculateBalanceAndMovementsOriginAccount() {
        AccountTransaction transaction = new AccountTransaction();
        transaction.setType(AccountTransactionType.DEPOSIT);
        transaction.setAmount(BigDecimal.valueOf(100));

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setBalance(BigDecimal.valueOf(500));
        accountResponse.setMonthlyMovements(2);

        AccountPatchRequest request = mapper.getAccountPatchRequest(transaction, accountResponse, true);

        assertEquals(BigDecimal.valueOf(600), request.getBalance());
        assertEquals(3, request.getMonthlyMovements());
    }

    @Test
    void getAccountPatchRequest_ForWithdrawal_ShouldCalculateBalanceAndMovementsOriginAccount() {
        AccountTransaction transaction = new AccountTransaction();
        transaction.setType(AccountTransactionType.WITHDRAWAL);
        transaction.setAmount(BigDecimal.valueOf(100));

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setBalance(BigDecimal.valueOf(500));
        accountResponse.setMonthlyMovements(2);

        AccountPatchRequest request = mapper.getAccountPatchRequest(transaction, accountResponse, true);

        assertEquals(BigDecimal.valueOf(400), request.getBalance());
        assertEquals(3, request.getMonthlyMovements());
    }

    @Test
    void getAccountPatchRequest_ForTransfer_ShouldCalculateBalanceAndMovementsOriginAccount() {
        AccountTransaction transaction = new AccountTransaction();
        transaction.setType(AccountTransactionType.TRANSFER);
        transaction.setAmount(BigDecimal.valueOf(100));

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setBalance(BigDecimal.valueOf(500));
        accountResponse.setMonthlyMovements(2);

        AccountPatchRequest request = mapper.getAccountPatchRequest(transaction, accountResponse, true);

        assertEquals(BigDecimal.valueOf(400), request.getBalance());
        assertEquals(3, request.getMonthlyMovements());
    }

    @Test
    void getAccountPatchRequest_ForTransfer_ShouldCalculateBalanceAndMovementsDestinationAccount() {
        AccountTransaction transaction = new AccountTransaction();
        transaction.setType(AccountTransactionType.TRANSFER);
        transaction.setAmount(BigDecimal.valueOf(100));

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setBalance(BigDecimal.valueOf(500));

        AccountPatchRequest request = mapper.getAccountPatchRequest(transaction, accountResponse, false);

        assertEquals(BigDecimal.valueOf(600), request.getBalance());
    }

    @Test
    void getAccountPatchRequest_ForWithdrawal_ShouldCalculateBalanceAndMovementsOriginAccountWithFee() {
        AccountTransaction transaction = new AccountTransaction();
        transaction.setType(AccountTransactionType.WITHDRAWAL);
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setCommissionFee(new BigDecimal("5"));

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setBalance(BigDecimal.valueOf(500));

        AccountPatchRequest request = mapper.getAccountPatchRequest(transaction, accountResponse, true);

        assertEquals(BigDecimal.valueOf(395), request.getBalance());
    }

    @Test
    void getAccountPatchRequest_ForDeposit_ShouldCalculateBalanceAndMovementsOriginAccountWithFee() {
        AccountTransaction transaction = new AccountTransaction();
        transaction.setType(AccountTransactionType.DEPOSIT);
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setCommissionFee(new BigDecimal("5"));

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setBalance(BigDecimal.valueOf(500));

        AccountPatchRequest request = mapper.getAccountPatchRequest(transaction, accountResponse, true);

        assertEquals(BigDecimal.valueOf(595), request.getBalance());
    }
}
