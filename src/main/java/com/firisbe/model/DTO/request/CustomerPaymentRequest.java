package com.firisbe.model.DTO.request;

import java.math.BigDecimal;

public record CustomerPaymentRequest(
    String receiveMail,
    BigDecimal amount
) {
}
