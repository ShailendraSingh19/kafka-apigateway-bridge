package com.company.bridge.consumer;

import com.company.bridge.model.AlertEvent;
import com.company.bridge.service.AlertService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AlertKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final AlertService alertService;

    public AlertKafkaConsumer(
            ObjectMapper objectMapper,
            AlertService alertService
    ) {
        this.objectMapper = objectMapper;
        this.alertService = alertService;
    }

    @KafkaListener(
            topics = "${bridge.kafka.topic}"
    )
    public void consume(String message) throws Exception {

        AlertEvent event =
                objectMapper.readValue(
                        message,
                        AlertEvent.class
                );

        alertService.process(event);
    }
}