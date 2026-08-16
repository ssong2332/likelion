package com.likelion.manyfast.domain.history.dto;

import com.likelion.manyfast.domain.history.MessageHistory;

import java.time.Instant;

public record MessageHistoryResponse(
        Long id,
        String originalText,
        String resultText,
        Instant createdAt
) {

    public static MessageHistoryResponse from(MessageHistory messageHistory) {
        return new MessageHistoryResponse(
                messageHistory.getId(),
                messageHistory.getOriginalText(),
                messageHistory.getResultText(),
                messageHistory.getCreatedAt()
        );
    }
}
