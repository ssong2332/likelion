package com.likelion.manyfast.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyDraftResponseDto {
    private Map<String, Object> analyzedRequest;
    private List<Map<String, String>> suggestedReplies;
}
