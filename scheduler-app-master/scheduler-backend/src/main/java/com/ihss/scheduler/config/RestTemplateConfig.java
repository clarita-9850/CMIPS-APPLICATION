package com.ihss.scheduler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Value("${cmips.backend.timeout.connect:5000}")
    private int connectTimeout;

    @Value("${cmips.backend.timeout.read:30000}")
    private int readTimeout;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofMillis(connectTimeout))
            .setReadTimeout(Duration.ofMillis(readTimeout))
            .additionalInterceptors(loggingInterceptor())
            .build();
    }

    private ClientHttpRequestInterceptor loggingInterceptor() {
        return (request, body, execution) -> {
            // Log outgoing requests
            org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RestTemplateConfig.class);
            log.debug("Request: {} {}", request.getMethod(), request.getURI());
            return execution.execute(request, body);
        };
    }
}
