package com.firisbe.model.DTO.request;




public record CustomerUpdateRequest(
        String name,
        String lastName,
        String email,
        String password,
        String creditCardNumber
){
}
