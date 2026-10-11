package com.company.bridge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            BridgeProperties properties
    ) {

        var factory =
                new ConcurrentKafkaListenerContainerFactory<String, String>();

        factory.setConsumerFactory(consumerFactory);

        long backoff =
                properties
                        .getRetry()
                        .getBackoffMs();

        long maxAttempts =
                properties
                        .getRetry()
                        .getMaxAttempts();

        // maxAttempts = 3 means:
        // initial attempt + 2 retries
        long retryCount =
                Math.max(0, maxAttempts - 1);

        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(
                        new FixedBackOff(
                                backoff,
                                retryCount
                        )
                );

        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}