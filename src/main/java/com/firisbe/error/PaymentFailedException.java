package com.firisbe.error;

public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(Throwable cause) {
        super("An error was encountered while making the payment. Reason for the error: " + cause);
    }

    public PaymentFailedException() {
    }
}
