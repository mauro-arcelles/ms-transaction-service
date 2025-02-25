package com.project1.ms_transaction_service.business.mapper;

import com.project1.ms_transaction_service.model.*;
import com.project1.ms_transaction_service.model.entity.AccountTransaction;
import com.project1.ms_transaction_service.model.entity.CreditCardTransaction;
import com.project1.ms_transaction_service.model.entity.DebitCardTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class TransactionMapperTest {

    @Autowired
    private TransactionMapper transactionMapper;

    @MockBean
    private CreditCardTransactionMapper creditCardTransactionMapper;

    @MockBean
    private DebitCardTransactionMapper debitCardTransactionMapper;

    @Test
    void getCustomerProductsResponse_ShouldMapCorrectly() {
        CustomerResponse customer = new CustomerResponse();
        List<AccountResponse> accounts = List.of(new AccountResponse());
        List<CreditCardResponse> creditCards = List.of(new CreditCardResponse());
        List<CreditResponse> credits = List.of(new CreditResponse());

        CustomerProductsResponse response = transactionMapper.getCustomerProductsResponse(
            customer, accounts, creditCards, credits);

        assertEquals(customer, response.getCustomer());
        assertEquals(accounts, response.getAccounts());
        assertEquals(creditCards, response.getCreditCards());
        assertEquals(credits, response.getCredits());
    }

    @Test
    void calculateAvgDailyBalance_ShouldCalculateCorrectly() {
        BigDecimal balance = new BigDecimal("100.00");
        int currentDay = LocalDate.now().getDayOfMonth();
        BigDecimal expected = balance.divide(BigDecimal.valueOf(currentDay), 2, RoundingMode.HALF_UP);

        BigDecimal result = transactionMapper.calculateAvgDailyBalance(balance);

        assertEquals(expected, result);
    }

    @Test
    void getProductsCommissionResponse_ShouldCalculateTotalsCorrectly() {
        AccountTransaction tx1 = new AccountTransaction();
        tx1.setOriginAccountNumber("123");
        tx1.setCommissionFee(new BigDecimal("10.00"));

        AccountTransaction tx2 = new AccountTransaction();
        tx2.setOriginAccountNumber("456");
        tx2.setCommissionFee(new BigDecimal("20.00"));

        List<AccountTransaction> transactions = List.of(tx1, tx2);

        ProductsCommissionResponse response = transactionMapper.getProductsCommissionResponse(transactions);

        assertNotNull(response.getCommissions());
        assertEquals(new BigDecimal("30.00"),
            response.getCommissions().getTotalAccountsCommissionFee());
    }

    @Test
    void getCreditDebitCardTransactionsResponse_ShouldMapTransactions() {
        CreditCardTransaction creditTx = new CreditCardTransaction();
        DebitCardTransaction debitTx = new DebitCardTransaction();

        CreditCardTransactionResponse creditResponse = new CreditCardTransactionResponse();
        DebitCardTransactionResponse debitResponse = new DebitCardTransactionResponse();

        when(creditCardTransactionMapper.getCreditCardTransactionResponse(creditTx))
            .thenReturn(creditResponse);
        when(debitCardTransactionMapper.getDebitCardTransactionResponse(debitTx))
            .thenReturn(debitResponse);

        CustomerProductsCreditDebitCardsTransactionsResponse response =
            transactionMapper.getCreditDebitCardTransactionsResponse(
                List.of(creditTx), List.of(debitTx));

        assertEquals(List.of(creditResponse), response.getCreditCardTransactions());
        assertEquals(List.of(debitResponse), response.getDebitCardTransactions());
    }

    @Test
    void getCustomerProductsAvgBalanceResponse_ShouldMapCorrectly() {
        CustomerResponse customer = new CustomerResponse();

        AccountResponse account = new AccountResponse();
        account.setAccountNumber("123");
        account.setBalance(new BigDecimal("100.00"));
        List<AccountResponse> accounts = List.of(account);

        CreditCardResponse creditCard = new CreditCardResponse();
        creditCard.setCardNumber("4444");
        creditCard.setUsedAmount(new BigDecimal("300.00"));
        creditCard.setCreditLimit(new BigDecimal("1000.00"));
        List<CreditCardResponse> creditCards = List.of(creditCard);

        CreditResponse credit = new CreditResponse();
        credit.setIdentifier("CR-001");
        credit.setAmountPaid(new BigDecimal("500.00"));
        List<CreditResponse> credits = List.of(credit);

        int currentDay = LocalDate.now().getDayOfMonth();
        BigDecimal expectedAccountAvg = new BigDecimal("100.00")
            .divide(BigDecimal.valueOf(currentDay), 2, RoundingMode.HALF_UP);
        BigDecimal expectedCreditCardAvg = new BigDecimal("700.00")
            .divide(BigDecimal.valueOf(currentDay), 2, RoundingMode.HALF_UP);
        BigDecimal expectedCreditAvg = new BigDecimal("500.00")
            .divide(BigDecimal.valueOf(currentDay), 2, RoundingMode.HALF_UP);

        CustomerProductsAverageBalanceResponse response = transactionMapper
            .getCustomerProductsAvgBalanceResponse(customer, accounts, creditCards, credits);

        assertEquals(customer, response.getCustomer());

        assertEquals(1, response.getAccounts().size());
        assertEquals("123", response.getAccounts().get(0).getAccountNumber());
        assertEquals(expectedAccountAvg, response.getAccounts().get(0).getAverageBalance());

        assertEquals(1, response.getCreditCards().size());
        assertEquals("4444", response.getCreditCards().get(0).getCardNumber());
        assertEquals(expectedCreditCardAvg, response.getCreditCards().get(0).getAverageBalance());

        assertEquals(1, response.getCredits().size());
        assertEquals("CR-001", response.getCredits().get(0).getCreditIdentifier());
        assertEquals(expectedCreditAvg, response.getCredits().get(0).getAverageBalance());
    }

    @Test
    void getCustomerProductsAvgBalanceResponse_WithNullValues_ShouldUseZero() {
        CustomerResponse customer = new CustomerResponse();

        AccountResponse account = new AccountResponse();
        account.setAccountNumber("123");
        account.setBalance(null);
        List<AccountResponse> accounts = List.of(account);

        CreditCardResponse creditCard = new CreditCardResponse();
        creditCard.setCardNumber("4444");
        creditCard.setUsedAmount(null);
        creditCard.setCreditLimit(null);
        List<CreditCardResponse> creditCards = List.of(creditCard);

        CreditResponse credit = new CreditResponse();
        credit.setIdentifier("CR-001");
        credit.setAmountPaid(null);
        List<CreditResponse> credits = List.of(credit);

        BigDecimal expectedZeroAvg = new BigDecimal("0.00");

        CustomerProductsAverageBalanceResponse response = transactionMapper
            .getCustomerProductsAvgBalanceResponse(customer, accounts, creditCards, credits);

        assertEquals(expectedZeroAvg, response.getAccounts().get(0).getAverageBalance());
        assertEquals(expectedZeroAvg, response.getCreditCards().get(0).getAverageBalance());
        assertEquals(expectedZeroAvg, response.getCredits().get(0).getAverageBalance());
    }
}
