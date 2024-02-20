package com.firisbe.aspect;

import com.firisbe.error.CustomerAlreadyExistsException;
import com.firisbe.error.CustomerNotFoundException;
import com.firisbe.error.UpdateCustomerRuntimeException;
import com.firisbe.model.DTO.request.GenericExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@ControllerAdvice
public class GenericException extends ResponseEntityExceptionHandler {
    @ExceptionHandler({
            CustomerAlreadyExistsException.class,
            CustomerNotFoundException.class,
            UpdateCustomerRuntimeException.class
    })
    public ResponseEntity<Object> handleCustomException(Exception e) {
        GenericExceptionResponse error = new GenericExceptionResponse(e.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.OK);
    }




}
