package com.firisbe.error;

public class InvalidCreditCardNumberException extends RuntimeException {
    public InvalidCreditCardNumberException() {
    }

    public InvalidCreditCardNumberException(Throwable cause) {
        super("Invalid credit card number according to Luhn algorithm: " + cause.getMessage());
    }
}
