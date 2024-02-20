package com.firisbe.model.DTO.response;

import com.firisbe.model.Customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Customer sentCustomer,
        Customer receiverCustomer,
        BigDecimal amount,
        LocalDateTime timestamp
) {
}
