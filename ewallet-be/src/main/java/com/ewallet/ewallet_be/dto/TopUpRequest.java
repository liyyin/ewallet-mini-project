package com.ewallet.ewallet_be.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TopUpRequest(
        @NotBlank @Size(max = 25) String referenceNo,
        @NotNull @DecimalMin(value = "10000",message = "Minimum Top up 10000") @Digits(integer = 17,fraction = 2,message = "max 2 decimal places") BigDecimal amount
        ) {
}
