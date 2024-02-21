package com.firisbe.service.Interface;

import com.firisbe.aspect.GenericResponse;
import com.firisbe.model.DTO.request.*;
import com.firisbe.model.DTO.response.AuthResponse;
import com.firisbe.model.DTO.response.CustomerResponse;
import com.firisbe.model.DTO.response.MonthlyStatisticsResponse;

import java.util.List;

public interface CustomerServiceInterface {
    public GenericResponse<CustomerResponse> updateCustomerForCustomers(String token, CustomerUpdateRequest request);

    public GenericResponse<CustomerResponse> updateCustomerForAdmin(AdminCustomerUpdateRequest request);

    public GenericResponse<CustomerResponse> readCustomerForAdmin(Long id);

    public GenericResponse<CustomerResponse> readCustomerForCustomers(String token);

    public GenericResponse<List<CustomerResponse>> readAllForAdmin();

    public GenericResponse<String> deleteCustomerForAdmin(Long id);

    GenericResponse<String> deleteCustomerForCustomers(String token);

    public GenericResponse<String> deleteAllForAdmin();

    public GenericResponse<AuthResponse> customerLogin(CustomerCredentials customerCredentials);

    public GenericResponse<AuthResponse> customerRegister(CustomerCreateRequest request);

    public GenericResponse<String> addPaymentMethod(String token, PaymentMethodRequest request);

    public GenericResponse<MonthlyStatisticsResponse> monthlyStatisticsForCustomer(String token, int monthOffset);

    public GenericResponse<MonthlyStatisticsResponse> monthlyStatisticsForAdmin(Long id, int monthOffset);
}
