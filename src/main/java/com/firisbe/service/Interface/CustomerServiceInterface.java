package com.firisbe.service.Interface;

import com.firisbe.aspect.GenericResponse;
import com.firisbe.model.DTO.request.CustomerCreateRequest;
import com.firisbe.model.DTO.request.CustomerCredentials;
import com.firisbe.model.DTO.request.CustomerUpdateRequest;
import com.firisbe.model.DTO.response.AuthResponse;
import com.firisbe.model.DTO.response.CustomerResponse;

import java.util.List;

public interface CustomerServiceInterface {
    public GenericResponse<CustomerResponse> updateCustomerForCustomers(String token, CustomerUpdateRequest request);
    public GenericResponse<CustomerResponse> updateCustomerForAdmin(CustomerUpdateRequest request);
    public GenericResponse<CustomerResponse> readCustomerForAdmin(Long id);
    public GenericResponse<CustomerResponse> readCustomerForCustomers(String token, Long id);
    public GenericResponse<List<CustomerResponse>> readAllForAdmin();
    public GenericResponse<String> deleteCustomerForAdmin(Long id);
    public GenericResponse<String> deleteCustomerForCustomers(String token, Long id);
    public GenericResponse<String> deleteAllForAdmin();
    public GenericResponse<AuthResponse> customerLogin(CustomerCredentials customerCredentials);
    public GenericResponse<AuthResponse> customerRegister(CustomerCreateRequest request);
}
