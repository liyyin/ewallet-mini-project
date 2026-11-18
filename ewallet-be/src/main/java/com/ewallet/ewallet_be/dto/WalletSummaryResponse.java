package com.ewallet.ewallet_be.dto;

import java.math.BigDecimal;
import java.util.List;

public record WalletSummaryResponse(
        Long walletId,
        BigDecimal grandTotalIn,
        BigDecimal grandTotalOut,
        BigDecimal grandNet,
        List<MonthlySummaryItem> months
) {
}
