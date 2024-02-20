package com.firisbe.repository.jpa;

import com.firisbe.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {
    Optional<Customer> findCustomerByName(String name);
    Optional<Customer> findCustomerByEmail(String email);
    Optional<Customer> findCustomerByCreditCardNumber(String creditCardNumber);

}
