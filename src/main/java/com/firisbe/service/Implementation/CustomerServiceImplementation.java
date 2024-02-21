package com.firisbe.service.Implementation;

import com.firisbe.aspect.GenericResponse;
import com.firisbe.aspect.encryption.Encryption;
import com.firisbe.error.*;
import com.firisbe.model.Customer;
import com.firisbe.model.DTO.request.*;
import com.firisbe.model.DTO.response.AuthResponse;
import com.firisbe.model.DTO.response.CustomerResponse;
import com.firisbe.model.DTO.response.MonthlyStatisticsResponse;
import com.firisbe.model.Enum.Role;
import com.firisbe.model.Transfer;
import com.firisbe.repository.jpa.CustomerRepository;
import com.firisbe.service.JwtService;
import com.firisbe.service.Interface.CustomerServiceInterface;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
    private final Encryption encryption;

    /* Bu method bearer token ile customer'ı bulmasını sağlıyor. */
    public Customer findCustomerToToken(String token) {
        try {
            token = token.substring(7);
            final String customerName = jwtService.extractUsername(token);
            return repo.findCustomerByEmail(customerName).orElseThrow(CustomerNotFoundException::new);
        } catch (CustomerNotFoundException e) {
            kafkaTemplate.send("error_logs", "CustomerNotFound: Failed to find the user in the database. Reason: " + e.getMessage());
            throw new CustomerNotFoundException(e);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "GeneralError: Failed to find the user in the database. Reason: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Customer findCustomerByMail(String mail) {
        try {
            return repo.findCustomerByEmail(mail).orElseThrow(CustomerNotFoundException::new);
        } catch (CustomerNotFoundException e) {
            kafkaTemplate.send("error_logs", "CustomerNotFound: Failed to find the user in the database. Reason: " + e.getMessage());
            throw new CustomerNotFoundException(e);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "GeneralError: Failed to find the user in the database. Reason: " + e.getMessage());
            throw new RuntimeException("Someting gone wrong", e);
        }
    }

    public Customer findById(Long id) {
        try {
            return repo.findById(id).orElseThrow(CustomerNotFoundException::new);
        } catch (CustomerNotFoundException e) {
            kafkaTemplate.send("error_logs", "CustomerNotFound: Failed to find the user in the database. Reason: " + e.getMessage());
            throw new CustomerNotFoundException(e);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "GeneralError: Failed to find the user in the database. Reason: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public void saveCustomer(Customer customer) {
        try {
            repo.save(customer);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "GeneralError: Failed to save the user in the database. Reason: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private boolean isValidCreditCardNumber(String cardNumber) {
        // Temizleme: Sadece rakamları içeren bir dize oluştur
        String cleanedNumber = cardNumber.replaceAll("[-\\s]+", "");

        // Uzunluk kontrolü
        if (cleanedNumber.length() < 13 || cleanedNumber.length() > 19) {
            return false;
        }

        // Luhn algoritması kontrolü
        int sum = 0;
        boolean alternate = false;
        for (int i = cleanedNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cleanedNumber.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }


    /* Bu method müşterinin kendi müşteri profilini güncellemesini sağlamaktadır. E-posta değişikliği ayrıca kontrol edilmiştir. */
    @Override
    public GenericResponse<CustomerResponse> updateCustomerForCustomers(String token, CustomerUpdateRequest request) {
        try {
            Customer c = findCustomerToToken(token);

            c.setName(request.name());
            c.setLastName(request.lastName());
            c.setPassword(passwordEncoder.encode(request.password()));

            //eMail adresinin kontrolünü sağlayan yapı
            Optional<Customer> testEmailCustomer = repo.findCustomerByEmail(request.email());
            if (testEmailCustomer.isEmpty() || testEmailCustomer.get().getEmail().equals(request.email())) {
                c.setEmail(request.email());
            } else {
                throw new InvalidEMailException();
            }

            //Kredi kartının güncellenmesi için gereken yapı
            Optional<Customer> testCustomer = repo.findCustomerByCreditCardNumber(encryption.encrypt(request.creditCardNumber()));
            if (isValidCreditCardNumber(request.creditCardNumber())) {
                if (testCustomer.isEmpty() || testCustomer.get().getCreditCardNumber().equals(passwordEncoder.encode(request.creditCardNumber()))) {
                    c.setCreditCardNumber(passwordEncoder.encode(request.creditCardNumber()));
                } else {
                    throw new InvalidCreditCardNumberException();
                }
            } else {
                throw new InvalidCreditCardNumberException();
            }

            //Hesap işlemleri için bir bakiye gerekiyor bu yüzden göstermelik bir değer
            c.setBalance(BigDecimal.valueOf(10000));


            repo.save(c);

            kafkaTemplate.send("successful_logs", "The user named " + request.name() + " has been successfully updated in the database");

            CustomerResponse response = new CustomerResponse(c.getName(), c.getLastName(), c.getEmail());
            return new GenericResponse<>(response, true);


        } catch (CustomerNotFoundException e) {
            kafkaTemplate.send("error_logs", "CustomerNotFound: Failed to update the user named " + request.name() + " in the database. Reason: " + e.getMessage());
            throw new CustomerNotFoundException(e);
        } catch (UpdateCustomerRuntimeException e) {
            kafkaTemplate.send("error_logs", "UpdateCustomerRuntime: Failed to update the user named " + request.name() + " in the database. Reason: " + e.getMessage());
            throw new UpdateCustomerRuntimeException(e);
        } catch (InvalidEMailException e) {
            kafkaTemplate.send("error_logs", "InvalidEMail: Failed to update the user named " + request.name() + " in the database. Reason: " + e.getMessage());
            throw new InvalidEMailException(e);
        } catch (InvalidCreditCardNumberException e) {
            kafkaTemplate.send("error_logs", "InvalidCreditCardNumber: Failed to update the user named " + request.name() + " in the database. Reason: " + e.getMessage());
            throw new InvalidCreditCardNumberException(e);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "GeneralError: Failed to update the user named " + request.name() + " in the database. Reason: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /* Bu method admin hesabının belirli bir müşteriyi güncellemesini sağlamaktadır. */
    @Override
    public GenericResponse<CustomerResponse> updateCustomerForAdmin(AdminCustomerUpdateRequest request) {
        try {
            Customer c = repo.findById(request.id()).orElseThrow(CustomerNotFoundException::new);

            c.setName(request.name());
            c.setLastName(request.lastName());
            c.setPassword(passwordEncoder.encode(request.password()));

            //eMail adresinin kontrolünü sağlayan yapı
            Optional<Customer> testEmailCustomer = repo.findCustomerByEmail(request.email());
            if (testEmailCustomer.isEmpty() || testEmailCustomer.get().getEmail().equals(request.email())) {
                c.setEmail(request.email());
            } else {
                throw new InvalidEMailException();
            }

            //Kredi kartının güncellenmesi için gereken yapı
            Optional<Customer> testCustomer = repo.findCustomerByCreditCardNumber(encryption.encrypt(request.creditCardNumber()));
            if (isValidCreditCardNumber(request.creditCardNumber())) {
                if (testCustomer.isEmpty() || testCustomer.get().getCreditCardNumber().equals(passwordEncoder.encode(request.creditCardNumber()))) {
                    c.setCreditCardNumber(passwordEncoder.encode(request.creditCardNumber()));
                } else {
                    throw new InvalidCreditCardNumberException();
                }
            } else {
                throw new InvalidCreditCardNumberException();
            }

            //Hesap işlemleri için bir bakiye gerekiyor bu yüzden göstermelik bir değer
            c.setBalance(BigDecimal.valueOf(10000));

            repo.save(c);

            kafkaTemplate.send("successful_logs", "The user named " + request.name() + " has been successfully updated in the database");

            CustomerResponse response = new CustomerResponse(c.getName(), c.getLastName(), c.getEmail());
            return new GenericResponse<>(response, true);

        } catch (CustomerNotFoundException e) {
            kafkaTemplate.send("error_logs", "CustomerNotFound: Failed to update the user named " + request.name() + " in the database. Reason: " + e.getMessage());
            throw new CustomerNotFoundException(e);
        } catch (InvalidEMailException e) {
            kafkaTemplate.send("error_logs", "InvalidEMail: Failed to update the user named " + request.name() + " in the database. Reason: " + e.getMessage());
            throw new InvalidEMailException(e);
        } catch (InvalidCreditCardNumberException e) {
            kafkaTemplate.send("error_logs", "InvalidCreditCardNumber: Failed to update the user named " + request.name() + " in the database. Reason: " + e.getMessage());
            throw new InvalidCreditCardNumberException(e);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "GeneralError: Failed to update the user named " + request.name() + " in the database. Reason: " + e.getMessage());
            throw new UpdateCustomerRuntimeException(e);
        }
    }

    /* Bu method admin hesabının belirli bir kullanıcının bilgilerini görüntülemesini sağlar. */
    @Override
    public GenericResponse<CustomerResponse> readCustomerForAdmin(Long id) {
        try {
            Customer c = repo.findById(id).orElseThrow(CustomerNotFoundException::new);
            CustomerResponse response = new CustomerResponse(c.getName(), c.getLastName(), c.getEmail());
            kafkaTemplate.send("successful_logs", "The user named " + c.getName() + " has been successfully read in the database");
            return new GenericResponse<>(response, true);
        } catch (CustomerNotFoundException e) {
            kafkaTemplate.send("error_logs", "Failed to read in the database. Reason: " + e.getMessage());
            throw new CustomerNotFoundException(e);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "Failed to read in the database. Reason: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /* Bu method müşterinin kendi müşteri hesabını görüntülemesini sağlar. */
    @Override
    public GenericResponse<CustomerResponse> readCustomerForCustomers(String token) {
        try {
            Customer c = findCustomerToToken(token);

            CustomerResponse response = new CustomerResponse(c.getName(), c.getLastName(), c.getEmail());
            kafkaTemplate.send("successful_logs", "The user named " + c.getName() + " has been successfully read in the database");
            return new GenericResponse<>(response, true);
        } catch (CustomerNotFoundException e) {
            kafkaTemplate.send("error_logs", "Failed to read in the database. Reason: " + e.getMessage());
            throw new CustomerNotFoundException(e);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "Failed to read in the database. Reason: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /* Bu method admin hesabının tüm müşterileri görüntülemesini sağlıyor. */
    @Override
    public GenericResponse<List<CustomerResponse>> readAllForAdmin() {
        try {
            List<CustomerResponse> responseList = repo.findAll().stream().map(customer -> new CustomerResponse(customer.getName(), customer.getLastName(), customer.getEmail())).toList();
            kafkaTemplate.send("successful_logs", "All data has been successfully read from the database");
            return new GenericResponse<>(responseList, true);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "An error was encountered while reading all data from the database. The reason for the error is: " + e.getMessage());
            throw new RuntimeException();
        }
    }

    /* Bu method admin hesabının belirli bir kullanıcıyı silmesini sağlıyor. */
    @Override
    public GenericResponse<String> deleteCustomerForAdmin(Long id) {
        try {
            Customer c = repo.findById(id).orElseThrow(CustomerNotFoundException::new);
            repo.delete(c);
            kafkaTemplate.send("successful_logs", "The user named " + c.getName() + " has been successfully deleted in the database");
            return new GenericResponse<>("Customer deleted successfully.", true);
        } catch (CustomerNotFoundException e) {
            kafkaTemplate.send("error_logs", "Failed to delete in the database. Reason: " + e.getMessage());
            throw new CustomerNotFoundException(e);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "Failed to delete in the database. Reason: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /* Bu method müşterinin kendi müşteri hesabını silmesini sağlar. */
    @Override
    public GenericResponse<String> deleteCustomerForCustomers(String token) {
        try {
            Customer c = findCustomerToToken(token);
            repo.delete(c);
            kafkaTemplate.send("successful_logs", "The user named " + c.getName() + " has been successfully deleted in the database");
            return new GenericResponse<>("Customer deleted successfully.", true);
        } catch (CustomerNotFoundException e) {
            kafkaTemplate.send("error_logs", "Failed to delete in the database. Reason: " + e.getMessage());
            throw new CustomerNotFoundException(e);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "Failed to delete in the database. Reason: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /* Bu method admin hesabının tüm müşterileri silmesini sağlıyor. */
    @Override
    public GenericResponse<String> deleteAllForAdmin() {
        try {
            repo.deleteAll();
            kafkaTemplate.send("successful_logs", "All data has been successfully deleted from the database");
            return new GenericResponse<>("All customers deleted successfully.", true);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "An error was encountered while deleting all data from the database. The reason for the error is: " + e.getMessage());
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
            kafkaTemplate.send("successful_logs", "The user named " + customer.getName() + " has been successfully logged in to the database");
            return new GenericResponse<>(response, true);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "An error occurred while logging in to the database. The reason for the error is: " + e.getMessage());
            throw new RuntimeException();
        }
    }

    /* Bu method kullanıcının hesap oluşturmasını sağlıyor. */
    @Override
    public GenericResponse<AuthResponse> customerRegister(CustomerCreateRequest request) {
        try {
            //Email adresiyle kayıtlı bir hesap var mı ?
            boolean checkCustomerAlreadyExist = repo.findCustomerByEmail(request.email()).isPresent();
            //Verilen kredi kartı numarası geçerli bir numara mı?
//            Boolean checkCreditCardIsValidFormat = isValidCreditCardNumber(request.creditCardNumber());
            //Verilen kredi kartı numarası başka bir hesaba kayıtlı mı?
//            Boolean checkCreditCardAlreadyExist = repo.findCustomerByCreditCardNumber(passwordEncoder.encode(request.creditCardNumber())).isPresent();


            if (!checkCustomerAlreadyExist) {
//                if (checkCreditCardIsValidFormat) {
//                    if (!checkCreditCardAlreadyExist) {

                Customer customer = Customer.builder()
                        .name(request.name())
                        .lastName(request.lastName())
                        .password(passwordEncoder.encode(request.password()))
                        .email(request.email())
                        .role(Role.ROLE_USER)
                        .sentTransfers(new ArrayList<>())
                        .receivedTransfers(new ArrayList<>())
                        .build();
                repo.save(customer);


                var jwtToken = jwtService.generateToken(customer);

                AuthResponse response = new AuthResponse("Register successful", jwtToken
                );
                kafkaTemplate.send("successful_logs", "The user named " + request.name() + " has been successfully registered in the database");

                return new GenericResponse<>(response, true);
            } else {
                throw new CreditCardAlreadyExist();
            }
//                } else {
//                    throw new InvalidCreditCardNumberException();
//                }
//            } else {
//                throw new CustomerAlreadyExistsException();
//            }

        } catch (CustomerAlreadyExistsException e) {
            kafkaTemplate.send("error_logs", "CustomerAlreadyExists: " + e.getMessage());
            throw new CustomerAlreadyExistsException(e);
        } catch (InvalidCreditCardNumberException e) {
            kafkaTemplate.send("error_logs", "InvalidCreditCardNumber: " + e.getMessage());
            throw new InvalidCreditCardNumberException(e);
        } catch (CreditCardAlreadyExist e) {
            kafkaTemplate.send("error_logs", "CreditCardAlreadyExist: " + e.getMessage());
            throw new CreditCardAlreadyExist(e);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "GeneralError: " + e.getMessage());
            throw new RuntimeException("An unknown error occurred: ", e.getCause());
        }

    }

    /* Kullanıcının ödeme yöntemi eklemesini sağlayan method */
    @Override
    public GenericResponse<String> addPaymentMethod(String token, PaymentMethodRequest request) {
        try {
            Customer c = findCustomerToToken(token);
            if (c.getCreditCardNumber() != null) {
                throw new CreditCardAlreadyExist();
            } else {
                if (repo.findCustomerByCreditCardNumber(encryption.encrypt(request.creditCardNumber())).isEmpty()) {
                    c.setCreditCardNumber(encryption.encrypt(request.creditCardNumber()));
                } else {
                    throw new CreditCardNumberAlreadyExist();
                }

                c.setBalance(BigDecimal.valueOf(10000));
            }
            repo.save(c);
            kafkaTemplate.send("successful_logs", "The user named " + c.getName() + " has been successfully added a payment method in the database");
            return new GenericResponse<>("Payment method added successfully", true);
        } catch (CreditCardAlreadyExist e) {
            kafkaTemplate.send("error_logs", "CreditCardAlreadyExist: " + e.getMessage());
            throw new CreditCardAlreadyExist(e);
        } catch (CreditCardNumberAlreadyExist e) {
            kafkaTemplate.send("error_logs", "CreditCardNumberAlreadyExist: " + e.getMessage());
            throw new CreditCardNumberAlreadyExist(e);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "GeneralError: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

    }

    /* Tüm veriye göre verilen ay ofsetinin değerine göre istatislik çıkaran fonksiyon */
    @Override
    public GenericResponse<MonthlyStatisticsResponse> monthlyStatisticsForCustomer(String token, int monthOffset) {
        try {
            Customer customer = findCustomerToToken(token);

            LocalDateTime referenceDate = LocalDateTime.now().minusMonths(monthOffset);

            BigDecimal averageAmountReceived = BigDecimal.valueOf(0);
            BigDecimal highestAmountReceived = BigDecimal.valueOf(0);
            BigDecimal lowestAmountReceived = BigDecimal.valueOf(0);
            int receivedCount = 0;
            BigDecimal monthlyReceivedAmount = BigDecimal.valueOf(0);

            BigDecimal averageAmountSent = BigDecimal.valueOf(0);
            BigDecimal highestAmountSent = BigDecimal.valueOf(0);
            BigDecimal lowestAmountSent = BigDecimal.valueOf(0);
            int sentCount = 0;
            BigDecimal monthlySentAmount = BigDecimal.valueOf(0);

            List<Transfer> receivedTransfers = customer.getReceivedTransfers().stream()
                    .filter(transfer -> transfer.getTimestamp().isAfter(referenceDate))
                    .toList();

            List<Transfer> sentTransfers = customer.getSentTransfers().stream()
                    .filter(transfer -> transfer.getTimestamp().isAfter(referenceDate))
                    .toList();

            if (!receivedTransfers.isEmpty()) {
                List<BigDecimal> receivedTransferAmounts = receivedTransfers.stream()
                        .map(Transfer::getAmount)
                        .toList();

                for (BigDecimal count : receivedTransferAmounts) {
                    monthlyReceivedAmount = monthlyReceivedAmount.add(count);
                }
                averageAmountReceived = monthlyReceivedAmount.divide(BigDecimal.valueOf(receivedTransferAmounts.size()), 2, RoundingMode.HALF_UP);
                highestAmountReceived = Collections.max(receivedTransferAmounts);
                lowestAmountReceived = Collections.min(receivedTransferAmounts);
                receivedCount = receivedTransferAmounts.size();
            }
            if (!sentTransfers.isEmpty()) {
                List<BigDecimal> sentTransferAmounts = sentTransfers.stream()
                        .map(Transfer::getAmount)
                        .toList();

                for (BigDecimal count : sentTransferAmounts) {
                    monthlySentAmount = monthlySentAmount.add(count);
                }
                averageAmountSent = monthlySentAmount.divide(BigDecimal.valueOf(sentTransferAmounts.size()), 2, RoundingMode.HALF_UP);
                highestAmountSent = Collections.max(sentTransferAmounts);
                lowestAmountSent = Collections.min(sentTransferAmounts);
                sentCount = sentTransferAmounts.size();
            }

            MonthlyStatisticsResponse response = new MonthlyStatisticsResponse(
                    monthlyReceivedAmount,
                    monthlySentAmount,
                    receivedCount,
                    sentCount,
                    highestAmountSent,
                    lowestAmountSent,
                    highestAmountReceived,
                    lowestAmountReceived,
                    averageAmountSent,
                    averageAmountReceived,
                    monthOffset
            );
            kafkaTemplate.send("successful_logs", "The user named " + customer.getName() + " has been successfully read monthly statistics in the database");
            return new GenericResponse<>(response, true);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "GeneralError: " + e.getMessage());
            throw new RuntimeException();
        }
    }

    /* Admin hesabının istatistik verisi çıkarması  */
    @Override
    public GenericResponse<MonthlyStatisticsResponse> monthlyStatisticsForAdmin(Long id, int monthOffset) {
        try {
            Customer customer = repo.findById(id).orElseThrow(CustomerNotFoundException::new);

            LocalDateTime referenceDate = LocalDateTime.now().minusMonths(monthOffset);

            BigDecimal averageAmountReceived = BigDecimal.valueOf(0);
            BigDecimal highestAmountReceived = BigDecimal.valueOf(0);
            BigDecimal lowestAmountReceived = BigDecimal.valueOf(0);
            int receivedCount = 0;
            BigDecimal monthlyReceivedAmount = BigDecimal.valueOf(0);

            BigDecimal averageAmountSent = BigDecimal.valueOf(0);
            BigDecimal highestAmountSent = BigDecimal.valueOf(0);
            BigDecimal lowestAmountSent = BigDecimal.valueOf(0);
            int sentCount = 0;
            BigDecimal monthlySentAmount = BigDecimal.valueOf(0);

            List<Transfer> receivedTransfers = customer.getReceivedTransfers().stream()
                    .filter(transfer -> transfer.getTimestamp().isAfter(referenceDate))
                    .toList();

            List<Transfer> sentTransfers = customer.getSentTransfers().stream()
                    .filter(transfer -> transfer.getTimestamp().isAfter(referenceDate))
                    .toList();

            if (!receivedTransfers.isEmpty()) {
                List<BigDecimal> receivedTransferAmounts = receivedTransfers.stream()
                        .map(Transfer::getAmount)
                        .toList();

                for (BigDecimal count : receivedTransferAmounts) {
                    monthlyReceivedAmount = monthlyReceivedAmount.add(count);
                }
                averageAmountReceived = monthlyReceivedAmount.divide(BigDecimal.valueOf(receivedTransferAmounts.size()), 2, RoundingMode.HALF_UP);
                highestAmountReceived = Collections.max(receivedTransferAmounts);
                lowestAmountReceived = Collections.min(receivedTransferAmounts);
                receivedCount = receivedTransferAmounts.size();
            }
            if (!sentTransfers.isEmpty()) {
                List<BigDecimal> sentTransferAmounts = sentTransfers.stream()
                        .map(Transfer::getAmount)
                        .toList();

                for (BigDecimal count : sentTransferAmounts) {
                    monthlySentAmount = monthlySentAmount.add(count);
                }
                averageAmountSent = monthlySentAmount.divide(BigDecimal.valueOf(sentTransferAmounts.size()), 2, RoundingMode.HALF_UP);
                highestAmountSent = Collections.max(sentTransferAmounts);
                lowestAmountSent = Collections.min(sentTransferAmounts);
                sentCount = sentTransferAmounts.size();
            }

            MonthlyStatisticsResponse response = new MonthlyStatisticsResponse(
                    monthlyReceivedAmount,
                    monthlySentAmount,
                    receivedCount,
                    sentCount,
                    highestAmountSent,
                    lowestAmountSent,
                    highestAmountReceived,
                    lowestAmountReceived,
                    averageAmountSent,
                    averageAmountReceived,
                    monthOffset
            );
            kafkaTemplate.send("successful_logs", "The user named " + customer.getName() + " has been successfully read monthly statistics in the database");
            return new GenericResponse<>(response, true);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "GeneralError: " + e.getMessage());
            throw new RuntimeException();
        }
    }




}
