package com.likelion.manyfast.domain.glossary.dto;

public record GlossaryErrorResponse(
        int status,
        String error,
        String message
) {
}
