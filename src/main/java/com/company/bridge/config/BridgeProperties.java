package com.company.bridge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bridge")
public class BridgeProperties {

    private Kafka kafka = new Kafka();
    private Apigee apigee = new Apigee();
    private Encryption encryption = new Encryption();
    private Retry retry = new Retry();

    public Kafka getKafka() {
        return kafka;
    }

    public Apigee getApigee() {
        return apigee;
    }

    public Encryption getEncryption() {
        return encryption;
    }

    public Retry getRetry() {
        return retry;
    }

    public static class Kafka {
        private String topic;

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }
    }

    public static class Apigee {
        private String tokenUrl;
        private String apiUrl;
        private String apiKey;
        private String clientKey;
        private String clientSecret;
        private String apiKeyHeader;
        private String service;
        private String clientInfo;
        private String optionalParam;
        private int connectTimeoutMs;
        private int readTimeoutMs;

        public String getTokenUrl() {
            return tokenUrl;
        }

        public void setTokenUrl(String tokenUrl) {
            this.tokenUrl = tokenUrl;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getClientKey() {
            return clientKey;
        }

        public void setClientKey(String clientKey) {
            this.clientKey = clientKey;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getApiKeyHeader() {
            return apiKeyHeader;
        }

        public void setApiKeyHeader(String apiKeyHeader) {
            this.apiKeyHeader = apiKeyHeader;
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getClientInfo() {
            return clientInfo;
        }

        public void setClientInfo(String clientInfo) {
            this.clientInfo = clientInfo;
        }

        public String getOptionalParam() {
            return optionalParam;
        }

        public void setOptionalParam(String optionalParam) {
            this.optionalParam = optionalParam;
        }

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }
    }

    public static class Encryption {
        private String publicCertificatePath;
        private String ivMode;
        private String oaepHashingAlgorithm;

        public String getPublicCertificatePath() {
            return publicCertificatePath;
        }

        public void setPublicCertificatePath(String publicCertificatePath) {
            this.publicCertificatePath = publicCertificatePath;
        }

        public String getIvMode() {
            return ivMode;
        }

        public void setIvMode(String ivMode) {
            this.ivMode = ivMode;
        }

        public String getOaepHashingAlgorithm() {
            return oaepHashingAlgorithm;
        }

        public void setOaepHashingAlgorithm(String oaepHashingAlgorithm) {
            this.oaepHashingAlgorithm = oaepHashingAlgorithm;
        }
    }

    public static class Retry {
        private int maxAttempts;
        private long backoffMs;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getBackoffMs() {
            return backoffMs;
        }

        public void setBackoffMs(long backoffMs) {
            this.backoffMs = backoffMs;
        }
    }
}