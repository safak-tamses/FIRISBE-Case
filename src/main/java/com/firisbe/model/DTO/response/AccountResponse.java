package com.firisbe.model.DTO.response;

import java.math.BigDecimal;

public record AccountResponse(
        String creditCardNumber,
        BigDecimal balance
) {
}
