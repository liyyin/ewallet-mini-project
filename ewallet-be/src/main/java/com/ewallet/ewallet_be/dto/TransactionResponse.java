package com.ewallet.ewallet_be.dto;

import com.ewallet.ewallet_be.enums.TransactionStatus;
import com.ewallet.ewallet_be.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionResponse(
        Long transactionId,
        String referenceNo,
        TransactionType type,
        BigDecimal amount,
        TransactionStatus status,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        LocalDateTime createdAt,
        Long destinationWalletId
) {
    public TransactionResponse(Long transactionId,
                               String referenceNo,
                               TransactionType type,
                               BigDecimal amount,
                               TransactionStatus status,
                               BigDecimal balanceBefore,
                               BigDecimal balanceAfter,
                               LocalDateTime createdAt){
        this(transactionId,referenceNo,type,amount,status,balanceBefore,balanceAfter,createdAt,null);
    }
}
