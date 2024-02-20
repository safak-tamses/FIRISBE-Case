package com.firisbe.model.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;


@AllArgsConstructor
@Getter
@Setter
public class GenericExceptionResponse {
    private String errorMessage;
    private LocalDateTime errorDate;
}
