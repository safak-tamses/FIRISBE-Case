package com.firisbe.service.Interface;

import com.firisbe.model.Account;



public interface AccountServiceInterface {
    public Account accountSave(Account account);
    public Boolean creditCardValidation(String cardNumber);

    public Account findAccountById(Long id);
}
