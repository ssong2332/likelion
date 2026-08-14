package com.likelion.manyfast.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.manyfast.ai.dto.RefineRequestDto;
import com.likelion.manyfast.ai.dto.RefineResponseDto;
import com.likelion.manyfast.ai.dto.ReplyDraftRequestDto;
import com.likelion.manyfast.ai.dto.ReplyDraftResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiService {

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.api-url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public RefineResponseDto refineMessage(RefineRequestDto request) {
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                String systemPrompt = "You are Manyfast AI Assistant. "
                        + "Analyze the user's business message draft and return structured JSON matching: "
                        + "refinedText, backTranslation, extractedInfo (purpose, assignee, deadline, urgency, businessImpact), "
                        + "missingInfoWarnings, riskyExpressions, appliedGlossary.";

                Map<String, Object> payload = new HashMap<>();
                payload.put("model", model);
                payload.put("response_format", Map.of("type", "json_object"));
                payload.put("messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", request.getOriginalText())
                ));

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    String content = root.path("choices").get(0).path("message").path("content").asText();
                    return objectMapper.readValue(content, RefineResponseDto.class);
                }
            } catch (Exception e) {
                System.err.println("[AiService] OpenAI API 호출 실패, 목업 엔진으로 대체: " + e.getMessage());
            }
        }

        // Mock Fallback matching API_CONTRACT.md
        return RefineResponseDto.builder()
                .refinedText("PR #142 is currently blocking today's release schedule. Could you please prioritize reviewing the Manyfast terminology and confirm your feedback by EOD?")
                .backTranslation("PR #142가 현재 오늘의 배포 일정을 지연시키고 있습니다. Manyfast 용어를 확인해 주시고 오늘 EOD까지 피드백을 검토해 주시겠어요?")
                .extractedInfo(Map.of(
                        "purpose", "PR 코드 리뷰 및 배포 블로커 해소",
                        "assignee", "수신자",
                        "deadline", "오늘 EOD (18:00)",
                        "urgency", "critical",
                        "businessImpact", "오늘자 릴리스 배포 일정 지연"
                ))
                .missingInfoWarnings(List.of(Map.of(
                        "type", "deadline_detail",
                        "warning", "구체적인 시간 기준이 모호합니다.",
                        "suggestedCompletion", "현지 시각 기준 '오늘 18:00 EST'로 명시하는 것을 권장합니다."
                )))
                .riskyExpressions(List.of(Map.of(
                        "originalPhrase", "안 봐주셔서",
                        "reason", "상대방에 대한 직접적 비난으로 오해될 수 있음",
                        "replacedWith", "is currently blocking schedule"
                )))
                .appliedGlossary(List.of(Map.of(
                        "term", "Manyfast",
                        "rule", "원문 유지 (Keep Original)",
                        "matchedInRefined", true
                )))
                .build();
    }

    public ReplyDraftResponseDto generateReplyDrafts(ReplyDraftRequestDto request) {
        return ReplyDraftResponseDto.builder()
                .analyzedRequest(Map.of(
                        "summary", "아키텍처 초안에 대한 피드백 논의 요청 (완곡한 수정 요구 가능성 높음)",
                        "urgency", "normal",
                        "actionRequired", "미팅 시간 조율 및 사전 피드백 확인"
                ))
                .suggestedReplies(List.of(
                        Map.of(
                                "direction", "accept",
                                "title", "즉시 수락 및 미팅 제안",
                                "draftText", "Thanks for taking a look! I would be glad to discuss your feedback. Would [선호하는 요일/시간] work for a quick sync?"
                        ),
                        Map.of(
                                "direction", "request_details",
                                "title", "사전 코멘트 서면 요청",
                                "draftText", "Thank you for the review. Could you leave a few notes in the [문서/티켓 링크] first so I can prepare before we jump into a call?"
                        ),
                        Map.of(
                                "direction", "schedule",
                                "title", "일정 지연 및 추후 조율",
                                "draftText", "Thanks for checking it. I am currently focusing on [진행 중인 작업], but I will reach out by [조율 가능 시점] to schedule our discussion."
                        )
                ))
                .build();
    }
}
