package com.ewallet.ewallet_be.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull Long fromWalletId,
        @NotNull Long toWalletId,
        @NotBlank @Size(max = 25) String referenceNo,
        @NotBlank @Size(max = 255) String description,
        @NotNull @DecimalMin(value = "10000",message = "Minimum transfer is 10000") @Digits(integer = 17, fraction = 2,message = "Max 2 decimal places")BigDecimal amount
        ) {
}
