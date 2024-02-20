package com.firisbe.model.DTO.request;

public record CustomerCreateRequest(
        String name,
        String lastName,
        String email,
        String password
) {
}
