package com.company.bridge.model;

public record ApigeeRequest(
        String requestId,
        String encryptedKey,
        String encryptedData,
        String iv,
        String service,
        String clientInfo,
        String optionalParam,
        String oaepHashingAlgorithm
) {
}