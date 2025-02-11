package com.project1.ms_transaction_service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean("accountWebClient")
    public WebClient accountClient() {
        return WebClient.create("http://localhost:8091/api/v1");
    }

    @Bean("creditCardWebClient")
    public WebClient creditCardClient() {
        return WebClient.create("http://localhost:8093/api/v1");
    }
}
