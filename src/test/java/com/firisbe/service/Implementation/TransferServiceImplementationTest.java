package com.firisbe.service.Implementation;

import com.firisbe.aspect.GenericResponse;
import com.firisbe.error.CustomerNotFoundException;
import com.firisbe.error.PaymentFailedException;
import com.firisbe.error.TransferNotFoundException;
import com.firisbe.model.Customer;
import com.firisbe.model.DTO.request.CustomerPaymentRequest;
import com.firisbe.model.DTO.response.PaymentResponse;
import com.firisbe.model.Transfer;
import com.firisbe.repository.jpa.TransferRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TransferServiceImplementationTest {
    private TransferServiceImplementation transferService;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private CustomerServiceImplementation customerService;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transferService = new TransferServiceImplementation(transferRepository, customerService, kafkaTemplate);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void testSendPaymentMessageToKafka_Success() {
        // Arrange
        Customer senderAccount = new Customer();
        senderAccount.setCreditCardNumber("1234567890123456");

        Customer receiveAccount = new Customer();
        receiveAccount.setCreditCardNumber("1234567890123456");

        CustomerPaymentRequest request = new CustomerPaymentRequest("receiver@example.com", BigDecimal.valueOf(100.0));

        when(customerService.findCustomerToToken(anyString())).thenReturn(senderAccount);
        when(customerService.findCustomerByMail(anyString())).thenReturn(receiveAccount);

        // Act
        GenericResponse<String> response = transferService.sendPaymentMessageToKafka(request, "token");

        // Assert
        assertTrue(response.getStatus());
        assertEquals("Payment request received successfully!", response.getData());
        assertNotNull(response.getResponseDate());
        verify(kafkaTemplate, times(1)).send(eq("payment_process"), anyString());
        verify(kafkaTemplate, times(1)).send(eq("payment_log"), eq("Payment request received successfully!"));
    }

    @Test
    public void testSendPaymentMessageToKafka_Failure() {
        // Arrange
        when(customerService.findCustomerToToken(anyString())).thenReturn(null);

        // Act & Assert
        assertThrows(PaymentFailedException.class, () -> {
            CustomerPaymentRequest request = new CustomerPaymentRequest("receiver@example.com", BigDecimal.valueOf(100.0));
            transferService.sendPaymentMessageToKafka(request, "token");
        });
    }

    @Test
    void processPaymentMessageFromKafka_ValidRequest_SuccessfulTransaction() {
        // Arrange
        String validRequest = "1/2/100"; // Example valid request
        Customer senderCustomer = new Customer();
        senderCustomer.setBalance(BigDecimal.valueOf(200)); // Enough balance for the transaction
        Customer receiverCustomer = new Customer();
        BigDecimal initialReceiverBalance = BigDecimal.valueOf(300);
        receiverCustomer.setBalance(initialReceiverBalance);
        when(customerService.findById(1L)).thenReturn(senderCustomer);
        when(customerService.findById(2L)).thenReturn(receiverCustomer);

        // Act
        transferService.processPaymentMessageFromKafka(validRequest);

        // Assert
        assertEquals(BigDecimal.valueOf(100), senderCustomer.getBalance());
        assertEquals(initialReceiverBalance.add(BigDecimal.valueOf(100)), receiverCustomer.getBalance());
        verify(transferRepository, times(1)).save(any(Transfer.class));
        verify(kafkaTemplate, times(1)).send(eq("payment_log"), eq("Payment processed successfully!"));
    }

    @Test
    void processPaymentMessageFromKafka_InvalidRequest_PaymentFailedExceptionThrown() {
        // Arrange
        String invalidRequest = "1/2/100"; // Example invalid request
        Customer senderCustomer = new Customer();
        senderCustomer.setBalance(BigDecimal.valueOf(50)); // Insufficient balance for the transaction
        when(customerService.findById(1L)).thenReturn(senderCustomer);

        // Act & Assert
        assertThrows(PaymentFailedException.class, () -> transferService.processPaymentMessageFromKafka(invalidRequest));
        // Ensure no balance update or transfer save occurs
        verify(customerService, never()).saveCustomer(any(Customer.class));
        verify(transferRepository, never()).save(any(Transfer.class));
        verify(kafkaTemplate, never()).send(eq("payment_log"), eq("Payment processed successfully!"));
    }

    @Test
    void processPaymentMessageFromKafka_Exception_RuntimeExceptionThrown() {
        // Arrange
        String request = "1/2/100"; // Example request
        when(customerService.findById(1L)).thenThrow(new RuntimeException("Simulated exception"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> transferService.processPaymentMessageFromKafka(request));
        // Ensure no balance update or transfer save occurs
        verify(customerService, never()).saveCustomer(any(Customer.class));
        verify(transferRepository, never()).save(any(Transfer.class));
        verify(kafkaTemplate, never()).send(eq("payment_log"), eq("Payment processed successfully!"));
        verify(kafkaTemplate, times(1)).send(eq("error_logs"), startsWith("RuntimeException:"));
    }

    @Test
    void readPaymentForCustomer_ValidTokenAndTransferId_ReturnsValidResponse() {
        // Arrange
        Long transferId = 1L;
        String token = "validToken";
        Transfer transfer = new Transfer();
        transfer.setId(transferId);
        Customer customer = new Customer();
        customer.setId(1L); // Assuming the customer ID is set
        transfer.setSender(customer);
        when(transferRepository.findById(transferId)).thenReturn(Optional.of(transfer));
        when(customerService.findCustomerToToken(token)).thenReturn(customer);

        // Act
        GenericResponse<PaymentResponse> response = transferService.readPaymentForCustomer(token, transferId);

        // Assert
        assertTrue(response.getStatus());
        assertNotNull(response.getData());
        verify(kafkaTemplate, times(1)).send(eq("payment_log"), eq("Transfer read successfully"));
    }

    @Test
    void readPaymentForCustomer_InvalidToken_ThrowsCustomerNotFoundException() {
        // Arrange
        Long transferId = 1L;
        String token = "invalidToken";
        Transfer transfer = new Transfer();
        transfer.setId(transferId);
        Customer customer = new Customer();
        customer.setId(1L); // Assuming the customer ID is set
        transfer.setSender(customer);
        when(transferRepository.findById(transferId)).thenReturn(Optional.of(transfer));
        when(customerService.findCustomerToToken(token)).thenReturn(null);

        // Act & Assert
        assertThrows(CustomerNotFoundException.class, () -> {
            transferService.readPaymentForCustomer(token, transferId);
        });
        verify(kafkaTemplate, times(1)).send(eq("error_logs"), contains("CustomerNotFoundException"));
    }

    @Test
    void readPaymentForCustomer_TransferNotFound_ThrowsTransferNotFoundException() {
        // Arrange
        Long transferId = 1L;
        String token = "validToken";
        when(transferRepository.findById(transferId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TransferNotFoundException.class, () -> {
            transferService.readPaymentForCustomer(token, transferId);
        });
        verify(kafkaTemplate, times(1)).send(eq("error_logs"), contains("TransferNotFoundException"));
    }

    @Test
    public void testListTransfer_sent() {
        // Arrange
        String token = "validToken";
        String value = "sent";
        Customer customer = new Customer();
        List<Transfer> sentTransfers = new ArrayList<>();
        Customer receiver = Customer.builder()
                .id(1L)
                .build();
        Customer sender = Customer.builder()
                .id(2L)
                .build();
        Transfer transfer = Transfer.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(BigDecimal.valueOf(100))
                .timestamp(LocalDateTime.now())
                .build();
        sentTransfers.add(transfer);
        customer.setSentTransfers(sentTransfers);
        when(customerService.findCustomerToToken(token)).thenReturn(customer);

        // Act
        List<PaymentResponse> response = transferService.listTransfer(token, value);

        // Assert
        assertEquals(1, response.size());
        assertEquals(sender, response.getFirst().sentCustomer());
    }

    @Test
    public void testListTransfer_noTransfers() {
        // Arrange
        String token = "validToken";
        String value = "sent";
        Customer customer = new Customer();
        List<Transfer> sentTransfers = new ArrayList<>();
        customer.setSentTransfers(sentTransfers);
        when(customerService.findCustomerToToken(token)).thenReturn(customer);

        // Act & Assert
        assertThrows(TransferNotFoundException.class, () -> transferService.listTransfer(token, value));
        verify(kafkaTemplate).send(eq("error_logs"), eq("TransferNotFoundException: No transfer found for customer"));
    }

    @Test
    public void testReadAllReceivedPaymentForCustomer() {
        // Arrange
        String token = "validToken";
        Customer customer = new Customer();
        List<Transfer> receivedTransfers = new ArrayList<>();
        Customer receiver = Customer.builder()
                .id(1L)
                .build();
        Customer sender = Customer.builder()
                .id(2L)
                .build();
        Transfer transfer = Transfer.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(BigDecimal.valueOf(100))
                .timestamp(LocalDateTime.now())
                .build();
        receivedTransfers.add(transfer);
        customer.setReceivedTransfers(receivedTransfers);
        when(customerService.findCustomerToToken(token)).thenReturn(customer);

        // Act
        GenericResponse<List<PaymentResponse>> response = transferService.readAllReceivedPaymentForCustomer(token);

        // Assert
        assertTrue(response.getStatus());
        assertEquals(1, response.getData().size());
        assertEquals(receiver, response.getData().getFirst().receiverCustomer());
    }

    @Test
    public void testReadAllSentPaymentForCustomer() {
        // Arrange
        String token = "validToken";
        Customer customer = new Customer();
        List<Transfer> receivedTransfers = new ArrayList<>();
        Customer receiver = Customer.builder()
                .id(1L)
                .build();
        Customer sender = Customer.builder()
                .id(2L)
                .build();
        Transfer transfer = Transfer.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(BigDecimal.valueOf(100))
                .timestamp(LocalDateTime.now())
                .build();
        receivedTransfers.add(transfer);
        customer.setSentTransfers(receivedTransfers);
        when(customerService.findCustomerToToken(token)).thenReturn(customer);

        // Act
        GenericResponse<List<PaymentResponse>> response = transferService.readAllSentPaymentForCustomer(token);

        // Assert
        assertTrue(response.getStatus());
        assertEquals(1, response.getData().size());
        assertEquals(sender, response.getData().getFirst().sentCustomer());
    }

    @Test
    public void testReadAllPaymentForCustomer() {
        // Arrange
        String token = "validToken";
        Customer customer = new Customer();
        List<Transfer> receivedTransfers = new ArrayList<>();
        Customer receiver = Customer.builder()
                .id(1L)
                .build();
        Customer sender = Customer.builder()
                .id(2L)
                .build();
        Transfer transfer = Transfer.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(BigDecimal.valueOf(100))
                .timestamp(LocalDateTime.now())
                .build();
        receivedTransfers.add(transfer);
        customer.setSentTransfers(receivedTransfers);
        when(customerService.findCustomerToToken(token)).thenReturn(customer);

        // Act
        GenericResponse<List<PaymentResponse>> response = transferService.readAllPaymentForCustomer(token);

        // Assert
        assertTrue(response.getStatus());
        assertEquals(1, response.getData().size());
        assertEquals(sender, response.getData().getFirst().sentCustomer());
        assertEquals(receiver, response.getData().getFirst().receiverCustomer());
    }

    @Test
    void testListTransfer_WhenNoTransfersExist() {
        // Mock customer
        Customer customer = new Customer();
        List<Transfer> sentTransfers = new ArrayList<>();
        List<Transfer> receivedTransfers = new ArrayList<>();
        customer.setSentTransfers(sentTransfers);
        customer.setReceivedTransfers(receivedTransfers);

        // Mock customerService
        when(customerService.findCustomerToToken(anyString())).thenReturn(customer);

        // Test listTransfer for "all"
        assertThrows(TransferNotFoundException.class, () -> transferService.listTransfer("token", "all"));

        // Verify Kafka template
        verify(kafkaTemplate).send(eq("error_logs"), eq("TransferNotFoundException: No transfer found for customer"));
    }

    @Test
    void testReadAllReceivedPaymentForCustomerX() {
        // Mock customer
        Customer receiver = Customer.builder()
                .id(1L)
                .build();
        Customer sender = Customer.builder()
                .id(2L)
                .build();
        Transfer transfer1 = Transfer.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(BigDecimal.valueOf(100))
                .timestamp(LocalDateTime.now())
                .build();
        Transfer transfer2 = Transfer.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(BigDecimal.valueOf(100))
                .timestamp(LocalDateTime.now())
                .build();
        Transfer transfer3 = Transfer.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(BigDecimal.valueOf(100))
                .timestamp(LocalDateTime.now())
                .build();

        List<Transfer> receivedTransfers = new ArrayList<>();
        receivedTransfers.add(transfer1);
        receivedTransfers.add(transfer2);
        receivedTransfers.add(transfer3);

        receiver.setReceivedTransfers(receivedTransfers);

        // Mock customerService
        when(customerService.findCustomerToToken(anyString())).thenReturn(receiver);

        // Test readAllReceivedPaymentForCustomer for last 2 months
        int monthOffset = 2;
        LocalDateTime referenceDate = LocalDateTime.now().minusMonths(monthOffset);
        GenericResponse<List<PaymentResponse>> response = transferService.readAllReceivedPaymentForCustomer("token", monthOffset);

        // Verify Kafka template
        verify(kafkaTemplate).send(eq("payment_log"), eq("Transfers listed successfully"));

        // Assertions
        assertNotNull(response);
        assertTrue(response.getStatus());
        List<PaymentResponse> filteredList = response.getData();
        assertNotNull(filteredList);
        assertEquals(3, filteredList.size());

        // Asserting if the payments are within the last 2 months
        for (PaymentResponse paymentResponse : filteredList) {
            assertTrue(paymentResponse.timestamp().isAfter(referenceDate));
        }
    }

    @Test
    void testListTransfer_WhenNoReceivedTransfersExist() {
        // Mock customer
        Customer customer = new Customer();
        List<Transfer> receivedTransfers = new ArrayList<>();
        customer.setReceivedTransfers(receivedTransfers);

        // Mock customerService
        when(customerService.findCustomerToToken(anyString())).thenReturn(customer);

        // Test listTransfer for "received" when no transfers exist
        assertThrows(TransferNotFoundException.class, () -> transferService.listTransfer("token", "received"));

        // Verify Kafka template
        verify(kafkaTemplate).send(eq("error_logs"), eq("TransferNotFoundException: No transfer found for customer"));
    }

    @Test
    void testReadAllSentPaymentForCustomerX() {
        // Mock customer
        Customer sender = Customer.builder()
                .id(1L)
                .build();
        Customer receiver = Customer.builder()
                .id(2L)
                .build();
        Transfer transfer1 = Transfer.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(BigDecimal.valueOf(100))
                .timestamp(LocalDateTime.now())
                .build();
        Transfer transfer2 = Transfer.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(BigDecimal.valueOf(200))
                .timestamp(LocalDateTime.now())
                .build();
        Transfer transfer3 = Transfer.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(BigDecimal.valueOf(300))
                .timestamp(LocalDateTime.now())
                .build();

        List<Transfer> sentTransfers = new ArrayList<>();
        sentTransfers.add(transfer1);
        sentTransfers.add(transfer2);
        sentTransfers.add(transfer3);

        sender.setSentTransfers(sentTransfers);

        // Mock customerService
        when(customerService.findCustomerToToken(anyString())).thenReturn(sender);

        // Test readAllReceivedPaymentForCustomer for last 2 months
        int monthOffset = 2;
        LocalDateTime referenceDate = LocalDateTime.now().minusMonths(monthOffset);
        GenericResponse<List<PaymentResponse>> response = transferService.readAllSentPaymentForCustomer("token", monthOffset);

        // Verify Kafka template
        verify(kafkaTemplate).send(eq("payment_log"), eq("Transfers listed successfully"));

        // Assertions
        assertNotNull(response);
        assertTrue(response.getStatus());
        List<PaymentResponse> filteredList = response.getData();
        assertNotNull(filteredList);
        assertEquals(3, filteredList.size());

        // Asserting if the payments are within the last 2 months
        for (PaymentResponse paymentResponse : filteredList) {
            assertTrue(paymentResponse.timestamp().isAfter(referenceDate));
        }
    }

    @Test
    void testListTransfer_WhenNoSentTransfersExist() {
        // Mock customer
        Customer customer = new Customer();
        List<Transfer> sentTransfer = new ArrayList<>();
        customer.setSentTransfers(sentTransfer);

        // Mock customerService
        when(customerService.findCustomerToToken(anyString())).thenReturn(customer);

        // Test listTransfer for "received" when no transfers exist
        assertThrows(TransferNotFoundException.class, () -> transferService.listTransfer("token", "sent"));

        // Verify Kafka template
        verify(kafkaTemplate).send(eq("error_logs"), eq("TransferNotFoundException: No transfer found for customer"));
    }

    @Test
    void testReadAllPaymentForCustomerX() {
        // Mock customer
        Customer sender = Customer.builder()
                .id(1L)
                .build();
        Customer receiver = Customer.builder()
                .id(2L)
                .build();
        Transfer transfer1 = Transfer.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(BigDecimal.valueOf(100))
                .timestamp(LocalDateTime.now())
                .build();
        Transfer transfer2 = Transfer.builder()
                .sender(receiver)
                .receiver(sender)
                .amount(BigDecimal.valueOf(200))
                .timestamp(LocalDateTime.now())
                .build();


        List<Transfer> sentTransfers = new ArrayList<>();
        sentTransfers.add(transfer1);
        List<Transfer> receivedTransfers = new ArrayList<>();
        receivedTransfers.add(transfer2);


        sender.setSentTransfers(sentTransfers);
        sender.setReceivedTransfers(receivedTransfers);

        // Mock customerService
        when(customerService.findCustomerToToken(anyString())).thenReturn(sender);


        // Test readAllReceivedPaymentForCustomer for last 2 months
        int monthOffset = 2;
        LocalDateTime referenceDate = LocalDateTime.now().minusMonths(monthOffset);
        GenericResponse<List<PaymentResponse>> response = transferService.readAllPaymentForCustomer("token", monthOffset);

        // Verify Kafka template
        verify(kafkaTemplate).send(eq("payment_log"), eq("Transfers listed successfully"));

        // Assertions
        assertNotNull(response);
        assertTrue(response.getStatus());
        List<PaymentResponse> filteredList = response.getData();
        assertNotNull(filteredList);
        assertEquals(2, filteredList.size());

        // Asserting if the payments are within the last 2 months
        for (PaymentResponse paymentResponse : filteredList) {
            assertTrue(paymentResponse.timestamp().isAfter(referenceDate));
        }
    }

    @Test
    void testReadPaymentForAdmin() {
        // Arrange
        Long transferId = 1L;
        Transfer transfer = new Transfer();
        transfer.setId(transferId);
        when(transferRepository.findById(transferId)).thenReturn(Optional.of(transfer));

        // Act
        GenericResponse<PaymentResponse> response = transferService.readPaymentForAdmin(transferId);

        // Assert
        assertTrue(response.getStatus());
        assertNotNull(response.getData());
        verify(kafkaTemplate, times(1)).send(eq("payment_log"), eq("Transfer read successfully"));
    }

    @Test
    void testReadPaymentForAdmin_WhenTransferNotFound() {
        // Arrange
        Long transferId = 1L;
        when(transferRepository.findById(transferId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TransferNotFoundException.class, () -> {
            transferService.readPaymentForAdmin(transferId);
        });
        verify(kafkaTemplate, times(1)).send(eq("error_logs"), contains("TransferNotFoundException"));
    }

    @Test
    void testReadAllPaymentForAdmin() {
        // Arrange
        List<Transfer> transfers = new ArrayList<>();
        Transfer transfer1 = new Transfer();
        Transfer transfer2 = new Transfer();
        Transfer transfer3 = new Transfer();
        transfers.add(transfer1);
        transfers.add(transfer2);
        transfers.add(transfer3);
        when(transferRepository.findAll()).thenReturn(transfers);

        // Act
        GenericResponse<List<PaymentResponse>> response = transferService.readAllPaymentForAdmin();

        // Assert
        assertTrue(response.getStatus());
        assertNotNull(response.getData());
        assertEquals(3, response.getData().size());
        verify(kafkaTemplate, times(1)).send(eq("payment_log"), eq("Transfers listed successfully"));
    }

    @Test
    void testReadAllPaymentForAdmin_WhenNoTransfersExist() {
        // Arrange
        List<Transfer> transfers = Collections.emptyList();
        when(transferRepository.findAll()).thenReturn(transfers);

        // Act & Assert
        TransferNotFoundException exception = assertThrows(TransferNotFoundException.class, () -> {
            transferService.readAllPaymentForAdmin();
        });

        // Verify Kafka template with the correct expected message
        verify(kafkaTemplate).send(eq("error_logs"), contains("TransferNotFoundException: "));

    }

    @Test
    void testReadAllPaymentForAdminWithMonthOffset() {
        // Arrange
        Customer sender = Customer.builder()
                .id(1L)
                .build();
        Customer receiver = Customer.builder()
                .id(2L)
                .build();
        Transfer transfer1 = Transfer.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(BigDecimal.valueOf(100))
                .timestamp(LocalDateTime.now())
                .build();
        Transfer transfer2 = Transfer.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(BigDecimal.valueOf(200))
                .timestamp(LocalDateTime.now())
                .build();
        Transfer transfer3 = Transfer.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(BigDecimal.valueOf(300))
                .timestamp(LocalDateTime.now())
                .build();
        List<Transfer> transfers = new ArrayList<>();
        transfers.add(transfer1);
        transfers.add(transfer2);
        transfers.add(transfer3);

        when(transferRepository.findAll()).thenReturn(transfers);
        int monthOffset = 1; // Testing for transfers within the last month

        // Act
        GenericResponse<List<PaymentResponse>> response = transferService.readAllPaymentForAdmin(monthOffset);

        // Assert
        assertTrue(response.getStatus());
        assertEquals(3, response.getData().size()); // Only one transfer should match the criteria
        assertEquals(sender, response.getData().getFirst().sentCustomer()); // Ensure the correct transfer is in the response

        verify(kafkaTemplate).send(eq("payment_log"), eq("Transfers listed successfully"));
    }


    @Test
    void testReadAllPaymentForAdmin_WhenNoTransfersExistX() {
        List<Transfer> transfers = Collections.emptyList();
        when(transferRepository.findAll()).thenReturn(transfers);

        // Act & Assert
        TransferNotFoundException exception = assertThrows(TransferNotFoundException.class, () -> {
            transferService.readAllPaymentForAdmin(1);
        });
        // Additional assertions can be made here if needed
        assertNotNull(exception);

        // Verify Kafka template with the correct expected message
        verify(kafkaTemplate).send(eq("error_logs"), contains("TransferNotFoundException: "));

    }


}