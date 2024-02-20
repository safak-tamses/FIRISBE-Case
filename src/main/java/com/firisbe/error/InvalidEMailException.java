package com.firisbe.error;

public class InvalidEMailException extends RuntimeException{
    public InvalidEMailException() {
    }

    public InvalidEMailException(Throwable cause) {
        super("Mail already used: " + cause.getMessage());
    }
}
