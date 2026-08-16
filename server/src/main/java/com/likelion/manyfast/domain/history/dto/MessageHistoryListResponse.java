package com.likelion.manyfast.domain.history.dto;

import java.util.List;

public record MessageHistoryListResponse(
        long totalCount,
        List<MessageHistoryResponse> history
) {

    public static MessageHistoryListResponse from(List<MessageHistoryResponse> history) {
        return new MessageHistoryListResponse(history.size(), history);
    }
}
