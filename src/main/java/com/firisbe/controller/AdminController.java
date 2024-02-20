package com.firisbe.controller;

import com.firisbe.aspect.GenericResponse;
import com.firisbe.model.DTO.request.AdminCustomerUpdateRequest;
import com.firisbe.model.DTO.request.CustomerUpdateRequest;
import com.firisbe.model.DTO.response.CustomerResponse;
import com.firisbe.model.DTO.response.MonthlyStatisticsResponse;
import com.firisbe.model.DTO.response.PaymentResponse;
import com.firisbe.service.Implementation.CustomerServiceImplementation;
import com.firisbe.service.Implementation.TransferServiceImplementation;
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

    @PutMapping
    public ResponseEntity<GenericResponse<CustomerResponse>> updateCustomer(@RequestBody AdminCustomerUpdateRequest request) {
        return new ResponseEntity<>(service.updateCustomerForAdmin(request), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<CustomerResponse>> readCustomer(@PathVariable(value = "id") Long id) {
        return new ResponseEntity<>(service.readCustomerForAdmin(id), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<GenericResponse<List<CustomerResponse>>> readAllCustomers() {
        return new ResponseEntity<>(service.readAllForAdmin(), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<String>> deleteCustomer(@PathVariable(value = "id") Long id) {
        return new ResponseEntity<>(service.deleteCustomerForAdmin(id), HttpStatus.OK);
    }

    @DeleteMapping("/all")
    public ResponseEntity<GenericResponse<String>> deleteAllCustomers() {
        return new ResponseEntity<>(service.deleteAllForAdmin(), HttpStatus.OK);
    }

    @GetMapping("/payment/{id}")
    public ResponseEntity<GenericResponse<PaymentResponse>> readPaymentForAdmin(@PathVariable(value = "id") Long id) {
        return new ResponseEntity<>(transferService.readPaymentForAdmin(id), HttpStatus.OK);
    }

    @GetMapping("/payment/all")
    public ResponseEntity<GenericResponse<List<PaymentResponse>>> readAllPaymentForAdmin() {
        return new ResponseEntity<>(transferService.readAllPaymentForAdmin(), HttpStatus.OK);
    }

    @GetMapping("/payment/statistics/{id}")
    public ResponseEntity<GenericResponse<MonthlyStatisticsResponse>> monthlyStatistics(@PathVariable(value = "id") Long id) {
        return new ResponseEntity<>(service.monthlyStatisticsForAdmin(id), HttpStatus.OK);
    }

}
