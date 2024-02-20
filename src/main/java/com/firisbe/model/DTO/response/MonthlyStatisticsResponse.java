package com.firisbe.model.DTO.response;

import java.math.BigDecimal;

public record MonthlyStatisticsResponse(
        BigDecimal monthlyReceivedAmount,
        BigDecimal monthlySentAmount,
        Integer receivedCount,
        Integer sentCount,
        BigDecimal highestAmountSent,
        BigDecimal lowestAmountSent,
        BigDecimal highestAmountReceived,
        BigDecimal lowestAmountReceived,
        BigDecimal averageAmountSent,
        BigDecimal averageAmountReceived
) {
}
