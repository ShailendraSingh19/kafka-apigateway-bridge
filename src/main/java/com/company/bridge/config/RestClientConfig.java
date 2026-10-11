package com.company.bridge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    System.out.println("Outgoing HTTP method: " + request.getMethod());
                    System.out.println("Outgoing URL: " + request.getURI());
                    System.out.println("Outgoing Content-Type: " + request.getHeaders().getContentType());
                    System.out.println("Outgoing body length: " + body.length);

                    return execution.execute(request, body);
                })
                .build();
    }
}