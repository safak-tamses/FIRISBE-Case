package com.firisbe.service.Implementation;

import com.firisbe.aspect.GenericResponse;
import com.firisbe.aspect.encryption.Encryption;
import com.firisbe.error.*;
import com.firisbe.model.Customer;
import com.firisbe.model.DTO.request.*;
import com.firisbe.model.DTO.response.AuthResponse;
import com.firisbe.model.DTO.response.CustomerResponse;
import com.firisbe.repository.jpa.CustomerRepository;
import com.firisbe.service.JwtService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceImplementationTest {
    @Mock
    private CustomerRepository repo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private CustomerServiceImplementation customerService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Encryption encryption;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(jwtService.extractUsername(anyString())).thenReturn("exampleUser");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void testFindCustomerToToken() {
        // Setup
        String token = "dummyToken";
        String customerEmail = "test@example.com";
        Customer dummyCustomer = new Customer(); // Dummy customer object

        // Mock jwtService behavior
        when(jwtService.extractUsername(anyString())).thenReturn(customerEmail);

        // Mock repository behavior
        when(repo.findCustomerByEmail(customerEmail)).thenReturn(Optional.of(dummyCustomer));

        // Test
        Customer foundCustomer = customerService.findCustomerToToken(token);

        // Assertions or verifications
        assertNotNull(foundCustomer);
        assertEquals(dummyCustomer, foundCustomer);

        // Verify that kafkaTemplate.send() method was called
        verify(kafkaTemplate).send(eq("successful_logs"), anyString());
    }

    @Test
    public void testFindCustomerToToken_CustomerNotFound() {
        // Given
        String token = "Bearer token";
        String customerName = "test@example.com";

        when(jwtService.extractUsername(token)).thenReturn(customerName);
        when(repo.findCustomerByEmail(customerName)).thenReturn(Optional.empty());

        // When/Then
        try {
            customerService.findCustomerToToken(token);
        } catch (CustomerNotFoundException e) {
            assertEquals("Customer not found: ", e.getMessage());
            verify(kafkaTemplate).send(eq("error_logs"), contains("CustomerNotFound"));
        }
    }

    @Test
    public void testFindCustomerToToken_GeneralError() {
        // Given
        String token = "Bearer token";

        when(jwtService.extractUsername(token)).thenThrow(new RuntimeException("Some error"));

        // When/Then
        try {
            customerService.findCustomerToToken(token);
        } catch (RuntimeException e) {
            assertEquals("Customer not found: ", e.getMessage());
            verify(kafkaTemplate).send(eq("error_logs"), contains("CustomerNotFound"));
        }
    }


    @Test
    public void testFindCustomerByMail_CustomerFound() {
        // Setup
        String email = "test@example.com";
        Customer dummyCustomer = new Customer(); // Dummy customer object

        // Mock repository behavior
        when(repo.findCustomerByEmail(email)).thenReturn(Optional.of(dummyCustomer));

        // Test
        Customer foundCustomer = customerService.findCustomerByMail(email);

        // Assertions
        assertNotNull(foundCustomer);
        assertEquals(dummyCustomer, foundCustomer);
    }

    @Test
    public void testFindCustomerByMail_CustomerNotFound() {
        // Setup
        String email = "nonexistent@example.com";

        // Mock repository behavior
        when(repo.findCustomerByEmail(email)).thenReturn(Optional.empty());

        // Test & Assertions
        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.findCustomerByMail(email);
        });

        // Verify that kafkaTemplate.send() method was called
        verify(kafkaTemplate).send(eq("error_logs"), contains("CustomerNotFound"));
    }

    @Test
    public void testFindCustomerByMail_GeneralError() {
        // Setup
        String email = "test@example.com";

        // Mock repository behavior to simulate a general error
        when(repo.findCustomerByEmail(email)).thenThrow(new RuntimeException("Some unexpected error"));

        // Test & Assertions
        assertThrows(RuntimeException.class, () -> {
            customerService.findCustomerByMail(email);
        });

        // Verify that kafkaTemplate.send() method was called
        verify(kafkaTemplate).send(eq("error_logs"), contains("GeneralError"));
    }

    @Test
    void testFindById_Successful() {
        // Given
        Long customerId = 1L;
        Customer dummyCustomer = new Customer(); // Dummy customer object

        // Mock repository behavior
        when(repo.findById(customerId)).thenReturn(Optional.of(dummyCustomer));

        // Test
        Customer foundCustomer = customerService.findById(customerId);

        // Assertions
        assertNotNull(foundCustomer);
        assertEquals(dummyCustomer, foundCustomer);
    }

    @Test
    void testFindById_CustomerNotFound() {
        // Given
        Long customerId = 1L;

        // Mock repository behavior
        when(repo.findById(customerId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.findById(customerId);
        });

        // Verify that kafkaTemplate.send() method was called
        verify(kafkaTemplate).send(eq("error_logs"), contains("CustomerNotFound"));
    }

    @Test
    void testFindById_GeneralError() {
        // Given
        Long customerId = 1L;

        // Mock repository behavior
        when(repo.findById(customerId)).thenThrow(new RuntimeException("Some error"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            customerService.findById(customerId);
        });

        // Verify that kafkaTemplate.send() method was called
        verify(kafkaTemplate).send(eq("error_logs"), contains("GeneralError"));
    }

    @Test
    void testSaveCustomer_Successful() {
        // Given
        Customer dummyCustomer = new Customer(); // Dummy customer object

        // Test
        customerService.saveCustomer(dummyCustomer);

        // Verify that repo.save() method was called
        verify(repo).save(dummyCustomer);
    }

    @Test
    void testSaveCustomer_GeneralError() {
        // Given
        Customer dummyCustomer = new Customer(); // Dummy customer object

        // Mock repository behavior
        doThrow(new RuntimeException("Some error")).when(repo).save(dummyCustomer);

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            customerService.saveCustomer(dummyCustomer);
        });

        // Verify that kafkaTemplate.send() method was called
        verify(kafkaTemplate).send(eq("error_logs"), contains("GeneralError"));
    }

    @Test
    void testIsValidCreditCardNumber_ValidNumber() {
        // Given

        String validNumber = "4539 1488 0343 6467"; // A valid credit card number

        // Test & Assertion
        assertTrue(customerService.isValidCreditCardNumber(validNumber));
    }

    @Test
    void testIsValidCreditCardNumber_InvalidLength() {
        // Given
        String shortNumber = "123456789012"; // Shorter than 13 characters
        String longNumber = "12345678901234567890"; // Longer than 19 characters

        // Test & Assertion
        assertFalse(customerService.isValidCreditCardNumber(shortNumber));
        assertFalse(customerService.isValidCreditCardNumber(longNumber));
    }

    @Test
    void testIsValidCreditCardNumber_InvalidLuhnAlgorithm() {
        // Given
        String invalidNumber = "4539 1488 0343 6466"; // The last digit changed to make it invalid

        // Test & Assertion
        assertFalse(customerService.isValidCreditCardNumber(invalidNumber));
    }

    @Test
    void testUpdateCustomerForCustomers_Successful() {
        // Given
        CustomerUpdateRequest request = new CustomerUpdateRequest("John", "Doe", "john.doe@example.com", "newPassword", "4539 1488 0343 6467");

        // Setup
        String token = "dummyToken";
        String customerEmail = "test@example.com";
        Customer dummyCustomer = new Customer(); // Dummy customer object

        // Mock jwtService behavior
        when(jwtService.extractUsername(anyString())).thenReturn(customerEmail);

        // Mock repository behavior
        when(repo.findCustomerByEmail(customerEmail)).thenReturn(Optional.of(dummyCustomer));

        // Test
        Customer foundCustomer = customerService.findCustomerToToken(token);

        // Mocking repository behavior for email and credit card checks
        when(repo.findCustomerByEmail(request.email())).thenReturn(Optional.empty());
        when(repo.findCustomerByCreditCardNumber(anyString())).thenReturn(Optional.empty());
        // Mocking encryption
        when(encryption.encrypt(request.creditCardNumber())).thenReturn("encryptedCreditCard");
        // Mocking password encoder
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

        // Test
        GenericResponse<CustomerResponse> response = customerService.updateCustomerForCustomers(token, request);

        // Assertions
        assertTrue(response.getStatus());
        assertEquals("John", foundCustomer.getName());
        assertEquals("Doe", foundCustomer.getLastName());
        assertEquals("encodedPassword", foundCustomer.getPassword());
        assertEquals("john.doe@example.com", foundCustomer.getEmail());
        assertEquals(BigDecimal.valueOf(10000), foundCustomer.getBalance());

        // Verify that repo.save() method was called
        verify(repo).save(foundCustomer);

    }

    @Test
    void testCustomerNotFoundException() {
        // Given
        String token = "invalidToken";
        CustomerUpdateRequest request = new CustomerUpdateRequest("John", "Doe", "john.doe@example.com", "newPassword", "4539 1488 0343 6467");
        when(jwtService.extractUsername(anyString())).thenReturn("test@example.com");
        when(repo.findCustomerByEmail(anyString())).thenReturn(Optional.empty());

        // When - Then
        assertThrows(CustomerNotFoundException.class, () -> customerService.updateCustomerForCustomers(token, request));

    }

    @Test
    void testUpdateCustomerRuntimeException() {
        // Given
        String token = "validToken";
        CustomerUpdateRequest request = new CustomerUpdateRequest("John", "Doe", "john.doe@example.com", "newPassword", "4539 1488 0343 6467");
        when(jwtService.extractUsername(anyString())).thenReturn("test@example.com");
        when(repo.findCustomerByEmail(anyString())).thenReturn(Optional.of(new Customer()));

        // When
        when(repo.save(any(Customer.class))).thenThrow(new RuntimeException("Test exception"));

        // Then
        assertThrows(UpdateCustomerRuntimeException.class, () -> customerService.updateCustomerForCustomers(token, request));
        verify(kafkaTemplate).send(eq("error_logs"), anyString());
    }

    @Test
    void testUpdateCustomerForAdmin_Successful() {
        // Given
        Customer dummyCustomer = new Customer(); // Dummy customer object
        dummyCustomer.setId(123L); // Set dummy customer ID
        AdminCustomerUpdateRequest request = new AdminCustomerUpdateRequest(
                dummyCustomer.getId(), "John", "Doe", "admin@mail.com", "newPassword", "4539 1488 0343 6467");

        when(repo.findById(request.id())).thenReturn(Optional.of(dummyCustomer));
        when(repo.findCustomerByEmail(request.email())).thenReturn(Optional.empty());
        when(repo.findCustomerByCreditCardNumber(anyString())).thenReturn(Optional.empty());
        when(encryption.encrypt(request.creditCardNumber())).thenReturn("encryptedCreditCard");
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

        // Test
        GenericResponse<CustomerResponse> response = customerService.updateCustomerForAdmin(request);

        // Assertions
        assertTrue(response.getStatus());
        assertEquals("John", dummyCustomer.getName());
        assertEquals("Doe", dummyCustomer.getLastName());
        assertEquals("encodedPassword", dummyCustomer.getPassword());
        assertEquals("admin@mail.com", dummyCustomer.getEmail());
        assertEquals(BigDecimal.valueOf(10000), dummyCustomer.getBalance());

        // Verify that repo.save() method was called
        verify(repo).save(dummyCustomer);
    }


    @Test
    void testCustomerNotFoundExceptionAdmin() {
        // Given
        String token = "invalidToken";
        AdminCustomerUpdateRequest request = new AdminCustomerUpdateRequest(1L, "John", "Doe", "john.doe@example.com", "newPassword", "4539 1488 0343 6467");
        when(jwtService.extractUsername(anyString())).thenReturn("test@example.com");
        when(repo.findCustomerByEmail(anyString())).thenReturn(Optional.empty());

        // When - Then
        assertThrows(CustomerNotFoundException.class, () -> customerService.updateCustomerForAdmin(request));

    }

    @Test
    void testUpdateCustomerRuntimeExceptionAdmin() {
        // Given
        String token = "validToken";
        AdminCustomerUpdateRequest request = new AdminCustomerUpdateRequest(1L, "John", "Doe", "john.doe@example.com", "newPassword", "4539 1488 0343 6467");
        when(jwtService.extractUsername(anyString())).thenReturn("test@example.com");
        when(repo.findCustomerByEmail(anyString())).thenReturn(Optional.of(new Customer()));

        // When
        when(repo.save(any(Customer.class))).thenThrow(new RuntimeException("Test exception"));

        // Then
        assertThrows(CustomerNotFoundException.class, () -> customerService.updateCustomerForAdmin(request));
        verify(kafkaTemplate).send(eq("error_logs"), anyString());
    }

    @Test
    void testReadCustomerForAdmin_Successful() {
        // Given
        Long customerId = 1L;
        Customer dummyCustomer = new Customer();
        dummyCustomer.setId(customerId);
        dummyCustomer.setName("John");
        dummyCustomer.setLastName("Doe");
        dummyCustomer.setEmail("john.doe@example.com");

        when(repo.findById(customerId)).thenReturn(Optional.of(dummyCustomer));

        // When
        GenericResponse<CustomerResponse> response = customerService.readCustomerForAdmin(customerId);

        // Then
        assertTrue(response.getStatus());
        assertEquals("John", response.getData().name());
        assertEquals("Doe", response.getData().lastName());
        assertEquals("john.doe@example.com", response.getData().email());

        verify(kafkaTemplate).send("successful_logs", "The user named John has been successfully read in the database");
    }

    @Test
    void testReadCustomerForAdmin_CustomerNotFoundException() {
        // Given
        Long customerId = 1L;

        when(repo.findById(customerId)).thenReturn(Optional.empty());

        // When - Then
        assertThrows(CustomerNotFoundException.class, () -> customerService.readCustomerForAdmin(customerId));
        verify(kafkaTemplate).send(eq("error_logs"), anyString());
    }

    @Test
    void testReadCustomerForAdmin_RuntimeException() {
        // Given
        Long customerId = 1L;

        when(repo.findById(customerId)).thenThrow(new RuntimeException("Test exception"));

        // When - Then
        assertThrows(RuntimeException.class, () -> customerService.readCustomerForAdmin(customerId));
        verify(kafkaTemplate).send(eq("error_logs"), anyString());
    }

    @Test
    void testReadCustomerForCustomers_Successful() {
        // Given
        String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        Customer mockCustomer = new Customer();
        mockCustomer.setName("John");
        mockCustomer.setLastName("Doe");
        mockCustomer.setEmail("john.doe@example.com");
        mockCustomer.setBalance(BigDecimal.valueOf(10000));
        mockCustomer.setCreditCardNumber("4539 1488 0343 6467");
        mockCustomer.setPassword("password");
        when(jwtService.extractUsername(anyString())).thenReturn("john.doe@example.com");
        when(repo.findCustomerByEmail(anyString())).thenReturn(Optional.of(mockCustomer));

        // When
        GenericResponse<CustomerResponse> response = customerService.readCustomerForCustomers(token);

        // Then
        assertTrue(response.getStatus());
        assertNotNull(response.getData());
        assertEquals("John", response.getData().name());
        assertEquals("Doe", response.getData().lastName());
        assertEquals("john.doe@example.com", response.getData().email());
    }

    @Test
    void testReadCustomerForCustomers_CustomerNotFoundException() {
        // Given
        String token = "Bearer invalidToken";
        when(jwtService.extractUsername(anyString())).thenThrow(new RuntimeException("Invalid token"));

        // When/Then
        assertThrows(CustomerNotFoundException.class, () -> customerService.readCustomerForCustomers(token));
        verify(kafkaTemplate, times(1)).send(eq("error_logs"), contains("CustomerNotFound"));
    }

    @Test
    void testReadCustomerForCustomers_RuntimeException() {
        // Given
        String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        when(jwtService.extractUsername(anyString())).thenReturn("john.doe@example.com");
        when(repo.findCustomerByEmail(anyString())).thenThrow(new RuntimeException("Database connection failed"));

        // When/Then
        assertThrows(RuntimeException.class, () -> customerService.readCustomerForCustomers(token));
        verify(kafkaTemplate, times(1)).send(eq("error_logs"), contains("Failed to read in the database"));
    }

    @Test
    void testReadAllForAdmin_Successful() {
        // Given
        List<Customer> customers = new ArrayList<>();
        Customer customer1 = new Customer();
        customer1.setName("John");
        customer1.setLastName("Doe");
        customer1.setEmail("john.doe@example.com");
        customer1.setBalance(BigDecimal.valueOf(10000));
        customer1.setCreditCardNumber("4539 1488 0343 6467");
        customer1.setPassword("password");
        Customer customer2 = new Customer();
        customer2.setName("Jane");
        customer2.setLastName("Smith");
        customer2.setEmail("jane.smith@example.com");
        customer2.setBalance(BigDecimal.valueOf(10000));
        customer2.setCreditCardNumber("4539 1488 0343 6467");
        customer2.setPassword("password");
        customers.add(customer1);
        customers.add(customer2);

        when(repo.findAll()).thenReturn(customers);


        // When
        GenericResponse<List<CustomerResponse>> response = customerService.readAllForAdmin();

        // Then
        assertTrue(response.getStatus());
        assertNotNull(response.getData());
        assertEquals(2, response.getData().size());
        assertEquals("John", response.getData().getFirst().name());
        assertEquals("Doe", response.getData().getFirst().lastName());
        assertEquals("john.doe@example.com", response.getData().get(0).email());
        assertEquals("Jane", response.getData().get(1).name());
        assertEquals("Smith", response.getData().get(1).lastName());
        assertEquals("jane.smith@example.com", response.getData().get(1).email());
        verify(kafkaTemplate, times(1)).send(eq("successful_logs"), anyString());
    }

    @Test
    void testReadAllForAdmin_Exception() {
        // Given
        when(repo.findAll()).thenThrow(new RuntimeException("Database connection failed"));

        // When/Then
        assertThrows(RuntimeException.class, () -> customerService.readAllForAdmin());
        verify(kafkaTemplate, times(1)).send(eq("error_logs"), anyString());
    }

    @Test
    void testDeleteCustomerForAdmin_Successful() {
        // Given
        Long customerId = 1L;
        Customer mockCustomer = new Customer();
        mockCustomer.setId(customerId);
        mockCustomer.setName("John");
        mockCustomer.setLastName("Doe");
        mockCustomer.setEmail("john.doe@example.com");
        mockCustomer.setBalance(BigDecimal.valueOf(10000));
        mockCustomer.setCreditCardNumber("4539 1488 0343 6467");
        mockCustomer.setPassword("password");
        when(repo.findById(customerId)).thenReturn(Optional.of(mockCustomer));

        // When
        GenericResponse<String> response = customerService.deleteCustomerForAdmin(customerId);

        // Then
        assertTrue(response.getStatus());
        assertEquals("Customer deleted successfully.", response.getData());
        verify(repo, times(1)).delete(eq(mockCustomer));
        verify(kafkaTemplate, times(1)).send(eq("successful_logs"), anyString());
    }

    @Test
    void testDeleteCustomerForAdmin_CustomerNotFoundException() {
        // Given
        Long customerId = 1L;
        when(repo.findById(customerId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(CustomerNotFoundException.class, () -> customerService.deleteCustomerForAdmin(customerId));
        verify(kafkaTemplate, times(1)).send(eq("error_logs"), contains("Failed to delete"));
    }

    @Test
    void testDeleteCustomerForAdmin_RuntimeException() {
        // Given
        Long customerId = 1L;
        when(repo.findById(customerId)).thenThrow(new RuntimeException("Database connection failed"));

        // When/Then
        assertThrows(RuntimeException.class, () -> customerService.deleteCustomerForAdmin(customerId));
        verify(kafkaTemplate, times(1)).send(eq("error_logs"), contains("Failed to delete"));
    }

    @Test
    void testDeleteCustomerForCustomers_Successful() {
        // Given
        String token = "valid_token";
        Customer mockCustomer = new Customer();
        mockCustomer.setName("John");
        mockCustomer.setLastName("Doe");
        mockCustomer.setEmail("john.doe@example.com");
        mockCustomer.setBalance(BigDecimal.valueOf(10000));
        mockCustomer.setCreditCardNumber("4539 1488 0343 6467");
        mockCustomer.setPassword("password");
        when(jwtService.extractUsername(anyString())).thenReturn("john.doe@example.com");
        when(repo.findCustomerByEmail(anyString())).thenReturn(Optional.of(mockCustomer));

        // When
        GenericResponse<String> response = customerService.deleteCustomerForCustomers(token);

        // Then
        assertTrue(response.getStatus());
        assertEquals("Customer deleted successfully.", response.getData());
        verify(repo, times(1)).delete(eq(mockCustomer));
    }

    @Test
    void testDeleteCustomerForCustomers_CustomerNotFoundException() {
        // Given
        String token = "invalid_token";
        Customer mockCustomer = new Customer();
        mockCustomer.setName("John");
        mockCustomer.setLastName("Doe");
        mockCustomer.setEmail("john.doe@example.com");
        mockCustomer.setBalance(BigDecimal.valueOf(10000));
        mockCustomer.setCreditCardNumber("4539 1488 0343 6467");
        mockCustomer.setPassword("password");
        when(jwtService.extractUsername(anyString())).thenReturn("john.doe@example.com");
        when(repo.findCustomerByEmail(anyString())).thenThrow(new CustomerNotFoundException());

        // When/Then
        assertThrows(CustomerNotFoundException.class, () -> customerService.deleteCustomerForCustomers(token));
        verify(kafkaTemplate, times(1)).send(eq("error_logs"), contains("Failed to delete"));
    }

    @Test
    void testDeleteAllForAdmin_Successful() {
        // Given

        // When
        GenericResponse<String> response = customerService.deleteAllForAdmin();

        // Then
        assertTrue(response.getStatus());
        assertEquals("All customers deleted successfully.", response.getData());
        verify(repo, times(1)).deleteAll();
        verify(kafkaTemplate, times(1)).send(eq("successful_logs"), anyString());
    }

    @Test
    void testDeleteAllForAdmin_ExceptionThrown() {
        // Given
        doThrow(new RuntimeException("Database connection failed")).when(repo).deleteAll();

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> customerService.deleteAllForAdmin());
        assertEquals("java.lang.RuntimeException: Database connection failed", exception.getMessage());
        verify(kafkaTemplate, times(1)).send(eq("error_logs"), contains("An error was encountered"));
    }

    @Test
    void testCustomerLogin_Successful() {
        // Given
        CustomerCredentials customerCredentials = new CustomerCredentials("username", "password");
        Customer mockCustomer = new Customer();
        mockCustomer.setName("John");
        mockCustomer.setEmail("username");
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(null);
        when(repo.findCustomerByEmail(eq(customerCredentials.username()))).thenReturn(Optional.of(mockCustomer));
        when(jwtService.generateToken(any(Customer.class))).thenReturn("jwtToken");

        // When
        GenericResponse<AuthResponse> response = customerService.customerLogin(customerCredentials);

        // Then
        assertTrue(response.getStatus());
        assertNotNull(response.getData());
        assertEquals("Login successful", response.getData().message());
        assertEquals("jwtToken", response.getData().token());
        verify(kafkaTemplate, times(1)).send(eq("successful_logs"), anyString());
    }

    @Test
    void testCustomerLogin_AuthenticationException() {
        // Given
        CustomerCredentials customerCredentials = new CustomerCredentials("username", "password");
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new AuthenticationException("Invalid credentials") {
        });

        // When/Then
        assertThrows(RuntimeException.class, () -> customerService.customerLogin(customerCredentials));
        verify(kafkaTemplate, times(1)).send(eq("error_logs"), anyString());
    }

    @Test
    void testCustomerRegister_Successful() {
        // Given
        CustomerCreateRequest request = new CustomerCreateRequest("John", "Doe", "john.doe@example.com", "password");

        when(repo.findCustomerByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(Customer.class))).thenReturn("jwtToken");

        // When
        GenericResponse<AuthResponse> response = customerService.customerRegister(request);

        // Then
        assertTrue(response.getStatus());
        assertEquals("Register successful", response.getData().message());
        assertNotNull(response.getData().token());
        verify(repo, times(1)).save(any(Customer.class));
        verify(kafkaTemplate, times(1)).send(eq("successful_logs"), anyString());
    }

    @Test
    void testCustomerRegister_CustomerAlreadyExists() {
        // Given
        CustomerCreateRequest request = new CustomerCreateRequest("John", "Doe", "john.doe@example.com", "password");
        when(repo.findCustomerByEmail(request.email())).thenThrow(new CustomerAlreadyExistsException());

        // When/Then
        assertThrows(CustomerAlreadyExistsException.class, () -> customerService.customerRegister(request));
        verify(kafkaTemplate, times(1)).send(eq("error_logs"), contains("CustomerAlreadyExists"));
    }

    @Test
    public void testAddPaymentMethod_Success() {
        // Setup
        // Given
        String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        Customer mockCustomer = new Customer();
        mockCustomer.setName("John");
        mockCustomer.setLastName("Doe");
        mockCustomer.setEmail("john.doe@example.com");
        mockCustomer.setPassword("password");
        when(jwtService.extractUsername(anyString())).thenReturn("john.doe@example.com");
        when(repo.findCustomerByEmail(anyString())).thenReturn(Optional.of(mockCustomer));

        // When
        PaymentMethodRequest request = new PaymentMethodRequest("mockCreditCardNumber");


// Mock findCustomerToToken to return a mock customer

// Test
        GenericResponse<String> response = customerService.addPaymentMethod(token, request);

        // Verify
        assertTrue(response.getStatus());
        assertEquals("Payment method added successfully", response.getData());

    }

    @Test
    void testAddPaymentMethod_CreditCardAlreadyExist() {
        // Given
        String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        Customer mockCustomer = new Customer();
        mockCustomer.setName("John");
        mockCustomer.setLastName("Doe");
        mockCustomer.setEmail("john.doe@example.com");
        mockCustomer.setPassword("password");
        when(jwtService.extractUsername(anyString())).thenReturn("john.doe@example.com");
        when(repo.findCustomerByEmail(anyString())).thenReturn(Optional.of(mockCustomer));
        PaymentMethodRequest request = new PaymentMethodRequest("4539 1488 0343 6467");

        when(customerService.findCustomerToToken(token)).thenThrow(new CreditCardAlreadyExist());

        // When/Then
        assertThrows(RuntimeException.class, () -> customerService.addPaymentMethod(token, request));
    }

    @Test
    void testAddPaymentMethod_CreditCardNumberAlreadyExist() {
        // Given
        String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        Customer mockCustomer = new Customer();
        mockCustomer.setName("John");
        mockCustomer.setLastName("Doe");
        mockCustomer.setEmail("john.doe@example.com");
        mockCustomer.setPassword("password");
        when(jwtService.extractUsername(anyString())).thenReturn("john.doe@example.com");
        when(repo.findCustomerByEmail(anyString())).thenReturn(Optional.of(mockCustomer));
        PaymentMethodRequest request = new PaymentMethodRequest("4539 1488 0343 6467");

        when(customerService.findCustomerToToken(token)).thenThrow(new CreditCardNumberAlreadyExist());

        // When/Then
        assertThrows(RuntimeException.class, () -> customerService.addPaymentMethod(token, request));
    }

    @Test
    public void testDeleteCustomerForAdmin_Success() {
        // Setup
        Long customerId = 1L;
        Customer mockCustomer = new Customer();
        mockCustomer.setId(customerId);
        mockCustomer.setName("Mock Customer");

        when(repo.findById(customerId)).thenReturn(Optional.of(mockCustomer));

        // Test
        GenericResponse<String> response = customerService.deleteCustomerForAdmin(customerId);

        // Verify
        assertTrue(response.getStatus());
        assertEquals("Customer deleted successfully.", response.getData());
        verify(repo, times(1)).delete(mockCustomer);
    }

    @Test
    public void testDeleteCustomerForAdmin_CustomerNotFound() {
        // Setup
        Long customerId = 1L;

        when(repo.findById(customerId)).thenReturn(Optional.empty());

        // Test
        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.deleteCustomerForAdmin(customerId);
        });

        // Verify
        verify(kafkaTemplate, times(1)).send(eq("error_logs"), anyString());
    }

    @Test
    public void testDeleteCustomerForAdmin_Exception() {
        // Setup
        Long customerId = 1L;

        when(repo.findById(customerId)).thenThrow(new RuntimeException("Database connection failed"));

        // Test
        assertThrows(RuntimeException.class, () -> {
            customerService.deleteCustomerForAdmin(customerId);
        });

        // Verify
        verify(kafkaTemplate, times(1)).send(eq("error_logs"), anyString());
    }

}