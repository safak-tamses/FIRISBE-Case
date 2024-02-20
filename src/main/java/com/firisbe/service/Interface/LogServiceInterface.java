package com.firisbe.service.Interface;

public interface LogServiceInterface {

    public void successLogListener(String message);

    public void errorLogListener(String message);

    public void paymentLogListener(String message);
}
