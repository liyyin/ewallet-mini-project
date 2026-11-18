package com.ewallet.ewallet_be.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface MutationView {
    Long getId();
    String getReferenceNo();
    String getType();
    String getDirection();
    BigDecimal getAmount();
    BigDecimal getBalanceAfter();
    LocalDateTime getCreatedAt();
    String getDescription();
}
