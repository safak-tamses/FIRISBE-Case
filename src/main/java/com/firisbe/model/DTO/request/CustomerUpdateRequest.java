package com.firisbe.model.DTO.request;


public record CustomerUpdateRequest(
        Long id,
        String name,
        String lastName,
        String email,
        String password
){
}
