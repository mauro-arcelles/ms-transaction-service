package com.project1.ms_transaction_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
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

    @Value("${application.config.bootcoin-service-url}")
    private String bootcoinServiceBaseUrl;

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean("accountWebClient")
    public WebClient accountWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
            .baseUrl(accountServiceBaseUrl)
            .build();
    }

    @Bean("creditWebClient")
    public WebClient creditWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
            .baseUrl(creditServiceBaseUrl)
            .build();
    }

    @Bean("customerWebClient")
    public WebClient customerWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
            .baseUrl(customerServiceBaseUrl)
            .build();
    }

    @Bean("bootcoinWebClient")
    public WebClient bootcoinWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
            .baseUrl(bootcoinServiceBaseUrl)
            .build();
    }
}
