package com.ewallet.user_service.dto;

import com.ewallet.user_service.enums.UserStatus;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        UserStatus status,
        LocalDateTime createdAt
) {
}
