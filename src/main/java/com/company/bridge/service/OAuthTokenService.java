package com.company.bridge.service;

import com.company.bridge.config.BridgeProperties;
import com.company.bridge.exception.BridgeException;
import com.company.bridge.model.OAuthTokenResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class OAuthTokenService {

    private final RestClient restClient;
    private final BridgeProperties properties;

    private volatile String accessToken;
    private volatile long tokenExpiryTimeMillis;

    public OAuthTokenService(
            RestClient restClient,
            BridgeProperties properties
    ) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public synchronized String getAccessToken() {

        long now = System.currentTimeMillis();

        // Refresh 60 seconds before actual expiry
        if (accessToken != null
                && now < tokenExpiryTimeMillis - 60_000) {

            return accessToken;
        }

        return generateAccessToken();
    }

    private String generateAccessToken() {

        try {

            BridgeProperties.Apigee apigee =
                    properties.getApigee();

            String credentials =
                    apigee.getClientKey()
                            + ":"
                            + apigee.getClientSecret();

            String basicAuth =
                    Base64.getEncoder()
                            .encodeToString(
                                    credentials.getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            );

            MultiValueMap<String, String> form =
                    new LinkedMultiValueMap<>();

            form.add(
                    "grant_type",
                    "client_credentials"
            );

            OAuthTokenResponse response =
                    restClient
                            .post()
                            .uri(apigee.getTokenUrl())
                            .contentType(
                                    MediaType.APPLICATION_FORM_URLENCODED
                            )
                            .header(
                                    "Authorization",
                                    "Basic " + basicAuth
                            )
                            .header(
                                    apigee.getApiKeyHeader(),
                                    apigee.getApiKey()
                            )
                            .body(form)
                            .retrieve()
                            .body(OAuthTokenResponse.class);

            if (response == null
                    || response.accessToken() == null
                    || response.accessToken().isBlank()) {

                throw new BridgeException(
                        "Apigee OAuth response did not contain access_token"
                );
            }

            this.accessToken =
                    response.accessToken();

            this.tokenExpiryTimeMillis =
                    System.currentTimeMillis()
                            + (response.expiresIn() * 1000);

            return accessToken;

        } catch (Exception e) {

            if (e instanceof BridgeException) {
                throw e;
            }

            throw new BridgeException(
                    "Failed to obtain Apigee OAuth token",
                    e
            );
        }
    }
}