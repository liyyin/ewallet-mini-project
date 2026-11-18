package com.ewallet.ewallet_be.repository.projection;

import java.math.BigDecimal;

public interface MonthlySummaryView {
    String getPeriodMonth();
    BigDecimal getTotalIn();
    BigDecimal getTotalOut();
    BigDecimal getNet();
    BigDecimal getPreviousNet();
    Long getEntryCount();
}
