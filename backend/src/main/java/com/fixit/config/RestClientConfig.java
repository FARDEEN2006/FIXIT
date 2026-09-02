package com.fixit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * REST Client Configuration
 * 
 * Configures RestTemplate bean for HTTP requests to Supabase APIs
 */
@Configuration
public class RestClientConfig {

    /**
     * Create RestTemplate bean
     * Uses buffering to allow request/response logging
     */
    @Bean
    public RestTemplate restTemplate() {
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(
            new SimpleClientHttpRequestFactory()
        );
        return new RestTemplate(factory);
    }
}
