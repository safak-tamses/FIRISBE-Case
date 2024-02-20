package com.firisbe.service.Implementation;

import com.firisbe.aspect.GenericResponse;
import com.firisbe.error.CustomerAlreadyExistsException;
import com.firisbe.error.CustomerNotFoundException;
import com.firisbe.error.UpdateCustomerRuntimeException;
import com.firisbe.model.Account;
import com.firisbe.model.Customer;
import com.firisbe.model.DTO.request.CustomerCreateRequest;
import com.firisbe.model.DTO.request.CustomerCredentials;
import com.firisbe.model.DTO.request.CustomerUpdateRequest;
import com.firisbe.model.DTO.response.AuthResponse;
import com.firisbe.model.DTO.response.CustomerResponse;
import com.firisbe.model.Enum.Role;
import com.firisbe.repository.jpa.CustomerRepository;
import com.firisbe.service.JwtService;
import com.firisbe.service.Interface.CustomerServiceInterface;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CustomerServiceImplementation implements CustomerServiceInterface {
    private final CustomerRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AccountServiceImplementation accountService;

    /* Bu method bearer token ile customer'ı bulmasını sağlıyor. */
    public Customer findCustomerToToken(String token) {
        try {
            token = token.substring(7);
            final String customerName = jwtService.extractUsername(token);
            return repo.findCustomerByEmail(customerName).orElseThrow(CustomerNotFoundException::new);
        } catch (CustomerNotFoundException e) {
            throw new CustomerNotFoundException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Customer findCustomerByMail(String mail) {
        try {
            return repo.findCustomerByEmail(mail).orElseThrow(CustomerNotFoundException::new);
        } catch (CustomerNotFoundException e) {
            throw new CustomerNotFoundException(e);
        } catch (Exception e) {
            throw new RuntimeException("Someting gone wrong", e);
        }
    }

    /* Bu method müşterinin kendi müşteri profilini güncellemesini sağlamaktadır. E-posta değişikliği ayrıca kontrol edilmiştir. */
    @Override
    public GenericResponse<CustomerResponse> updateCustomerForCustomers(String token, CustomerUpdateRequest request) {
        try {
            Customer c = findCustomerToToken(token);
            if (c.getEmail().equals(request.email())) {
                if (request.id() != null && c.getId().equals(request.id())) {
                    c.setName(request.name());
                    c.setLastName(request.lastName());
                    c.setPassword(passwordEncoder.encode(request.password()));
                    repo.save(c);

                    kafkaTemplate.send("dbUpdate", "The user named " + request.name() + " has been successfully updated in the database");

                    CustomerResponse response = new CustomerResponse(c.getName(), c.getLastName(), c.getEmail());
                    return new GenericResponse<>(response, true);
                } else {
                    throw new UpdateCustomerRuntimeException();
                }
            } else {
                if (request.id() != null && c.getId().equals(request.id()) && repo.findCustomerByEmail(request.email()).isEmpty()) {
                    c.setName(request.name());
                    c.setLastName(request.lastName());
                    c.setEmail(request.email());
                    c.setPassword(passwordEncoder.encode(request.password()));
                    repo.save(c);

                    kafkaTemplate.send("dbUpdate", "The user named " + request.name() + " has been successfully updated in the database");

                    CustomerResponse response = new CustomerResponse(c.getName(), c.getLastName(), c.getEmail());
                    return new GenericResponse<>(response, true);
                } else {
                    throw new UpdateCustomerRuntimeException();
                }
            }
        } catch (CustomerNotFoundException e) {
            kafkaTemplate.send("dbUpdate", "Failed to update the user named " + request.name() + " in the database. Reason: " + e.getMessage());
            throw new CustomerNotFoundException(e);
        } catch (UpdateCustomerRuntimeException e) {
            kafkaTemplate.send("dbUpdate", "Failed to update the user named " + request.name() + " in the database. Reason: " + e.getMessage());
            throw new UpdateCustomerRuntimeException(e);
        } catch (Exception e) {
            kafkaTemplate.send("dbUpdate", "Failed to update the user named " + request.name() + " in the database. Reason: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /* Bu method admin hesabının belirli bir müşteriyi güncellemesini sağlamaktadır. */
    @Override
    public GenericResponse<CustomerResponse> updateCustomerForAdmin(CustomerUpdateRequest request) {
        try {
            Customer c = repo.findById(request.id()).orElseThrow(RuntimeException::new);
            if (c.getId().equals(request.id())) {
                c.setName(request.name());
                c.setLastName(request.lastName());
                c.setEmail(request.email());
                c.setPassword(passwordEncoder.encode(request.password()));
                repo.save(c);

                kafkaTemplate.send("dbUpdate", "The user named " + request.name() + " has been successfully updated in the database");

                CustomerResponse response = new CustomerResponse(c.getName(), c.getLastName(), c.getEmail());
                return new GenericResponse<>(response, true);
            }
            throw new UpdateCustomerRuntimeException();
        } catch (CustomerNotFoundException e) {
            kafkaTemplate.send("dbUpdate", "Failed to update the user named " + request.name() + " in the database. Reason: " + e.getMessage());
            throw new CustomerNotFoundException(e);
        } catch (UpdateCustomerRuntimeException e) {
            kafkaTemplate.send("dbUpdate", "Failed to update the user named " + request.name() + " in the database. Reason: " + e.getMessage());
            throw new UpdateCustomerRuntimeException(e);
        } catch (Exception e) {
            kafkaTemplate.send("dbUpdate", "Failed to update the user named " + request.name() + " in the database. Reason: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /* Bu method admin hesabının belirli bir kullanıcının bilgilerini görüntülemesini sağlar. */
    @Override
    public GenericResponse<CustomerResponse> readCustomerForAdmin(Long id) {
        try {
            Customer c = repo.findById(id).orElseThrow(CustomerNotFoundException::new);
            CustomerResponse response = new CustomerResponse(c.getName(), c.getLastName(), c.getEmail());
            kafkaTemplate.send("dbRead", "The user named " + c.getName() + " has been successfully read in the database");
            return new GenericResponse<>(response, true);
        } catch (CustomerNotFoundException e) {
            kafkaTemplate.send("dbRead", "Failed to read in the database. Reason: " + e.getMessage());
            throw new CustomerNotFoundException(e);
        } catch (Exception e) {
            kafkaTemplate.send("dbRead", "Failed to read in the database. Reason: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /* Bu method müşterinin kendi müşteri hesabını görüntülemesini sağlar. */
    @Override
    public GenericResponse<CustomerResponse> readCustomerForCustomers(String token, Long id) {
        try {
            Customer test = findCustomerToToken(token);
            Customer c = repo.findById(id).orElseThrow(CustomerNotFoundException::new);
            if (test.getId().equals(c.getId())) {
                CustomerResponse response = new CustomerResponse(c.getName(), c.getLastName(), c.getEmail());
                kafkaTemplate.send("dbRead", "The user named " + c.getName() + " has been successfully read in the database");
                return new GenericResponse<>(response, true);
            } else throw new CustomerNotFoundException();
        } catch (CustomerNotFoundException e) {
            kafkaTemplate.send("dbRead", "Failed to read in the database. Reason: " + e.getMessage());
            throw new CustomerNotFoundException(e);
        } catch (Exception e) {
            kafkaTemplate.send("dbRead", "Failed to read in the database. Reason: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /* Bu method admin hesabının tüm müşterileri görüntülemesini sağlıyor. */
    @Override
    public GenericResponse<List<CustomerResponse>> readAllForAdmin() {
        try {
            List<CustomerResponse> responseList = repo.findAll().stream().map(customer -> new CustomerResponse(customer.getName(), customer.getLastName(), customer.getEmail())).toList();
            kafkaTemplate.send("dbRead", "All data in the database was retrieved successfully");
            return new GenericResponse<>(responseList, true);
        } catch (Exception e) {
            kafkaTemplate.send("dbRead", "An error was encountered while retrieving all data from the database. The reason is: " + e.getMessage());
            throw new RuntimeException();
        }
    }

    /* Bu method admin hesabının belirli bir kullanıcıyı silmesini sağlıyor. */
    @Override
    public GenericResponse<String> deleteCustomerForAdmin(Long id) {
        try {
            Customer c = repo.findById(id).orElseThrow(CustomerNotFoundException::new);
            repo.delete(c);
            kafkaTemplate.send("dbDelete", "The user named " + c.getName() + " has been successfully deleted in the database");
            return new GenericResponse<>("Customer deleted successfully.", true);
        } catch (CustomerNotFoundException e) {
            kafkaTemplate.send("dbDelete", "Failed to delete in the database. Reason: " + e.getMessage());
            throw new CustomerNotFoundException(e);
        } catch (Exception e) {
            kafkaTemplate.send("dbDelete", "Failed to delete in the database. Reason: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /* Bu method müşterinin kendi müşteri hesabını silmesini sağlar. */
    @Override
    public GenericResponse<String> deleteCustomerForCustomers(String token, Long id) {
        try {
            Customer test = findCustomerToToken(token);
            Customer c = repo.findById(id).orElseThrow(CustomerNotFoundException::new);
            if (test.getId().equals(c.getId())) {
                repo.delete(c);
                kafkaTemplate.send("dbDelete", "The user named " + c.getName() + " has been successfully deleted in the database");
                return new GenericResponse<>("Customer deleted successfully.", true);
            } else throw new CustomerNotFoundException();
        } catch (CustomerNotFoundException e) {
            kafkaTemplate.send("dbDelete", "Failed to delete in the database. Reason: " + e.getMessage());
            throw new CustomerNotFoundException(e);
        } catch (Exception e) {
            kafkaTemplate.send("dbDelete", "Failed to delete in the database. Reason: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /* Bu method admin hesabının tüm müşterileri silmesini sağlıyor. */
    @Override
    public GenericResponse<String> deleteAllForAdmin() {
        try {
            repo.deleteAll();
            kafkaTemplate.send("dbDelete", "All data has been successfully deleted from the database");
            return new GenericResponse<>("All customers deleted successfully.", true);
        } catch (Exception e) {
            kafkaTemplate.send("dbDelete", "An error was encountered while deleting all data from the database. The reason for the error is: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /* Bu method kullanıcının ismi ve şifresi ile giriş yapmasını sağlıyor. */
    @Override
    public GenericResponse<AuthResponse> customerLogin(CustomerCredentials customerCredentials) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            customerCredentials.username(),
                            customerCredentials.password()
                    )
            );
            var customer = repo.findCustomerByEmail(customerCredentials.username())
                    .orElseThrow();
            var jwtToken = jwtService.generateToken(customer);

            AuthResponse response = new AuthResponse(
                    "Login successful", jwtToken
            );
            kafkaTemplate.send("dbCreate", "Login successful.");
            return new GenericResponse<>(response, true);
        } catch (Exception e) {
            kafkaTemplate.send("dbCreate", "Login failed.");
            throw new RuntimeException();
        }
    }

    /* Bu method kullanıcının hesap oluşturmasını sağlıyor. */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public GenericResponse<AuthResponse> customerRegister(CustomerCreateRequest request) {
        try {
            Optional<Customer> checkCustomer = repo.findCustomerByEmail(request.email());
            if (checkCustomer.isEmpty()
                    && Account.isValidFormat(request.creditCardNumber())
                    && !accountService.creditCardValidation(request.creditCardNumber())) {
                Account account = Account.builder()
                        /* Hesaba bakiye nasıl aktarılacak normalde kredi kartı servisi bakiye kontrolü yapar. Bu case de her hesaba belirli bir miktar verdim. */
                        .balance(BigDecimal.valueOf(1000))
                        .creditCardNumber(passwordEncoder.encode(request.creditCardNumber()))
                        .build();
                Customer customer = Customer.builder()
                        .name(request.name())
                        .lastName(request.lastName())
                        .password(passwordEncoder.encode(request.password()))
                        .email(request.email())
                        .role(Role.ROLE_USER)
                        .account(account)
                        .sentTransfers(new ArrayList<>())
                        .receivedTransfers(new ArrayList<>())
                        .build();
                account.setCustomer(customer);
                repo.save(customer);


                var jwtToken = jwtService.generateToken(customer);

                AuthResponse response = new AuthResponse("Register successful", jwtToken
                );

                kafkaTemplate.send("dbCreate", "The user named " + request.name() + " has been successfully registered in the database");
                return new GenericResponse<>(response, true);
            }
            throw new CustomerAlreadyExistsException();
        } catch (CustomerAlreadyExistsException e) {
            kafkaTemplate.send("dbCreate", "Failed to register the user named " + request.name() + " in the database. Reason: " + e.getMessage());
            throw new CustomerAlreadyExistsException(e);
        } catch (Exception e) {
            kafkaTemplate.send("dbCreate", "Failed to register the user named " + request.name() + " in the database. Reason: " + e.getMessage());
            throw new RuntimeException(e);
        }


    }


}
