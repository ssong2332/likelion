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
public class RefineResponseDto {
    private String refinedText;
    private String backTranslation;
    private Map<String, Object> extractedInfo;
    private List<Map<String, String>> missingInfoWarnings;
    private List<Map<String, String>> riskyExpressions;
    private List<Map<String, Object>> appliedGlossary;
    private Map<String, Object> timezoneInfo;
}
