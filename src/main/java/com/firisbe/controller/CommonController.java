package com.firisbe.controller;

import com.firisbe.aspect.GenericResponse;
import com.firisbe.model.DTO.request.CustomerCreateRequest;
import com.firisbe.model.DTO.request.CustomerCredentials;
import com.firisbe.model.DTO.response.AuthResponse;
import com.firisbe.service.Implementation.CustomerServiceImplementation;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/common")
@AllArgsConstructor
public class CommonController {
    private final CustomerServiceImplementation service;

    @PostMapping("/register")
    public ResponseEntity<GenericResponse<AuthResponse>> customerRegister(@RequestBody CustomerCreateRequest request) {
        return new ResponseEntity<>(service.customerRegister(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<GenericResponse<AuthResponse>> customerLogin(@RequestBody CustomerCredentials request) {
        return new ResponseEntity<>(service.customerLogin(request), HttpStatus.OK);
    }
}
