package com.firisbe.error;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException() {
    }

    public AccountNotFoundException(Throwable cause) {
        super("Account not found! " + cause);
    }
}
