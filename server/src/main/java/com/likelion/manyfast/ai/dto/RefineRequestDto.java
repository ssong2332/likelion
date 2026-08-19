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
public class RefineRequestDto {
    private String originalText;
    private String sourceLang = "ko";
    private String targetLang = "en";
    private String senderTimezone = "Asia/Seoul";
    private String receiverTimezone = "America/New_York";
    private Map<String, Object> collaborationStyle;
    private List<Long> appliedGlossaryIds;
    private List<Long> appliedRuleIds;
}
