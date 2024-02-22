package com.firisbe.repository.jpa;

import com.firisbe.model.Customer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerRepositoryTest {
    @Mock
    private CustomerRepository customerRepository;
    @Test
    void testFindCustomerByName() {
        String name = "John Doe";
        Customer customer = new Customer();
        customer.setName(name);

        when(customerRepository.findCustomerByName(name)).thenReturn(Optional.of(customer));

        Optional<Customer> found = customerRepository.findCustomerByName(name);

        assertEquals(name, found.get().getName());
    }

    @Test
    void testFindCustomerByEmail() {
        String email = "john@example.com";
        Customer customer = new Customer();
        customer.setEmail(email);

        when(customerRepository.findCustomerByEmail(email)).thenReturn(Optional.of(customer));

        Optional<Customer> found = customerRepository.findCustomerByEmail(email);

        assertEquals(email, found.get().getEmail());
    }

    @Test
    void testFindCustomerByCreditCardNumber() {
        String creditCardNumber = "1234567890123456";
        Customer customer = new Customer();
        customer.setCreditCardNumber(creditCardNumber);

        when(customerRepository.findCustomerByCreditCardNumber(creditCardNumber)).thenReturn(Optional.of(customer));

        Optional<Customer> found = customerRepository.findCustomerByCreditCardNumber(creditCardNumber);

        assertEquals(creditCardNumber, found.get().getCreditCardNumber());
    }
}