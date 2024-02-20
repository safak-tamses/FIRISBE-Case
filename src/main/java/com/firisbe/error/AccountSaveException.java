package com.firisbe.error;

public class AccountSaveException extends RuntimeException {
    public AccountSaveException(Throwable cause) {
        super("An error occurred while saving the account", cause);
    }

    public AccountSaveException() {
    }
}
