package com.likelion.manyfast.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReplyDraftRequestDto {
    private String receivedMessage;
    private String receiverTimezone = "America/New_York";
    private String replyDirection = "all";
}
