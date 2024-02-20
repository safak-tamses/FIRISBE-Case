package com.firisbe.model.DTO.request;

public record AdminCustomerUpdateRequest(
        Long id,
        String name,
        String lastName,
        String email,
        String password,
        String creditCardNumber
) {
}
