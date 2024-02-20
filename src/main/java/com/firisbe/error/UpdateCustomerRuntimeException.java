package com.firisbe.error;

public class UpdateCustomerRuntimeException extends RuntimeException{

    public UpdateCustomerRuntimeException(Throwable cause) {
        super("An error occurred while updating the customer.",cause);
    }

    public UpdateCustomerRuntimeException() {
    }
}
