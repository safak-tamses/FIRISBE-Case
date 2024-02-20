package com.firisbe.error;

public class CreditCardValidation extends RuntimeException {
    public CreditCardValidation(Throwable cause) {
        super("An error occurred during credit card validation!" + cause);
    }

    public CreditCardValidation() {
    }
}
