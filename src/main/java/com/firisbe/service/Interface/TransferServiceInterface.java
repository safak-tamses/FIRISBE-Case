package com.firisbe.service.Interface;

import com.firisbe.aspect.GenericResponse;
import com.firisbe.model.DTO.request.CustomerPaymentRequest;
import com.firisbe.model.DTO.response.PaymentResponse;
import com.firisbe.model.Transfer;

import java.util.List;

public interface TransferServiceInterface {
    public GenericResponse<String> sendPaymentMessageToKafka(CustomerPaymentRequest request, String token);

    public void processPaymentMessageFromKafka(String request);

    GenericResponse<PaymentResponse> readPaymentForCustomer(String token, Long id);

    GenericResponse<List<PaymentResponse>> readAllReceivedPaymentForCustomer(String token);

    GenericResponse<List<PaymentResponse>> readAllSentPaymentForCustomer(String token);

    GenericResponse<List<PaymentResponse>> readAllPaymentForCustomer(String token);

    GenericResponse<List<PaymentResponse>> readAllReceivedPaymentForCustomer(String token, int monthOffset);

    GenericResponse<List<PaymentResponse>> readAllSentPaymentForCustomer(String token, int monthOffset);

    GenericResponse<List<PaymentResponse>> readAllPaymentForCustomer(String token, int monthOffset);

    GenericResponse<PaymentResponse> readPaymentForAdmin(Long id);

    GenericResponse<List<PaymentResponse>> readAllPaymentForAdmin();
    GenericResponse<List<PaymentResponse>> readAllPaymentForAdmin(int monthOffset);

}
