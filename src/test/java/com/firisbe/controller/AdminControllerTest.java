package com.firisbe.controller;

import com.firisbe.aspect.GenericResponse;
import com.firisbe.model.Customer;
import com.firisbe.model.DTO.request.AdminCustomerUpdateRequest;
import com.firisbe.model.DTO.response.CustomerResponse;
import com.firisbe.model.DTO.response.MonthlyStatisticsResponse;
import com.firisbe.model.DTO.response.PaymentResponse;
import com.firisbe.service.Implementation.CustomerServiceImplementation;
import com.firisbe.service.Implementation.TransferServiceImplementation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class AdminControllerTest {

    private AdminController adminController;
    private CustomerServiceImplementation customerService;
    private TransferServiceImplementation transferService;

    @BeforeEach
    void setUp() {
        customerService = mock(CustomerServiceImplementation.class);
        transferService = mock(TransferServiceImplementation.class);
        adminController = new AdminController(customerService, transferService);
    }

    @Test
    void testUpdateCustomer() {
        // Prepare
        AdminCustomerUpdateRequest request = new AdminCustomerUpdateRequest(
                1L,
                "name",
                "lastName",
                "email",
                "password",
                "creditCardNumber"
        );
        ResponseEntity<GenericResponse<CustomerResponse>> expectedResponseEntity = new ResponseEntity<>(HttpStatus.OK);

        // When
        when(customerService.updateCustomerForAdmin(request)).thenReturn(new GenericResponse<>(new CustomerResponse(
                "name",
                "lastName",
                "email"
        ), true));
        ResponseEntity<GenericResponse<CustomerResponse>> responseEntity = adminController.updateCustomer(request);

        // Verify
        assertEquals(expectedResponseEntity.getStatusCode(), responseEntity.getStatusCode());
        verify(customerService, times(1)).updateCustomerForAdmin(request);
    }

    @Test
    void testReadCustomer() {
        // Prepare
        Long customerId = 1L;
        ResponseEntity<GenericResponse<CustomerResponse>> expectedResponseEntity = new ResponseEntity<>(HttpStatus.OK);

        // When
        when(customerService.readCustomerForAdmin(customerId)).thenReturn(new GenericResponse<>(new CustomerResponse(
                "name",
                "lastName",
                "email"
        ), true));

        ResponseEntity<GenericResponse<CustomerResponse>> responseEntity = adminController.readCustomer(customerId);

        // Verify
        assertEquals(expectedResponseEntity.getStatusCode(), responseEntity.getStatusCode());
        verify(customerService, times(1)).readCustomerForAdmin(customerId);
    }

    @Test
    void testReadAllCustomers() {
        // Prepare
        List<CustomerResponse> customers = new ArrayList<>();
        ResponseEntity<GenericResponse<List<CustomerResponse>>> expectedResponseEntity = new ResponseEntity<>(HttpStatus.OK);

        // When
        when(customerService.readAllForAdmin()).thenReturn(new GenericResponse<>(customers, true));
        ResponseEntity<GenericResponse<List<CustomerResponse>>> responseEntity = adminController.readAllCustomers();

        // Verify
        assertEquals(expectedResponseEntity.getStatusCode(), responseEntity.getStatusCode());
        verify(customerService, times(1)).readAllForAdmin();
    }

    @Test
    void testDeleteCustomer() {
        // Prepare
        Long customerId = 1L;
        ResponseEntity<GenericResponse<String>> expectedResponseEntity = new ResponseEntity<>(HttpStatus.OK);

        // When
        when(customerService.deleteCustomerForAdmin(customerId)).thenReturn(new GenericResponse<>("Customer deleted successfully", true));
        ResponseEntity<GenericResponse<String>> responseEntity = adminController.deleteCustomer(customerId);

        // Verify
        assertEquals(expectedResponseEntity.getStatusCode(), responseEntity.getStatusCode());
        verify(customerService, times(1)).deleteCustomerForAdmin(customerId);
    }

    @Test
    void testDeleteAllCustomers() {
        // Prepare
        ResponseEntity<GenericResponse<String>> expectedResponseEntity = new ResponseEntity<>(HttpStatus.OK);

        // When
        when(customerService.deleteAllForAdmin()).thenReturn(new GenericResponse<>("All customers deleted successfully", true));
        ResponseEntity<GenericResponse<String>> responseEntity = adminController.deleteAllCustomers();

        // Verify
        assertEquals(expectedResponseEntity.getStatusCode(), responseEntity.getStatusCode());
        verify(customerService, times(1)).deleteAllForAdmin();
    }

    @Test
    void testReadPaymentForAdmin() {
        // Prepare
        Long paymentId = 1L;
        ResponseEntity<GenericResponse<PaymentResponse>> expectedResponseEntity = new ResponseEntity<>(HttpStatus.OK);
        Customer sentCustomer = new Customer();
        Customer receiverCustomer = new Customer();
        // When
        when(transferService.readPaymentForAdmin(paymentId)).thenReturn(new GenericResponse<>(new PaymentResponse(
                sentCustomer,
                receiverCustomer,
                BigDecimal.valueOf(100),
                LocalDateTime.now()
        ), true));
        ResponseEntity<GenericResponse<PaymentResponse>> responseEntity = adminController.readPaymentForAdmin(paymentId);

        // Verify
        assertEquals(expectedResponseEntity.getStatusCode(), responseEntity.getStatusCode());
        verify(transferService, times(1)).readPaymentForAdmin(paymentId);
    }

    @Test
    void testReadAllPaymentForAdmin() {
        // Prepare
        ResponseEntity<GenericResponse<List<PaymentResponse>>> expectedResponseEntity = new ResponseEntity<>(HttpStatus.OK);

        // When
        when(transferService.readAllPaymentForAdmin()).thenReturn(new GenericResponse<>(new ArrayList<>(), true));
        ResponseEntity<GenericResponse<List<PaymentResponse>>> responseEntity = adminController.readAllPaymentForAdmin();

        // Verify
        assertEquals(expectedResponseEntity.getStatusCode(), responseEntity.getStatusCode());
        verify(transferService, times(1)).readAllPaymentForAdmin();
    }

    @Test
    void testReadAllPaymentForAdminWithTimeOffset() {
        // Prepare
        int monthOffset = 1;
        ResponseEntity<GenericResponse<List<PaymentResponse>>> expectedResponseEntity = new ResponseEntity<>(HttpStatus.OK);

        // When
        when(transferService.readAllPaymentForAdmin(monthOffset)).thenReturn(new GenericResponse<>(new ArrayList<>(), true));
        ResponseEntity<GenericResponse<List<PaymentResponse>>> responseEntity = adminController.readAllPaymentForAdmin(monthOffset);

        // Verify
        assertEquals(expectedResponseEntity.getStatusCode(), responseEntity.getStatusCode());
        verify(transferService, times(1)).readAllPaymentForAdmin(monthOffset);
    }

    @Test
    void testMonthlyStatistics() {
        // Prepare
        Long customerId = 1L;
        int monthOffset = 1;
        ResponseEntity<GenericResponse<MonthlyStatisticsResponse>> expectedResponseEntity = new ResponseEntity<>(HttpStatus.OK);

        // When
        when(customerService.monthlyStatisticsForAdmin(customerId, monthOffset)).thenReturn(new GenericResponse<>(new MonthlyStatisticsResponse(
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
        ),true));
        ResponseEntity<GenericResponse<MonthlyStatisticsResponse>> responseEntity = adminController.monthlyStatistics(customerId, monthOffset);

        // Verify
        assertEquals(expectedResponseEntity.getStatusCode(), responseEntity.getStatusCode());
        verify(customerService, times(1)).monthlyStatisticsForAdmin(customerId, monthOffset);
    }
}