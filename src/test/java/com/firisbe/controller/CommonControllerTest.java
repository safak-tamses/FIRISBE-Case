package com.firisbe.controller;

import com.firisbe.aspect.GenericResponse;
import com.firisbe.model.DTO.request.CustomerCreateRequest;
import com.firisbe.model.DTO.request.CustomerCredentials;
import com.firisbe.model.DTO.response.AuthResponse;
import com.firisbe.service.Implementation.CustomerServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class CommonControllerTest {

    @Mock
    CustomerServiceImplementation service;

    CommonController controller;

    @BeforeEach
    void setUp() {
        initMocks(this);
        controller = new CommonController(service);
    }

    @Test
    void customerRegister() {
        // Given
        CustomerCreateRequest request = new CustomerCreateRequest("John", "Doe", "john.doe@example.com", "password");

        AuthResponse authResponse = new AuthResponse("token", "john.doe");

        when(service.customerRegister(request)).thenReturn(new GenericResponse<AuthResponse>(authResponse, true));

        // When
        ResponseEntity<GenericResponse<AuthResponse>> responseEntity = controller.customerRegister(request);

        // Then
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(authResponse, responseEntity.getBody().getData());
        verify(service, times(1)).customerRegister(request);
    }

    @Test
    void customerLogin() {
        // Given
        CustomerCredentials request = new CustomerCredentials("john.doe@example.com", "password");

        AuthResponse authResponse = new AuthResponse("token", "john.doe");

        when(service.customerLogin(request)).thenReturn(new GenericResponse<>(authResponse, true));

        // When
        ResponseEntity<GenericResponse<AuthResponse>> responseEntity = controller.customerLogin(request);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(authResponse, responseEntity.getBody().getData());
        verify(service, times(1)).customerLogin(request);
    }
}