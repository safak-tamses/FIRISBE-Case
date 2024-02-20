package com.firisbe.error;

public class CustomerNotFoundException extends RuntimeException{
    public CustomerNotFoundException(Throwable cause) {
        super("Customer not found: ", cause);
    }

    public CustomerNotFoundException() {
    }
}
