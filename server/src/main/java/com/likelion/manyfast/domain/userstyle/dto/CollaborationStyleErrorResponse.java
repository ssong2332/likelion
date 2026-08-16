package com.likelion.manyfast.domain.userstyle.dto;

public record CollaborationStyleErrorResponse(
        int status,
        String error,
        String message
) {
}
