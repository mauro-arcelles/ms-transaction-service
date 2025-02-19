package com.project1.ms_transaction_service.business.mapper;

import com.project1.ms_transaction_service.model.*;
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

    public CustomerProductsAverageBalance getCustomerProductsAvgBalanceResponse(CustomerResponse customer,
                                                                                List<AccountResponse> accounts,
                                                                                List<CreditCardResponse> creditCards,
                                                                                List<CreditResponse> credits) {
        CustomerProductsAverageBalance response = new CustomerProductsAverageBalance();
        response.setCustomer(customer);

        List<CustomerProductsAverageBalanceAccountsInner> accountsAvgBalances = accounts.stream()
                .map(accountResponse -> {
                    CustomerProductsAverageBalanceAccountsInner accountAvgBalance = new CustomerProductsAverageBalanceAccountsInner();
                    accountAvgBalance.setAccountNumber(accountResponse.getAccountNumber());
                    BigDecimal balance = Optional.ofNullable(accountResponse.getBalance())
                            .orElse(BigDecimal.ZERO);
                    accountAvgBalance.setAverageBalance(calculateAvgDailyBalance(balance));
                    return accountAvgBalance;
                })
                .collect(Collectors.toList());
        response.setAccounts(accountsAvgBalances);

        List<CustomerProductsAverageBalanceCreditCardsInner> creditCardsAvgBalances = creditCards.stream()
                .map(creditCardResponse -> {
                    CustomerProductsAverageBalanceCreditCardsInner creditCardAvgBalance = new CustomerProductsAverageBalanceCreditCardsInner();
                    BigDecimal usedAmount = Optional.ofNullable(creditCardResponse.getUsedAmount())
                            .orElse(BigDecimal.ZERO);
                    BigDecimal creditLimit = Optional.ofNullable(creditCardResponse.getCreditLimit())
                                    .orElse(BigDecimal.ZERO);

                    creditCardAvgBalance.setAverageBalance(calculateAvgDailyBalance(creditLimit.subtract(usedAmount)));
                    creditCardAvgBalance.setCardNumber(creditCardResponse.getCardNumber());

                    return creditCardAvgBalance;
                }).collect(Collectors.toList());
        response.setCreditCards(creditCardsAvgBalances);

        List<CustomerProductsAverageBalanceCreditsInner> creditsAvgBalances = credits.stream()
                .map(creditResponse -> {
                    CustomerProductsAverageBalanceCreditsInner creditAvgBalance = new CustomerProductsAverageBalanceCreditsInner();
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

}