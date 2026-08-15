package com.likelion.manyfast.domain.timezone.dto;

public record TimezoneErrorResponse(
        int status,
        String error,
        String message
) {
}
