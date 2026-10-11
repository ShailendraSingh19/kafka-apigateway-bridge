package com.company.bridge.model;

public record EncryptionResult(
        String encryptedKey,
        String encryptedData,
        String iv
) {
}