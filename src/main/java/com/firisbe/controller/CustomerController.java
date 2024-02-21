package com.firisbe.controller;

import com.firisbe.aspect.GenericResponse;
import com.firisbe.model.DTO.request.CustomerPaymentRequest;
import com.firisbe.model.DTO.request.CustomerUpdateRequest;
import com.firisbe.model.DTO.request.PaymentMethodRequest;
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
@RequestMapping("api/v1/customer")
@SecurityRequirement(name = "bearerAuth")
@AllArgsConstructor
public class CustomerController {
    private final CustomerServiceImplementation service;
    private final TransferServiceImplementation transferService;

    @Operation(summary = "Update customer information", description = "Update customer information", tags = {"customer-controller"})
    @PutMapping
    public ResponseEntity<GenericResponse<CustomerResponse>> updateCustomer(@RequestHeader("Authorization") String token, @RequestBody CustomerUpdateRequest request) {
        return new ResponseEntity<>(service.updateCustomerForCustomers(token, request), HttpStatus.OK);
    }

    @Operation(summary = "Get customer profile information", description = "Get customer profile information", tags = {"customer-controller"})
    @GetMapping("/profile")
    public ResponseEntity<GenericResponse<CustomerResponse>> readCustomer(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(service.readCustomerForCustomers(token), HttpStatus.OK);
    }

    @Operation(summary = "Delete customer information", description = "Delete customer information", tags = {"customer-controller"})
    @DeleteMapping
    public ResponseEntity<GenericResponse<String>> deleteCustomer(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(service.deleteCustomerForCustomers(token), HttpStatus.OK);
    }

    @Operation(summary = "Add payment method for customer", description = "Add payment method for customer", tags = {"customer-controller"})
    @PostMapping("/payment/add-payment-method")
    public ResponseEntity<GenericResponse<String>> addPaymentMethodForCustomer(@RequestHeader("Authorization") String token, @RequestBody PaymentMethodRequest request) {
        return new ResponseEntity<>(service.addPaymentMethod(token, request), HttpStatus.OK);
    }

    @Operation(summary = "Send payment message to Kafka", description = "Send payment message to Kafka", tags = {"customer-controller"})
    @PostMapping("/payment")
    public ResponseEntity<GenericResponse<String>> sendPaymentMessageToKafka(@RequestBody CustomerPaymentRequest request, @RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(transferService.sendPaymentMessageToKafka(request, token), HttpStatus.OK);
    }

    @Operation(summary = "Read payment information", description = "Read payment information", tags = {"customer-controller"})
    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<GenericResponse<PaymentResponse>> readPaymentForCustomer(@RequestHeader("Authorization") String token, @PathVariable(value = "paymentId") Long id) {
        return new ResponseEntity<>(transferService.readPaymentForCustomer(token, id), HttpStatus.OK);
    }

    @Operation(summary = "Read all received payment for customer", description = "Read all received payment for customer", tags = {"customer-controller"})
    @GetMapping("/payment/received/all")
    public ResponseEntity<GenericResponse<List<PaymentResponse>>> readAllReceivedPaymentForCustomer(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(transferService.readAllReceivedPaymentForCustomer(token), HttpStatus.OK);
    }

    @Operation(summary = "Read all sent payment for customer", description = "Read all sent payment for customer", tags = {"customer-controller"})
    @GetMapping("/payment/sent/all")
    public ResponseEntity<GenericResponse<List<PaymentResponse>>> readAllSentPaymentForCustomer(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(transferService.readAllSentPaymentForCustomer(token), HttpStatus.OK);
    }

    @Operation(summary = "Read all payment for customer", description = "Read all payment for customer", tags = {"customer-controller"})
    @GetMapping("/payment/all")
    public ResponseEntity<GenericResponse<List<PaymentResponse>>> readAllPaymentForCustomer(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(transferService.readAllPaymentForCustomer(token), HttpStatus.OK);
    }

    @Operation(summary = "Read all received payment information with time offset", description = "Read all payment information with time offset", tags = {"customer-controller"})
    @GetMapping("/payment/received")
    public ResponseEntity<GenericResponse<List<PaymentResponse>>> readAllReceivedPaymentForCustomer(
            @RequestHeader("Authorization") String token,
            @RequestParam("monthlyOffset") int monthOffset
    ) {
        return new ResponseEntity<>(transferService.readAllReceivedPaymentForCustomer(token, monthOffset), HttpStatus.OK);
    }

    @Operation(summary = "Read all sent payment information with time offset", description = "Read all payment information with time offset", tags = {"customer-controller"})
    @GetMapping("/payment/sent")
    public ResponseEntity<GenericResponse<List<PaymentResponse>>> readAllSentPaymentForCustomer(
            @RequestHeader("Authorization") String token,
            @RequestParam("monthlyOffset") int monthOffset
    ) {
        return new ResponseEntity<>(transferService.readAllSentPaymentForCustomer(token, monthOffset), HttpStatus.OK);
    }

    @Operation(summary = "Read all payment information with time offset", description = "Read all payment information with time offset", tags = {"customer-controller"})
    @GetMapping("/payment/all-with-time-offset")
    public ResponseEntity<GenericResponse<List<PaymentResponse>>> readAllPaymentForCustomer(
            @RequestHeader("Authorization") String token,
            @RequestParam("monthlyOffset") int monthOffset
    ) {
        return new ResponseEntity<>(transferService.readAllPaymentForCustomer(token, monthOffset), HttpStatus.OK);
    }

    @Operation(summary = "Read monthly statistics for customer", description = "Read monthly statistics for customer", tags = {"customer-controller"})
    @GetMapping("/payment/statistics")
    public ResponseEntity<GenericResponse<MonthlyStatisticsResponse>> monthlyStatisticsForCustomer
            (
                    @RequestHeader("Authorization") String token,
                    @RequestParam("monthlyOffset") int monthOffset
            ) {
        return new ResponseEntity<>(service.monthlyStatisticsForCustomer(token, monthOffset), HttpStatus.OK);
    }
}
