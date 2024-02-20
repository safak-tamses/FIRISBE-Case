package com.firisbe.model.DTO.request;

import java.math.BigDecimal;

public record PaymentMethodRequest(
        String creditCardNumber
) {
}
