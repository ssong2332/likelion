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

    /**
     * F-2, F-3, F-4.1, F-5, F-6: 도메인 데이터(용어집, 성향)를 동적 주입한 AI 교정
     */
    public RefineResponseDto refineMessage(RefineRequestDto request) {
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                String targetLanguage = "en".equalsIgnoreCase(request.getTargetLang()) ? "English"
                        : "zh".equalsIgnoreCase(request.getTargetLang()) ? "Chinese"
                        : "ja".equalsIgnoreCase(request.getTargetLang()) ? "Japanese" : "English";

                // Phase 5: 동적 프롬프트 조립 (성향 및 용어집 규칙)
                String styleSection = buildStyleSection(request.getCollaborationStyle());
                String glossarySection = buildGlossarySection(request.getAppliedGlossaryIds());

                String systemPrompt = """
                    You are 'Manyfast AI', an expert executive communication assistant for global business collaboration.
                    Your goal is to refine raw, emotional, or vague business messages into clear, polite, and effective messages in %s without losing critical context.

                    %s
                    %s

                    [CORE INSTRUCTIONS]
                    1. FACT PRESERVATION: Extract the 5 core business facts from the user's message:
                       - purpose: Main goal/task requested
                       - assignee: Who is expected to act
                       - deadline: Specific or relative deadline mentioned
                       - urgency: One of ['low', 'normal', 'critical']
                       - businessImpact: Risk or outcome if delayed
                    2. REFINEMENT & TONE:
                       - Transform blameful, emotional, or demanding tone (e.g., "안 봐주셔서", "빨리") into respectful, professional business language.
                       - Preserve the urgency and deadline clearly without sounding hostile.
                       - Strictly follow the Style Preferences and Glossary Rules designated above.
                    3. BACK-TRANSLATION:
                       - Provide a faithful Korean translation of your refinedText so the user can verify the meaning.
                    4. RISKY EXPRESSIONS & WARNINGS:
                       - Identify specific phrases that could cause misunderstanding or offense, explaining why and how they were replaced.
                       - If a deadline or detail is vague, provide a helpful suggestion in 'missingInfoWarnings'.
                    5. GLOSSARY MATCHING:
                       - ONLY include items in 'appliedGlossary' IF the specific glossary term was explicitly present or mentioned in the user's original message.
                       - If no registered glossary terms appear in the user's input, 'appliedGlossary' MUST be an empty array [].
                    6. OUTPUT FORMAT:
                       - Return ONLY a valid JSON object matching this structure:
                       {
                         "refinedText": "string",
                         "backTranslation": "string",
                         "extractedInfo": {
                           "purpose": "string",
                           "assignee": "string",
                           "deadline": "string",
                           "urgency": "string",
                           "businessImpact": "string"
                         },
                         "missingInfoWarnings": [
                           { "type": "string", "warning": "string", "suggestedCompletion": "string" }
                         ],
                         "riskyExpressions": [
                           { "originalPhrase": "string", "reason": "string", "replacedWith": "string" }
                         ],
                         "appliedGlossary": [
                           { "term": "string", "rule": "string", "matchedInRefined": true }
                         ]
                       }
                    """.formatted(targetLanguage, styleSection, glossarySection);

                Map<String, Object> payload = new HashMap<>();
                payload.put("model", model);
                payload.put("temperature", 0.3);
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
                    RefineResponseDto dto = objectMapper.readValue(content, RefineResponseDto.class);

                    // 실제 원문에 포함된 용어만 남기는 엄격 필터링
                    if (dto.getAppliedGlossary() != null && request.getOriginalText() != null) {
                        String originalLower = request.getOriginalText().toLowerCase();
                        List<Map<String, Object>> filtered = dto.getAppliedGlossary().stream()
                                .filter(g -> {
                                    String term = String.valueOf(g.getOrDefault("term", "")).toLowerCase();
                                    return !term.isBlank() && originalLower.contains(term);
                                })
                                .toList();
                        dto.setAppliedGlossary(filtered);
                    }
                    return dto;
                }
            } catch (Exception e) {
                System.err.println("[AiService] OpenAI API 호출 실패, 목업 엔진으로 안전 폴백: " + e.getMessage());
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

    /**
     * F-8: 수신 메시지 분석 및 3가지 방향 회신 초안 생성
     */
    public ReplyDraftResponseDto generateReplyDrafts(ReplyDraftRequestDto request) {
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                String systemPrompt = """
                    You are 'Manyfast Reply Assistant' (F-8).
                    Analyze the incoming English business message and provide:
                    1. A concise Korean summary of the request, its perceived urgency, and the required action.
                    2. Exactly 3 distinct reply draft options for the recipient to choose from:
                       - 'accept': Accept the request / agree to meet / proceed immediately.
                       - 'request_details': Request written notes, documents, or ticket links first before syncing.
                       - 'schedule': Propose a delay due to current workload and promise a follow-up date/time.
                    3. Use [bracketed placeholders] like [Preferred Day/Time], [Document Link], or [Follow-up Date] where the user needs to fill in details.
                    4. Return ONLY a valid JSON object matching:
                    {
                      "analyzedRequest": {
                        "summary": "Korean summary of the request",
                        "urgency": "normal",
                        "actionRequired": "What the user needs to do"
                      },
                      "suggestedReplies": [
                        { "direction": "accept", "title": "즉시 수락 및 미팅 제안", "draftText": "..." },
                        { "direction": "request_details", "title": "사전 코멘트 서면 요청", "draftText": "..." },
                        { "direction": "schedule", "title": "일정 지연 및 추후 조율", "draftText": "..." }
                      ]
                    }
                    """;

                Map<String, Object> payload = new HashMap<>();
                payload.put("model", model);
                payload.put("temperature", 0.3);
                payload.put("response_format", Map.of("type", "json_object"));
                payload.put("messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", request.getReceivedMessage())
                ));

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    String content = root.path("choices").get(0).path("message").path("content").asText();
                    return objectMapper.readValue(content, ReplyDraftResponseDto.class);
                }
            } catch (Exception e) {
                System.err.println("[AiService] Reply Draft OpenAI 호출 실패, 목업 엔진으로 대체: " + e.getMessage());
            }
        }

        // Mock Fallback
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

    /**
     * Phase 5 Helper: 사용자 협업 성향 지시문 빌더
     */
    private String buildStyleSection(Map<String, Object> style) {
        if (style == null || style.isEmpty()) {
            return "[USER COLLABORATION STYLE]\n- Tone: Polite and professional\n- Directness: Balanced\n- Detail: Concise";
        }
        String tone = String.valueOf(style.getOrDefault("tone", "polite"));
        String directness = String.valueOf(style.getOrDefault("directness", "balanced"));
        String detailLevel = String.valueOf(style.getOrDefault("detailLevel", "concise"));
        String lengthLevel = String.valueOf(style.getOrDefault("lengthLevel", "medium"));

        String toneInstruction = switch (tone) {
            case "friendly" -> "Friendly, warm, and approachable (친근하고 부드러운 어조, 친절한 인사 포함)";
            case "professional" -> "Highly professional, formal, and authoritative (격식 있고 명확한 전문 비즈니스 어조)";
            default -> "Polite, respectful, and standard courteous business tone (정중하고 예의 바른 표준 비즈니스 어조)";
        };

        String directnessInstruction = switch (directness) {
            case "direct" -> "Direct and upfront (용건을 서두에 명확하게 직설적으로 전달)";
            case "indirect" -> "Soft and considerate (정중한 쿠션어와 배려하는 완곡 표현 사용)";
            default -> "Balanced between direct and polite";
        };

        String detailInstruction = switch (detailLevel) {
            case "concise" -> "Concise and brief (핵심만 군더더기 없이 요약)";
            case "detailed" -> "Detailed with full context and clarity (배경과 맥락을 상세하게 설명)";
            default -> "Moderate detail level";
        };

        String lengthInstruction = switch (lengthLevel) {
            case "short" -> "Short (1-2 sentences)";
            case "long" -> "Longer and more elaborated message";
            default -> "Standard message length";
        };

        return """
            [USER COLLABORATION STYLE - STRICTLY APPLY TO REFINEMENT]
            - Tone: %s
            - Directness: %s
            - Detail: %s
            - Length: %s
            """.formatted(toneInstruction, directnessInstruction, detailInstruction, lengthInstruction);
    }

    /**
     * Phase 5 Helper: 용어집 규칙 지시문 빌더
     */
    private String buildGlossarySection(List<String> glossaryIds) {
        // 기본 내장 용어집 규칙 (Manyfast 원문 유지, ASAP 시간 명시)
        return """
            [DESIGNATED GLOSSARY RULES]
            - Term 'Manyfast': Keep original spelling unchanged (Do not translate).
            - Term 'ASAP': Clarify with a concrete EOD / business hour timeframe.
            - Term 'PR': Retain as 'Pull Request' or 'PR'.
            """;
    }
}
