package com.ewallet.ewallet_be.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MonthlySummaryItem(
    String month,
    BigDecimal totalIn,
    BigDecimal totalOut,
    BigDecimal net,
    BigDecimal previousNet,
    long entryCount
) {
}
