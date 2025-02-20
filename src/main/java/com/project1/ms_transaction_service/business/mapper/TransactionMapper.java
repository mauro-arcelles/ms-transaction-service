package com.project1.ms_transaction_service.business.mapper;

import com.project1.ms_transaction_service.model.*;
import com.project1.ms_transaction_service.model.entity.AccountTransaction;
import com.project1.ms_transaction_service.model.entity.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TransactionMapper {

    public CustomerProductsResponse getCustomerProductsResponse(CustomerResponse customer,
                                                                List<AccountResponse> accounts,
                                                                List<CreditCardResponse> creditCards,
                                                                List<CreditResponse> credits) {
        CustomerProductsResponse response = new CustomerProductsResponse();
        response.setCustomer(customer);
        response.setAccounts(accounts);
        response.setCreditCards(creditCards);
        response.setCredits(credits);
        return response;
    }

    public CustomerProductsAverageBalanceResponse getCustomerProductsAvgBalanceResponse(CustomerResponse customer,
                                                                                        List<AccountResponse> accounts,
                                                                                        List<CreditCardResponse> creditCards,
                                                                                        List<CreditResponse> credits) {
        CustomerProductsAverageBalanceResponse response = new CustomerProductsAverageBalanceResponse();
        response.setCustomer(customer);

        List<CustomerProductsAverageBalanceResponseAccountsInner> accountsAvgBalances = accounts.stream()
            .map(accountResponse -> {
                CustomerProductsAverageBalanceResponseAccountsInner accountAvgBalance = new CustomerProductsAverageBalanceResponseAccountsInner();
                accountAvgBalance.setAccountNumber(accountResponse.getAccountNumber());
                BigDecimal balance = Optional.ofNullable(accountResponse.getBalance())
                    .orElse(BigDecimal.ZERO);
                accountAvgBalance.setAverageBalance(calculateAvgDailyBalance(balance));
                return accountAvgBalance;
            })
            .collect(Collectors.toList());
        response.setAccounts(accountsAvgBalances);

        List<CustomerProductsAverageBalanceResponseCreditCardsInner> creditCardsAvgBalances = creditCards.stream()
            .map(creditCardResponse -> {
                CustomerProductsAverageBalanceResponseCreditCardsInner creditCardAvgBalance = new CustomerProductsAverageBalanceResponseCreditCardsInner();
                BigDecimal usedAmount = Optional.ofNullable(creditCardResponse.getUsedAmount())
                    .orElse(BigDecimal.ZERO);
                BigDecimal creditLimit = Optional.ofNullable(creditCardResponse.getCreditLimit())
                    .orElse(BigDecimal.ZERO);

                creditCardAvgBalance.setAverageBalance(calculateAvgDailyBalance(creditLimit.subtract(usedAmount)));
                creditCardAvgBalance.setCardNumber(creditCardResponse.getCardNumber());

                return creditCardAvgBalance;
            }).collect(Collectors.toList());
        response.setCreditCards(creditCardsAvgBalances);

        List<CustomerProductsAverageBalanceResponseCreditsInner> creditsAvgBalances = credits.stream()
            .map(creditResponse -> {
                CustomerProductsAverageBalanceResponseCreditsInner creditAvgBalance = new CustomerProductsAverageBalanceResponseCreditsInner();
                creditAvgBalance.setCreditIdentifier(creditResponse.getIdentifier());
                BigDecimal balance = Optional.ofNullable(creditResponse.getAmountPaid())
                    .orElse(BigDecimal.ZERO);
                creditAvgBalance.setAverageBalance(calculateAvgDailyBalance(balance));
                return creditAvgBalance;
            }).collect(Collectors.toList());
        response.setCredits(creditsAvgBalances);

        return response;
    }

    public BigDecimal calculateAvgDailyBalance(BigDecimal balance) {
        int actualDay = LocalDate.now().getDayOfMonth();
        return balance.divide(BigDecimal.valueOf(actualDay), 2, RoundingMode.HALF_UP);
    }

    public ProductsCommissionResponse getProductsCommissionResponse(List<AccountTransaction> transactions) {
        ProductsCommissionResponse response = new ProductsCommissionResponse();
        ProductsCommissionResponseCommissions commissions = new ProductsCommissionResponseCommissions();

        List<ProductsCommissionResponseCommissionsAccountsInner> accountsCommissions = transactions.stream()
            .filter(accountTransaction -> accountTransaction.getCommissionFee() != null)
            .map(accountTransaction -> {
                ProductsCommissionResponseCommissionsAccountsInner accountCommissions = new ProductsCommissionResponseCommissionsAccountsInner();
                accountCommissions.setAccountNumber(accountTransaction.getOriginAccountNumber());
                accountCommissions.setTotalCommissionFee(accountTransaction.getCommissionFee());
                return accountCommissions;
            }).collect(Collectors.toList());
        commissions.setAccounts(accountsCommissions);

        BigDecimal totalAccountCommissions = accountsCommissions.stream()
            .map(ProductsCommissionResponseCommissionsAccountsInner::getTotalCommissionFee)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        commissions.setTotalAccountsCommissionFee(totalAccountCommissions);
        response.setCommissions(commissions);
        return response;
    }

}
