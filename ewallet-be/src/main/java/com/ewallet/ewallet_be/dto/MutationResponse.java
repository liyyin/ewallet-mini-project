package com.ewallet.ewallet_be.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MutationResponse(
        Long id,
        String referenceNo,
        String type,
        String direction,
        BigDecimal amount,
        BigDecimal balanceAfter,
        LocalDateTime createdAt,
        String description
) {
}
