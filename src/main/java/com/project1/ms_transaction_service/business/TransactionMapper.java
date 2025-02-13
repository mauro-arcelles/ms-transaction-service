package com.project1.ms_transaction_service.business;

import com.project1.ms_transaction_service.model.*;
import com.project1.ms_transaction_service.model.entity.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TransactionMapper {

    public AccountTransactionResponse getAccountTransactionResponse(Transaction transaction) {
        AccountTransactionResponse response = new AccountTransactionResponse();
        AccountTransaction accountTransaction = (AccountTransaction) transaction;
        response.setId(transaction.getId());
        response.setOriginAccountNumber(accountTransaction.getOriginAccountNumber());
        response.setDestinationAccountNumber(accountTransaction.getDestinationAccountNumber());
        response.setType(accountTransaction.getType().toString());
        response.setAmount(accountTransaction.getAmount());
        response.setDate(accountTransaction.getDate());
        response.setDescription(accountTransaction.getDescription());
        return response;
    }

    public Transaction getAccountTransactionEntity(AccountTransactionRequest request) {
        AccountTransaction accountTransaction = new AccountTransaction();
        accountTransaction.setOriginAccountNumber(request.getOriginAccountNumber());
        accountTransaction.setDestinationAccountNumber(request.getDestinationAccountNumber());
        accountTransaction.setType(AccountTransactionType.valueOf(request.getType()));
        accountTransaction.setAmount(request.getAmount());
        accountTransaction.setDate(LocalDateTime.now());
        accountTransaction.setDescription(request.getDescription());
        return accountTransaction;
    }

    public CreditCardUsageTransactionResponse getCreditCardTransactionResponse(Transaction transaction) {
        CreditCardUsageTransactionResponse creditCardTransactionResponse = new CreditCardUsageTransactionResponse();
        CreditCardTransaction creditCardTransaction = (CreditCardTransaction) transaction;
        creditCardTransactionResponse.setCreditCard(creditCardTransaction.getCreditCard());
        creditCardTransactionResponse.setAmount(creditCardTransaction.getAmount());
        creditCardTransactionResponse.setDescription(creditCardTransaction.getDescription());
        creditCardTransactionResponse.setCustomerId(creditCardTransaction.getCustomerId());
        return creditCardTransactionResponse;
    }

    public CreditCardTransaction getCreditCardTransactionEntity(CreditCardUsageTransactionRequest request) {
        CreditCardTransaction creditCardTransaction = new CreditCardTransaction();
        creditCardTransaction.setAmount(request.getAmount());
        creditCardTransaction.setCreditCard(request.getCreditCard());
        creditCardTransaction.setDescription(request.getDescription());
        creditCardTransaction.setCustomerId(request.getCustomerId());
        return creditCardTransaction;
    }

    public CustomerProductsResponse getCustomerProductsResponse(CustomerResponse customer,
                                                                List<AccountResponse> accounts,
                                                                List<CreditCardResponse> creditCards) {
        CustomerProductsResponse customerProductsResponse = new CustomerProductsResponse();
        customerProductsResponse.setCustomer(customer);
        customerProductsResponse.setAccounts(accounts);
        customerProductsResponse.setCreditCards(creditCards);
        return customerProductsResponse;
    }

    public CreditTransaction getCreditPaymentTransactionEntity(CreditPaymentTransactionRequest request) {
        CreditTransaction creditTransaction = new CreditTransaction();
        creditTransaction.setCreditId(request.getCreditId());
        return creditTransaction;
    }

    public CreditPaymentTransactionResponse getCreditPaymentTransactionResponse(CreditTransaction creditTransaction) {
        CreditPaymentTransactionResponse creditPaymentTransactionResponse = new CreditPaymentTransactionResponse();
        creditPaymentTransactionResponse.setCreditId(creditTransaction.getCreditId());
        return  creditPaymentTransactionResponse;
    }

}