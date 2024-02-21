package com.firisbe.service.Interface;

import com.firisbe.aspect.GenericResponse;
import com.firisbe.model.DTO.request.*;
import com.firisbe.model.DTO.response.AuthResponse;
import com.firisbe.model.DTO.response.CustomerResponse;
import com.firisbe.model.DTO.response.MonthlyStatisticsResponse;

import java.util.List;

public interface CustomerServiceInterface {
    GenericResponse<CustomerResponse> updateCustomerForCustomers(String token, CustomerUpdateRequest request);

    GenericResponse<CustomerResponse> updateCustomerForAdmin(AdminCustomerUpdateRequest request);

    GenericResponse<CustomerResponse> readCustomerForAdmin(Long id);

    GenericResponse<CustomerResponse> readCustomerForCustomers(String token);

    GenericResponse<List<CustomerResponse>> readAllForAdmin();

    GenericResponse<String> deleteCustomerForAdmin(Long id);

    GenericResponse<String> deleteCustomerForCustomers(String token);

    GenericResponse<String> deleteAllForAdmin();

    GenericResponse<AuthResponse> customerLogin(CustomerCredentials customerCredentials);

    GenericResponse<AuthResponse> customerRegister(CustomerCreateRequest request);

    GenericResponse<String> addPaymentMethod(String token, PaymentMethodRequest request);

    GenericResponse<MonthlyStatisticsResponse> monthlyStatisticsForCustomer(String token, int monthOffset);

    GenericResponse<MonthlyStatisticsResponse> monthlyStatisticsForAdmin(Long id, int monthOffset);
}
