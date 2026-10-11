package com.company.bridge.service;

import com.company.bridge.config.BridgeProperties;
import com.company.bridge.exception.BridgeException;
import com.company.bridge.model.EncryptionResult;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.MGF1ParameterSpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

@Service
public class EncryptionService {

    private static final int AES_KEY_SIZE = 256;
    private static final int IV_SIZE = 12;
    private static final int GCM_TAG_SIZE = 128;

    private final BridgeProperties properties;
    private final SecureRandom secureRandom;

    public EncryptionService(BridgeProperties properties) {
        this.properties = properties;
        this.secureRandom = new SecureRandom();
    }

    public EncryptionResult encrypt(String jsonPayload) {

        try {
            // 1. Generate a new AES-256 session key
            SecretKey sessionKey = generateSessionKey();

            // 2. Encrypt AES session key using RSA-OAEP SHA-256
            PublicKey publicKey = loadPublicKey();

            byte[] encryptedSessionKey =
                    encryptSessionKey(sessionKey, publicKey);

            // 3. Generate a new random 12-byte IV
            byte[] iv = generateIv();

            // 4. Encrypt JSON using AES-256-GCM
            byte[] encryptedPayload =
                    encryptPayload(jsonPayload, sessionKey, iv);

            // 5. Pattern A: prepend IV
            byte[] finalEncryptedData =
                    new byte[iv.length + encryptedPayload.length];

            System.arraycopy(
                    iv,
                    0,
                    finalEncryptedData,
                    0,
                    iv.length
            );

            System.arraycopy(
                    encryptedPayload,
                    0,
                    finalEncryptedData,
                    iv.length,
                    encryptedPayload.length
            );

            String encodedKey =
                    Base64.getEncoder()
                            .encodeToString(encryptedSessionKey);

            String encodedData =
                    Base64.getEncoder()
                            .encodeToString(finalEncryptedData);

            return new EncryptionResult(
                    encodedKey,
                    encodedData,
                    ""
            );

        } catch (Exception e) {
            throw new BridgeException(
                    "Failed to encrypt payload",
                    e
            );
        }
    }

    private SecretKey generateSessionKey() throws Exception {

        KeyGenerator keyGenerator =
                KeyGenerator.getInstance("AES");

        keyGenerator.init(AES_KEY_SIZE);

        return keyGenerator.generateKey();
    }

    private byte[] generateIv() {

        byte[] iv = new byte[IV_SIZE];

        secureRandom.nextBytes(iv);

        return iv;
    }

    private byte[] encryptSessionKey(
            SecretKey sessionKey,
            PublicKey publicKey
    ) throws Exception {

        OAEPParameterSpec oaepParameters =
                new OAEPParameterSpec(
                        "SHA-256",
                        "MGF1",
                        MGF1ParameterSpec.SHA256,
                        PSource.PSpecified.DEFAULT
                );

        Cipher rsaCipher =
                Cipher.getInstance(
                        "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
                );

        rsaCipher.init(
                Cipher.ENCRYPT_MODE,
                publicKey,
                oaepParameters
        );

        return rsaCipher.doFinal(
                sessionKey.getEncoded()
        );
    }

    private byte[] encryptPayload(
            String jsonPayload,
            SecretKey sessionKey,
            byte[] iv
    ) throws Exception {

        Cipher aesCipher =
                Cipher.getInstance("AES/GCM/NoPadding");

        GCMParameterSpec gcmSpec =
                new GCMParameterSpec(
                        GCM_TAG_SIZE,
                        iv
                );

        aesCipher.init(
                Cipher.ENCRYPT_MODE,
                sessionKey,
                gcmSpec
        );

        return aesCipher.doFinal(
                jsonPayload.getBytes(StandardCharsets.UTF_8)
        );
    }

    private PublicKey loadPublicKey() throws Exception {

        String certificatePath =
                properties
                        .getEncryption()
                        .getPublicCertificatePath();

        byte[] certificateBytes =
                Files.readAllBytes(
                        Path.of(certificatePath)
                );

        CertificateFactory certificateFactory =
                CertificateFactory.getInstance("X.509");

        X509Certificate certificate =
                (X509Certificate)
                        certificateFactory.generateCertificate(
                                new ByteArrayInputStream(
                                        certificateBytes
                                )
                        );

        return certificate.getPublicKey();
    }
}