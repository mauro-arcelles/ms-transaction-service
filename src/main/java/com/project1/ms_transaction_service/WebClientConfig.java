package com.project1.ms_transaction_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${account.service.baseUrl}")
    private String accountServiceBaseUrl;

    @Value("${credit.service.baseUrl}")
    private String creditServiceBaseUrl;

    @Bean("accountWebClient")
    public WebClient accountClient() {
        return WebClient.builder()
                .baseUrl(accountServiceBaseUrl)
                .build();
    }

    @Bean("creditCardWebClient")
    public WebClient creditCardClient() {
        return WebClient.builder()
                .baseUrl(creditServiceBaseUrl)
                .build();
    }
}
