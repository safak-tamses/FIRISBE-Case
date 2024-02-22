package com.firisbe.controller;

import com.firisbe.aspect.GenericResponse;
import com.firisbe.model.Customer;
import com.firisbe.model.DTO.request.CustomerPaymentRequest;
import com.firisbe.model.DTO.request.CustomerUpdateRequest;
import com.firisbe.model.DTO.request.PaymentMethodRequest;
import com.firisbe.model.DTO.response.CustomerResponse;
import com.firisbe.model.DTO.response.MonthlyStatisticsResponse;
import com.firisbe.model.DTO.response.PaymentResponse;
import com.firisbe.service.Implementation.CustomerServiceImplementation;
import com.firisbe.service.Implementation.TransferServiceImplementation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerControllerTest {
    private CustomerController customerController;
    private CustomerServiceImplementation customerService;
    private TransferServiceImplementation transferService;

    @BeforeEach
    void setUp() {
        customerService = mock(CustomerServiceImplementation.class);
        transferService = mock(TransferServiceImplementation.class);
        customerController = new CustomerController(customerService, transferService);
    }


    @AfterEach
    void tearDown() {
    }

    @Test
    void updateCustomer() {
        // Prepare
        String token = "sample_token";
        CustomerUpdateRequest request = new CustomerUpdateRequest(
                "name",
                "lastName",
                "email",
                "password",
                "creditCardNumber"
        );
        CustomerResponse customerResponse = new CustomerResponse(
                "name",
                "lastName",
                "email"
        );
        ResponseEntity<GenericResponse<CustomerResponse>> expectedResponseEntity =
                new ResponseEntity<>(new GenericResponse<>(customerResponse, true), HttpStatus.OK);

        // Stubbing the service method
        when(customerService.updateCustomerForCustomers(token, request))
                .thenReturn(new GenericResponse<>(customerResponse, true));

        // Execute
        ResponseEntity<GenericResponse<CustomerResponse>> responseEntity =
                customerController.updateCustomer(token, request);

        // Verify
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(customerResponse, responseEntity.getBody().getData());
    }

    @Test
    void readCustomer() {
        // Prepare
        String token = "sample_token";
        CustomerResponse customerResponse = new CustomerResponse(
                "name",
                "lastName",
                "email"
        ); // Dummy response
        ResponseEntity<GenericResponse<CustomerResponse>> expectedResponseEntity =
                new ResponseEntity<>(new GenericResponse<>(customerResponse, true), HttpStatus.OK);

        // Stubbing the service method
        when(customerService.readCustomerForCustomers(token))
                .thenReturn(new GenericResponse<>(customerResponse, true));

        // Execute
        ResponseEntity<GenericResponse<CustomerResponse>> responseEntity =
                customerController.readCustomer(token);

        // Verify
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(customerResponse, responseEntity.getBody().getData());
    }

    @Test
    void deleteCustomer() {
        // Prepare
        String token = "sample_token";
        String message = "Customer deleted successfully";
        ResponseEntity<GenericResponse<String>> expectedResponseEntity =
                new ResponseEntity<>(new GenericResponse<>(message, true), HttpStatus.OK);

        // Stubbing the service method
        when(customerService.deleteCustomerForCustomers(token))
                .thenReturn(new GenericResponse<>(message, true));

        // Execute
        ResponseEntity<GenericResponse<String>> responseEntity =
                customerController.deleteCustomer(token);

        // Verify
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(message, responseEntity.getBody().getData());
    }

    @Test
    void addPaymentMethodForCustomer() {
        // Prepare
        String token = "sample_token";
        PaymentMethodRequest request = new PaymentMethodRequest(
                "creditCardNumber"
        );
        String message = "Payment method added successfully";
        ResponseEntity<GenericResponse<String>> expectedResponseEntity =
                new ResponseEntity<>(new GenericResponse<>(message, true), HttpStatus.OK);

        // Stubbing the service method
        when(customerService.addPaymentMethod(token, request))
                .thenReturn(new GenericResponse<>(message, true));

        // Execute
        ResponseEntity<GenericResponse<String>> responseEntity =
                customerController.addPaymentMethodForCustomer(token, request);

        // Verify
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(message, responseEntity.getBody().getData());
    }

    @Test
    void sendPaymentMessageToKafka() {
        // Prepare
        String token = "sample_token";
        CustomerPaymentRequest request = new CustomerPaymentRequest(
                "receiveMail",
                BigDecimal.valueOf(1000.0)
        );
        String message = "Payment message sent to Kafka successfully";
        ResponseEntity<GenericResponse<String>> expectedResponseEntity =
                new ResponseEntity<>(new GenericResponse<>(message, true), HttpStatus.OK);

        // Stubbing the service method
        when(transferService.sendPaymentMessageToKafka(request, token))
                .thenReturn(new GenericResponse<>(message, true));

        // Execute
        ResponseEntity<GenericResponse<String>> responseEntity =
                customerController.sendPaymentMessageToKafka(request, token);

        // Verify
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(message, responseEntity.getBody().getData());
    }


    @Test
    void readPaymentForCustomer() {
        // Prepare
        String token = "sample_token";
        Long paymentId = 123L;
        Customer sentCustomer = new Customer();
        Customer receiverCustomer = new Customer();
        PaymentResponse paymentResponse = new PaymentResponse(
                sentCustomer,
                receiverCustomer,
                BigDecimal.valueOf(1000.0),
                LocalDateTime.now()
        ); // Dummy response
        ResponseEntity<GenericResponse<PaymentResponse>> expectedResponseEntity =
                new ResponseEntity<>(new GenericResponse<>(paymentResponse,true), HttpStatus.OK);

        // Stubbing the service method
        when(transferService.readPaymentForCustomer(token, paymentId))
                .thenReturn(new GenericResponse<>(paymentResponse,true));

        // Execute
        ResponseEntity<GenericResponse<PaymentResponse>> responseEntity =
                customerController.readPaymentForCustomer(token, paymentId);

        // Verify
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(paymentResponse, responseEntity.getBody().getData());
    }

    @Test
    void readAllReceivedPaymentForCustomer() {
        // Prepare
        String token = "sample_token";
        List<PaymentResponse> paymentResponses = new ArrayList<>(); // Dummy response
        ResponseEntity<GenericResponse<List<PaymentResponse>>> expectedResponseEntity =
                new ResponseEntity<>(new GenericResponse<>(paymentResponses,true), HttpStatus.OK);

        // Stubbing the service method
        when(transferService.readAllReceivedPaymentForCustomer(token))
                .thenReturn(new GenericResponse<>(paymentResponses,true));

        // Execute
        ResponseEntity<GenericResponse<List<PaymentResponse>>> responseEntity =
                customerController.readAllReceivedPaymentForCustomer(token);

        // Verify
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(paymentResponses, responseEntity.getBody().getData());
    }

    @Test
    void readAllSentPaymentForCustomer() {
        // Prepare
        String token = "sample_token";
        List<PaymentResponse> paymentResponses = new ArrayList<>(); // Dummy response
        ResponseEntity<GenericResponse<List<PaymentResponse>>> expectedResponseEntity =
                new ResponseEntity<>(new GenericResponse<>(paymentResponses,true), HttpStatus.OK);

        // Stubbing the service method
        when(transferService.readAllSentPaymentForCustomer(token))
                .thenReturn(new GenericResponse<>(paymentResponses,true));

        // Execute
        ResponseEntity<GenericResponse<List<PaymentResponse>>> responseEntity =
                customerController.readAllSentPaymentForCustomer(token);

        // Verify
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(paymentResponses, responseEntity.getBody().getData());
    }

    @Test
    void readAllPaymentForCustomer() {
        // Prepare
        String token = "sample_token";
        List<PaymentResponse> paymentResponses = new ArrayList<>(); // Dummy response
        ResponseEntity<GenericResponse<List<PaymentResponse>>> expectedResponseEntity =
                new ResponseEntity<>(new GenericResponse<>(paymentResponses,true), HttpStatus.OK);

        // Stubbing the service method
        when(transferService.readAllPaymentForCustomer(token))
                .thenReturn(new GenericResponse<>(paymentResponses,true));

        // Execute
        ResponseEntity<GenericResponse<List<PaymentResponse>>> responseEntity =
                customerController.readAllPaymentForCustomer(token);

        // Verify
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(paymentResponses, responseEntity.getBody().getData());
    }

    @Test
    void readAllReceivedPaymentForCustomerX() {
        // Prepare
        String token = "sample_token";
        int monthOffset = 2;
        List<PaymentResponse> paymentResponses = new ArrayList<>(); // Dummy response
        ResponseEntity<GenericResponse<List<PaymentResponse>>> expectedResponseEntity =
                new ResponseEntity<>(new GenericResponse<>(paymentResponses,true), HttpStatus.OK);

        // Stubbing the service method
        when(transferService.readAllReceivedPaymentForCustomer(token, monthOffset))
                .thenReturn(new GenericResponse<>(paymentResponses,true));

        // Execute
        ResponseEntity<GenericResponse<List<PaymentResponse>>> responseEntity =
                customerController.readAllReceivedPaymentForCustomer(token, monthOffset);

        // Verify
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(paymentResponses, responseEntity.getBody().getData());
    }
    @Test
    void readAllSentPaymentForCustomerX() {
        // Prepare
        String token = "sample_token";
        int monthOffset = 2;
        List<PaymentResponse> paymentResponses = new ArrayList<>(); // Dummy response
        ResponseEntity<GenericResponse<List<PaymentResponse>>> expectedResponseEntity =
                new ResponseEntity<>(new GenericResponse<>(paymentResponses,true), HttpStatus.OK);

        // Stubbing the service method
        when(transferService.readAllSentPaymentForCustomer(token, monthOffset))
                .thenReturn(new GenericResponse<>(paymentResponses,true));

        // Execute
        ResponseEntity<GenericResponse<List<PaymentResponse>>> responseEntity =
                customerController.readAllSentPaymentForCustomer(token, monthOffset);

        // Verify
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(paymentResponses, responseEntity.getBody().getData());
    }



    @Test
    void readAllPaymentForCustomerX() {
        // Prepare
        String token = "sample_token";
        int monthOffset = 2;
        List<PaymentResponse> paymentResponses = new ArrayList<>(); // Dummy response
        ResponseEntity<GenericResponse<List<PaymentResponse>>> expectedResponseEntity =
                new ResponseEntity<>(new GenericResponse<>(paymentResponses,true), HttpStatus.OK);

        // Stubbing the service method
        when(transferService.readAllPaymentForCustomer(token, monthOffset))
                .thenReturn(new GenericResponse<>(paymentResponses,true));

        // Execute
        ResponseEntity<GenericResponse<List<PaymentResponse>>> responseEntity =
                customerController.readAllPaymentForCustomer(token, monthOffset);

        // Verify
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(paymentResponses, responseEntity.getBody().getData());
    }

    @Test
    void monthlyStatisticsForCustomer() {
        // Prepare
        String token = "sample_token";
        int monthOffset = 2;
        MonthlyStatisticsResponse statisticsResponse = new MonthlyStatisticsResponse(
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                0,
                0,
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                0
        ); // Dummy response
        ResponseEntity<GenericResponse<MonthlyStatisticsResponse>> expectedResponseEntity =
                new ResponseEntity<>(new GenericResponse<>(statisticsResponse,true), HttpStatus.OK);

        // Stubbing the service method
        when(customerService.monthlyStatisticsForCustomer(token, monthOffset))
                .thenReturn(new GenericResponse<>(statisticsResponse,true));

        // Execute
        ResponseEntity<GenericResponse<MonthlyStatisticsResponse>> responseEntity =
                customerController.monthlyStatisticsForCustomer(token, monthOffset);

        // Verify
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(statisticsResponse, responseEntity.getBody().getData());
    }
}