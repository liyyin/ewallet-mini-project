package com.ewallet.ewallet_be.dto;

import jakarta.validation.constraints.NotNull;

public record CreateWalletRequest(
        @NotNull Long id
) {
}
