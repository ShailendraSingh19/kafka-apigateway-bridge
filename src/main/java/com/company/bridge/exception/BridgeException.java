package com.company.bridge.exception;

public class BridgeException extends RuntimeException {

    public BridgeException(String message) {
        super(message);
    }

    public BridgeException(String message, Throwable cause) {
        super(message, cause);
    }
}