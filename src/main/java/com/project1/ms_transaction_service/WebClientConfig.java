package com.project1.ms_transaction_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${application.config.account-service-url}")
    private String accountServiceBaseUrl;

    @Value("${application.config.credit-service-url}")
    private String creditServiceBaseUrl;

    @Value("${application.config.customer-service-url}")
    private String customerServiceBaseUrl;

    @Bean("accountWebClient")
    public WebClient accountWebClient() {
        return WebClient.builder()
                .baseUrl(accountServiceBaseUrl)
                .build();
    }

    @Bean("creditWebClient")
    public WebClient creditWebClient() {
        return WebClient.builder()
                .baseUrl(creditServiceBaseUrl)
                .build();
    }

    @Bean("customerServiceWebClient")
    public WebClient customerServiceWebClient() {
        return WebClient.builder()
                .baseUrl(customerServiceBaseUrl)
                .build();
    }
}
