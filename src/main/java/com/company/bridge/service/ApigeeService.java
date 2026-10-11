package com.company.bridge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.company.bridge.config.BridgeProperties;
import com.company.bridge.exception.BridgeException;
import com.company.bridge.model.ApigeeRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ApigeeService {

        private final RestClient restClient;
        private final OAuthTokenService oauthTokenService;
        private final BridgeProperties properties;
        // private final ObjectMapper objectMapper;

        public ApigeeService(
                        RestClient restClient,
                        OAuthTokenService oauthTokenService,
                        BridgeProperties properties) {
                this.restClient = restClient;
                this.oauthTokenService = oauthTokenService;
                this.properties = properties;
                // this.objectMapper = objectMapper;
        }

        public void send(ApigeeRequest request) throws Exception {

                BridgeProperties.Apigee apigee = properties.getApigee();

                String accessToken = oauthTokenService.getAccessToken();

                // System.out.println("ApigeeRequest object: " + request);
                // try {
                // String json = objectMapper.writeValueAsString(request);
                // System.out.println("Serialized Apigee request: " + json);
                // } catch (Exception e) {
                // throw new RuntimeException("Failed to serialize Apigee request", e);
                // }
                restClient
                                .post()
                                .uri(apigee.getApiUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(
                                                "Authorization",
                                                "Bearer " + accessToken)
                                .header(
                                                apigee.getApiKeyHeader(),
                                                apigee.getApiKey())
                                .body(request)
                                .retrieve()
                                .toBodilessEntity();
        }
}