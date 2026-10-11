package com.company.bridge.service;

import com.company.bridge.config.BridgeProperties;
import com.company.bridge.model.AlertEvent;
import com.company.bridge.model.ApigeeRequest;
import com.company.bridge.model.EncryptionResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AlertService {

        private final ObjectMapper objectMapper;
        private final EncryptionService encryptionService;
        private final ApigeeService apigeeService;
        private final BridgeProperties properties;

        public AlertService(
                        ObjectMapper objectMapper,
                        EncryptionService encryptionService,
                        ApigeeService apigeeService,
                        BridgeProperties properties) {
                this.objectMapper = objectMapper;
                this.encryptionService = encryptionService;
                this.apigeeService = apigeeService;
                this.properties = properties;
        }

        public void process(AlertEvent event) {

                try {

                        // Convert Kafka event object back to JSON.
                        // This JSON is the plaintext that will be encrypted.
                        String payload = objectMapper.writeValueAsString(event);

                        EncryptionResult encrypted = encryptionService.encrypt(payload);

                        String requestId = UUID.randomUUID().toString();

                        BridgeProperties.Apigee apigee = properties.getApigee();

                        ApigeeRequest request = new ApigeeRequest(
                                        requestId,
                                        encrypted.encryptedKey(),
                                        encrypted.encryptedData(),
                                        encrypted.iv(),
                                        apigee.getService(),
                                        apigee.getClientInfo(),
                                        apigee.getOptionalParam(),
                                        properties
                                                        .getEncryption()
                                                        .getOaepHashingAlgorithm());

                        apigeeService.send(request);

                } catch (JsonProcessingException e) {

                        throw new RuntimeException(
                                        "Failed to serialize Kafka event",
                                        e);
                } catch (Exception e) {

                        throw new RuntimeException(
                                        "Failed to send alert to Apigee",
                                        e);
                }
        }
}