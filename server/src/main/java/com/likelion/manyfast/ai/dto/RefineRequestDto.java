package com.likelion.manyfast.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class RefineRequestDto {
    private String originalText;
    private String sourceLang = "ko";
    private String targetLang = "en";
    private String senderTimezone = "Asia/Seoul";
    private String receiverTimezone = "America/New_York";
    private Map<String, Object> collaborationStyle;
    private List<String> appliedGlossaryIds;
    private List<String> appliedRuleIds;
}
