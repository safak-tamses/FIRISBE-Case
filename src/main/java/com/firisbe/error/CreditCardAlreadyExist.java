package com.firisbe.error;

public class CreditCardAlreadyExist extends RuntimeException {
    public CreditCardAlreadyExist(Throwable cause) {
        super("Credit card already exists: " + cause.getMessage());
    }

    public CreditCardAlreadyExist() {
    }
}
