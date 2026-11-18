package com.ewallet.ewallet_be.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserInfo(Long id, String fullName, String status) {
}
