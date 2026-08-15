package com.likelion.manyfast.domain.rules.dto;

public record RuleErrorResponse(
        int status,
        String error,
        String message
) {
}
