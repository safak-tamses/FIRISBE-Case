package com.firisbe.error;

public class CreditCardNumberAlreadyExist extends RuntimeException {
    public CreditCardNumberAlreadyExist() {
    }

    public CreditCardNumberAlreadyExist(Throwable cause) {
        super("Credit card number already exist: " + cause.getMessage());
    }
}
