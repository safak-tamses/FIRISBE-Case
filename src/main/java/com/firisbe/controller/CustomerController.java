package com.firisbe.controller;

import com.firisbe.aspect.GenericResponse;
import com.firisbe.model.DTO.request.CustomerPaymentRequest;
import com.firisbe.model.DTO.request.CustomerUpdateRequest;
import com.firisbe.model.DTO.response.CustomerResponse;
import com.firisbe.model.DTO.response.PaymentResponse;
import com.firisbe.model.Transfer;
import com.firisbe.service.Implementation.CustomerServiceImplementation;
import com.firisbe.service.Implementation.TransferServiceImplementation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/customer")
@SecurityRequirement(name = "bearerAuth")
@AllArgsConstructor
public class CustomerController {
    private final CustomerServiceImplementation service;
    private final TransferServiceImplementation transferService;

    @PutMapping
    public ResponseEntity<GenericResponse<CustomerResponse>> updateCustomer(@RequestHeader("Authorization") String token, @RequestBody CustomerUpdateRequest request) {
        return new ResponseEntity<>(service.updateCustomerForCustomers(token, request), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<CustomerResponse>> readCustomer(@RequestHeader("Authorization") String token, @PathVariable(value = "id") Long id) {
        return new ResponseEntity<>(service.readCustomerForCustomers(token, id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<String>> deleteCustomer(@RequestHeader("Authorization") String token, @PathVariable(value = "id") Long id) {
        return new ResponseEntity<>(service.deleteCustomerForCustomers(token, id), HttpStatus.OK);
    }

    @PostMapping("/payment")
    public ResponseEntity<GenericResponse<String>> sendPaymentMessageToKafka(@RequestBody CustomerPaymentRequest request, @RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(transferService.sendPaymentMessageToKafka(request, token), HttpStatus.OK);
    }

    @GetMapping("/payment/{id}")
    public ResponseEntity<GenericResponse<PaymentResponse>> readPaymentForCustomer(@RequestHeader("Authorization") String token, @PathVariable(value = "id") Long id) {
        return new ResponseEntity<>(transferService.readPaymentForCustomer(token, id), HttpStatus.OK);
    }

    @GetMapping("/payment/received/all")
    public ResponseEntity<GenericResponse<List<PaymentResponse>>> readAllReceivedPaymentForCustomer(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(transferService.readAllReceivedPaymentForCustomer(token),HttpStatus.OK);
    }

    @GetMapping("/payment/sent/all")
    public ResponseEntity<GenericResponse<List<PaymentResponse>>> readAllSentPaymentForCustomer(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(transferService.readAllSentPaymentForCustomer(token),HttpStatus.OK);
    }

    @GetMapping("/payment/all")
    public ResponseEntity<GenericResponse<List<PaymentResponse>>> readAllPaymentForCustomer(@RequestHeader("Authorization") String token){
    return new ResponseEntity<>(transferService.readAllPaymentForCustomer(token),HttpStatus.OK);
    }
}
