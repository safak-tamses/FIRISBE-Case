package com.firisbe.controller;

import com.firisbe.aspect.GenericResponse;
import com.firisbe.model.DTO.request.AdminCustomerUpdateRequest;

import com.firisbe.model.DTO.response.CustomerResponse;
import com.firisbe.model.DTO.response.MonthlyStatisticsResponse;
import com.firisbe.model.DTO.response.PaymentResponse;
import com.firisbe.service.Implementation.CustomerServiceImplementation;
import com.firisbe.service.Implementation.TransferServiceImplementation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/admin")
@SecurityRequirement(name = "bearerAuth")
@AllArgsConstructor
public class AdminController {
    private final CustomerServiceImplementation service;
    private final TransferServiceImplementation transferService;

    @Operation(summary = "Update customer information", description = "Update customer information", tags = {"admin-controller"})
    @PutMapping
    public ResponseEntity<GenericResponse<CustomerResponse>> updateCustomer(@RequestBody AdminCustomerUpdateRequest request) {
        return new ResponseEntity<>(service.updateCustomerForAdmin(request), HttpStatus.OK);
    }

    @Operation(summary = "Read customer information", description = "Read customer information", tags = {"admin-controller"})
    @GetMapping("/{customerId}")
    public ResponseEntity<GenericResponse<CustomerResponse>> readCustomer(@PathVariable(value = "customerId") Long id) {
        return new ResponseEntity<>(service.readCustomerForAdmin(id), HttpStatus.OK);
    }

    @Operation(summary = "Read all customers", description = "Read all customers", tags = {"admin-controller"})
    @GetMapping("/all")
    public ResponseEntity<GenericResponse<List<CustomerResponse>>> readAllCustomers() {
        return new ResponseEntity<>(service.readAllForAdmin(), HttpStatus.OK);
    }

    @Operation(summary = "Delete customer", description = "Delete customer", tags = {"admin-controller"})
    @DeleteMapping("/{customerId}")
    public ResponseEntity<GenericResponse<String>> deleteCustomer(@PathVariable(value = "customerId") Long id) {
        return new ResponseEntity<>(service.deleteCustomerForAdmin(id), HttpStatus.OK);
    }

    @Operation(summary = "Delete all customers", description = "Delete all customers", tags = {"admin-controller"})
    @DeleteMapping("/all")
    public ResponseEntity<GenericResponse<String>> deleteAllCustomers() {
        return new ResponseEntity<>(service.deleteAllForAdmin(), HttpStatus.OK);
    }

    @Operation(summary = "Read payment information", description = "Read payment information", tags = {"admin-controller"})
    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<GenericResponse<PaymentResponse>> readPaymentForAdmin(@PathVariable(value = "paymentId") Long id) {
        return new ResponseEntity<>(transferService.readPaymentForAdmin(id), HttpStatus.OK);
    }

    @Operation(summary = "Read all payment information", description = "Read all payment information", tags = {"admin-controller"})
    @GetMapping("/payment/all")
    public ResponseEntity<GenericResponse<List<PaymentResponse>>> readAllPaymentForAdmin() {
        return new ResponseEntity<>(transferService.readAllPaymentForAdmin(), HttpStatus.OK);
    }

    @Operation(summary = "Read all payment information with time offset", description = "Read all payment information with time offset", tags = {"admin-controller"})
    @GetMapping("/payment/all-with-time-offset")
    public ResponseEntity<GenericResponse<List<PaymentResponse>>> readAllPaymentForAdmin(
            @RequestParam("monthlyOffset") int monthOffset
    ) {
        return new ResponseEntity<>(transferService.readAllPaymentForAdmin(monthOffset), HttpStatus.OK);
    }

    @Operation(summary = "Read all payment information with time offset", description = "Read all payment information with time offset", tags = {"admin-controller"})
    @GetMapping("/payment/statistics/{customerId}")
    public ResponseEntity<GenericResponse<MonthlyStatisticsResponse>> monthlyStatistics
            (
                    @PathVariable("customerId") Long id,
                    @RequestParam("monthlyOffset") int monthOffset
            ) {
        return new ResponseEntity<>(service.monthlyStatisticsForAdmin(id, monthOffset), HttpStatus.OK);
    }

    @GetMapping("/payment/find-by-customer-id/{customerId}")
    @Operation(summary = "Read all payment information by customer id", description = "Read all payment information by customer id", tags = {"admin-controller"})
    public ResponseEntity<GenericResponse<List<PaymentResponse>>> readAllPaymentForAdminByCustomerId(@PathVariable("customerId") Long customerId) {
        return new ResponseEntity<>(transferService.readAllPaymentForAdminByCustomerId(customerId), HttpStatus.OK);
    }


    @GetMapping("/payment/find-by-card-number/{cardNumber}")
    @Operation(summary = "Read all payment information by card number", description = "Read all payment information by card number", tags = {"admin-controller"})
    public ResponseEntity<GenericResponse<List<PaymentResponse>>> readAllPaymentForAdminByCardNumber(@PathVariable("cardNumber") String cardNumber) {
        return new ResponseEntity<>(transferService.readAllPaymentForAdminByCardNumber(cardNumber), HttpStatus.OK);
    }


    @GetMapping("/payment/find-by-customer-name/{customerName}")
    @Operation(summary = "Read all payment information by customer name", description = "Read all payment information by customer name", tags = {"admin-controller"})
    public ResponseEntity<GenericResponse<List<PaymentResponse>>> readAllPaymentForAdminByCustomerName(@PathVariable("customerName") String customerName) {
        return new ResponseEntity<>(transferService.readAllPaymentForAdminByCustomerName(customerName), HttpStatus.OK);
    }


    @GetMapping("/payment/find-all-with-date-interval")
    @Operation(summary = "Read all payment information with date interval", description = "Read all payment information with date interval: startDate = , how many months ago from now || endDate = how many months until now", tags = {"admin-controller"})
    public ResponseEntity<GenericResponse<List<PaymentResponse>>> readAllPaymentForAdminWithDateInterval(@RequestParam("startDate") Integer startDate, @RequestParam("endDate") Integer endDate) {
        return new ResponseEntity<>(transferService.readAllPaymentForAdminWithDateInterval(startDate, endDate), HttpStatus.OK);
    }

}
