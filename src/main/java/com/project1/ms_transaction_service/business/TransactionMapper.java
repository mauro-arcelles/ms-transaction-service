package com.project1.ms_transaction_service.business;

import com.project1.ms_transaction_service.model.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionMapper {

    public CustomerProductsResponse getCustomerProductsResponse(CustomerResponse customer,
                                                                List<AccountResponse> accounts,
                                                                List<CreditCardResponse> creditCards) {
        CustomerProductsResponse customerProductsResponse = new CustomerProductsResponse();
        customerProductsResponse.setCustomer(customer);
        customerProductsResponse.setAccounts(accounts);
        customerProductsResponse.setCreditCards(creditCards);
        return customerProductsResponse;
    }
}