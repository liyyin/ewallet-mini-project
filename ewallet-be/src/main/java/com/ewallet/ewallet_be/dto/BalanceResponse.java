package com.ewallet.ewallet_be.dto;

import java.math.BigDecimal;

public record BalanceResponse(Long walletId, Long userId, BigDecimal balance, String currency) {
}
