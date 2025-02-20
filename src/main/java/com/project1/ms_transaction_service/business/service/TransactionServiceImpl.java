package com.project1.ms_transaction_service.business.service;

import com.project1.ms_transaction_service.business.adapter.AccountService;
import com.project1.ms_transaction_service.business.adapter.CreditCardService;
import com.project1.ms_transaction_service.business.adapter.CreditService;
import com.project1.ms_transaction_service.business.adapter.CustomerService;
import com.project1.ms_transaction_service.business.mapper.TransactionMapper;
import com.project1.ms_transaction_service.model.*;
import com.project1.ms_transaction_service.model.entity.AccountTransaction;
import com.project1.ms_transaction_service.repository.AccountTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private CreditCardService creditCardService;

    @Autowired
    private CreditService creditService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AccountTransactionRepository accountTransactionRepository;

    @Override
    public Mono<CustomerProductsResponse> getAllCustomerProductsByDni(String dni) {
        return customerService.getCustomerByDni(dni)
                .flatMap(customerResponse ->
                        Mono.zip(
                                accountService.getAccountsByCustomerId(customerResponse.getId()).collectList(),
                                creditCardService.getCreditCardsByCustomerId(customerResponse.getId()).collectList(),
                                creditService.getCreditsByCustomerId(customerResponse.getId()).collectList()
                        ).map(tuple ->
                                transactionMapper.getCustomerProductsResponse(
                                        customerResponse,
                                        tuple.getT1(),
                                        tuple.getT2(),
                                        tuple.getT3()
                                )
                        )
                );
    }

    @Override
    public Mono<CustomerProductsResponse> getAllCustomerProductsByRuc(String ruc) {
        return customerService.getCustomerByRuc(ruc)
                .flatMap(customerResponse ->
                        Mono.zip(
                                accountService.getAccountsByCustomerId(customerResponse.getId()).collectList(),
                                creditCardService.getCreditCardsByCustomerId(customerResponse.getId()).collectList(),
                                creditService.getCreditsByCustomerId(customerResponse.getId()).collectList()
                        ).map(tuple ->
                                transactionMapper.getCustomerProductsResponse(
                                        customerResponse,
                                        tuple.getT1(),
                                        tuple.getT2(),
                                        tuple.getT3()
                                )
                        )
                );
    }

    @Override
    public Mono<CustomerProductsResponse> getAllCustomerProductsByCustomerId(String customerId) {
        return customerService.getCustomerById(customerId)
                .flatMap(customerResponse ->
                        Mono.zip(
                                accountService.getAccountsByCustomerId(customerResponse.getId()).collectList(),
                                creditCardService.getCreditCardsByCustomerId(customerResponse.getId()).collectList(),
                                creditService.getCreditsByCustomerId(customerResponse.getId()).collectList()
                        ).map(tuple ->
                                transactionMapper.getCustomerProductsResponse(
                                        customerResponse,
                                        tuple.getT1(),
                                        tuple.getT2(),
                                        tuple.getT3()
                                )
                        )
                );
    }

    @Override
    public Mono<CustomerProductsAverageBalanceResponse> getAllCustomerProductsAvgBalanceCustomerId(String customerId) {
        return customerService.getCustomerById(customerId)
                .flatMap(customerResponse ->
                        Mono.zip(
                                accountService.getAccountsByCustomerId(customerResponse.getId()).collectList(),
                                creditCardService.getCreditCardsByCustomerId(customerResponse.getId()).collectList(),
                                creditService.getCreditsByCustomerId(customerResponse.getId()).collectList()
                        ).map(tuple ->
                                transactionMapper.getCustomerProductsAvgBalanceResponse(
                                        customerResponse,
                                        tuple.getT1(),
                                        tuple.getT2(),
                                        tuple.getT3()
                                )
                        )
                );
    }

    @Override
    public Mono<ProductsCommissionResponse> getProductsCommissionByRange(LocalDateTime startDate, LocalDateTime endDate) {
        return accountTransactionRepository.findAllByDateBetween(startDate, endDate)
                .collectList()
                .map(transactionMapper::getProductsCommissionResponse);
    }
}
