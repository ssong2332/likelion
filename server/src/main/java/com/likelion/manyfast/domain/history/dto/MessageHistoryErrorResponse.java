package com.likelion.manyfast.domain.history.dto;

public record MessageHistoryErrorResponse(
        int status,
        String error,
        String message
) {
}
