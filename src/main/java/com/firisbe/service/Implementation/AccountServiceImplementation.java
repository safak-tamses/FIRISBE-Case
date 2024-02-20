package com.firisbe.service.Implementation;

import com.firisbe.error.AccountNotFoundException;
import com.firisbe.error.AccountSaveException;
import com.firisbe.error.CreditCardValidation;
import com.firisbe.model.Account;
import com.firisbe.repository.jpa.AccountRepository;
import com.firisbe.service.Interface.AccountServiceInterface;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class AccountServiceImplementation implements AccountServiceInterface {
    private final AccountRepository repo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public Account accountSave(Account account) {
        try {
            repo.save(account);
            kafkaTemplate.send("dbUpdate", "The user named " + " has been successfully updated in the database");
            return account;
        } catch (Exception e) {
            throw new AccountSaveException(e);
        }
    }

    @Override
    public Boolean creditCardValidation(String creditCardNumber) {
        try {
            return repo.findByCreditCardNumber(creditCardNumber).isPresent();
        } catch (Exception e) {
            throw new CreditCardValidation(e);
        }
    }

    @Override
    public Account findAccountById(Long id) {
        try {
            return repo.findById(id).orElseThrow(AccountNotFoundException::new);
        } catch (AccountNotFoundException e) {
            throw new AccountNotFoundException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
