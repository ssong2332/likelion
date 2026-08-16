package com.likelion.manyfast.domain.userstyle.dto;

import com.likelion.manyfast.domain.userstyle.CollaborationStyle;

public record CollaborationStyleResponse(
        String tone,
        String directness,
        String detailLevel
) {

    public static CollaborationStyleResponse from(CollaborationStyle style) {
        return new CollaborationStyleResponse(
                style.getTone(),
                style.getDirectness(),
                style.getDetailLevel()
        );
    }
}
