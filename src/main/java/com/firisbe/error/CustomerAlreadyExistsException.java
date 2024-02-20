package com.firisbe.error;

public class CustomerAlreadyExistsException extends RuntimeException{
    public CustomerAlreadyExistsException(Throwable cause) {
        super("Customer already exists.", cause);
    }

    public CustomerAlreadyExistsException() {
    }
}
