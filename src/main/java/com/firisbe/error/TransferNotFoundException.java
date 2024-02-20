package com.firisbe.error;

public class TransferNotFoundException extends RuntimeException {
    public TransferNotFoundException() {
    }

    public TransferNotFoundException(Throwable cause) {
        super("Transfer not found: " + cause.getMessage());
    }
}
