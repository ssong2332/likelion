package com.likelion.manyfast.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.manyfast.ai.dto.RefineRequestDto;
import com.likelion.manyfast.ai.dto.RefineResponseDto;
import com.likelion.manyfast.ai.dto.ReplyDraftRequestDto;
import com.likelion.manyfast.ai.dto.ReplyDraftResponseDto;
import com.likelion.manyfast.domain.glossary.GlossaryService;
import com.likelion.manyfast.domain.glossary.dto.GlossaryResponse;
import com.likelion.manyfast.domain.rules.RuleService;
import com.likelion.manyfast.domain.rules.dto.RuleResponse;
import com.likelion.manyfast.domain.userstyle.CollaborationStyleService;
import com.likelion.manyfast.domain.userstyle.dto.CollaborationStyleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AiService {

    private final GlossaryService glossaryService;
    private final RuleService ruleService;
    private final CollaborationStyleService collaborationStyleService;

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
        RefineContext context = prepareRefineContext(request);

        if (apiKey != null && !apiKey.isBlank()) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("model", model);
                payload.put("temperature", 0.7);
                payload.put("response_format", Map.of("type", "json_object"));
                payload.put("messages", List.of(
                        Map.of("role", "system", "content", context.systemPrompt()),
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

        String fallbackRefinedText = "PR #142 is currently blocking today's release schedule. Could you please prioritize reviewing the Manyfast terminology and confirm your feedback by EOD?";

        // Mock Fallback matching API_CONTRACT.md
        return RefineResponseDto.builder()
                .refinedText(fallbackRefinedText)
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
                .appliedGlossary(buildFallbackAppliedGlossary(
                        request.getOriginalText(),
                        fallbackRefinedText,
                        context.glossaries()
                ))
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

    RefineContext prepareRefineContext(RefineRequestDto request) {
        List<GlossaryResponse> glossaries = glossaryService.findByIds(
                safeIds(request.getAppliedGlossaryIds())
        );
        List<RuleResponse> rules = ruleService.findByIds(
                safeIds(request.getAppliedRuleIds())
        );
        CollaborationStyleResponse savedStyle = collaborationStyleService.get();
        CollaborationStyleResponse effectiveStyle = mergeCollaborationStyle(
                savedStyle,
                request.getCollaborationStyle()
        );
        String lengthLevel = overrideValue(
                request.getCollaborationStyle(),
                "lengthLevel",
                "medium"
        );

        String systemPrompt = buildRefineSystemPrompt(
                resolveTargetLanguage(request.getTargetLang()),
                effectiveStyle,
                lengthLevel,
                glossaries,
                rules
        );
        return new RefineContext(glossaries, rules, effectiveStyle, lengthLevel, systemPrompt);
    }

    private List<Long> safeIds(List<Long> ids) {
        return ids == null ? List.of() : ids;
    }

    private CollaborationStyleResponse mergeCollaborationStyle(
            CollaborationStyleResponse savedStyle,
            Map<String, Object> override
    ) {
        CollaborationStyleResponse baseStyle = savedStyle != null
                ? savedStyle
                : new CollaborationStyleResponse("polite", "balanced", "concise");

        return new CollaborationStyleResponse(
                overrideValue(override, "tone", baseStyle.tone()),
                overrideValue(override, "directness", baseStyle.directness()),
                overrideValue(override, "detailLevel", baseStyle.detailLevel())
        );
    }

    private String overrideValue(Map<String, Object> override, String key, String fallback) {
        if (override == null || !override.containsKey(key) || override.get(key) == null) {
            return fallback;
        }

        String value = String.valueOf(override.get(key)).trim();
        return value.isEmpty() ? fallback : value;
    }

    private String resolveTargetLanguage(String targetLang) {
        if ("zh".equalsIgnoreCase(targetLang)) {
            return "Chinese";
        }
        if ("ja".equalsIgnoreCase(targetLang)) {
            return "Japanese";
        }
        return "English";
    }

    private String buildRefineSystemPrompt(
            String targetLanguage,
            CollaborationStyleResponse style,
            String lengthLevel,
            List<GlossaryResponse> glossaries,
            List<RuleResponse> rules
    ) {
        return """
            You are 'Manyfast AI', an expert executive communication assistant for global business collaboration.
            Your goal is to refine raw, emotional, or vague business messages into clear, polite, and effective messages in %s without losing critical context.

            %s
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
               - Transform blameful, emotional, or demanding tone into respectful, professional business language.
               - Preserve the urgency and deadline clearly without sounding hostile.
               - Strictly follow the resolved collaboration style, selected glossary entries, and selected communication rules above.
            3. BACK-TRANSLATION:
               - Provide a faithful Korean translation of your refinedText so the user can verify the meaning.
            4. RISKY EXPRESSIONS & WARNINGS:
               - Identify specific phrases that could cause misunderstanding or offense, explaining why and how they were replaced.
               - If a deadline or detail is vague, provide a helpful suggestion in 'missingInfoWarnings'.
            5. GLOSSARY MATCHING:
               - ONLY include items in 'appliedGlossary' if they are listed in SELECTED GLOSSARY ENTRIES and the term appears in the original message.
               - If no selected glossary terms appear in the input, 'appliedGlossary' MUST be an empty array [].
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
            """.formatted(
                targetLanguage,
                buildStyleSection(style, lengthLevel),
                buildGlossarySection(glossaries),
                buildRuleSection(rules)
        );
    }

    private String buildStyleSection(CollaborationStyleResponse style, String lengthLevel) {
        String toneInstruction = switch (style.tone()) {
            case "friendly" -> "CASUAL & WARM: Use warm greetings, friendly phrasing, and a soft, cheerful tone. Avoid overly stiff corporate vocabulary.";
            case "professional" -> "FORMAL & EXECUTIVE: Use authoritative, precise executive business language. Do not use slang or emojis.";
            default -> "POLITE & COURTEOUS: Use respectful standard business language with courteous modal verbs.";
        };

        String directnessInstruction = switch (style.directness()) {
            case "direct" -> "DIRECT: State the action item and key ask in the first sentence.";
            case "indirect" -> "INDIRECT: Cushion the request with a polite preamble and considerate buffer phrases.";
            default -> "BALANCED: Use a natural blend of clarity and politeness.";
        };

        String detailInstruction = switch (style.detailLevel()) {
            case "concise" -> "CONCISE: Focus tightly on core facts and omit unnecessary filler.";
            case "detailed" -> "DETAILED: Provide background rationale, context, and clear next steps.";
            default -> "MODERATE: Include enough context to make the request actionable.";
        };

        String lengthInstruction = switch (lengthLevel) {
            case "short" -> "SHORT: Use one compact sentence or two very short lines.";
            case "long" -> "LONG: Elaborate thoroughly with complete context.";
            default -> "STANDARD: Use two or three natural sentences.";
        };

        return """
            [RESOLVED USER COLLABORATION STYLE]
            - Tone: %s
              Tone Instruction: %s
            - Directness: %s
              Directness Instruction: %s
            - Detail Level: %s
              Detail Instruction: %s
            - Length Level: %s
              Length Instruction: %s
            """.formatted(
                style.tone(),
                toneInstruction,
                style.directness(),
                directnessInstruction,
                style.detailLevel(),
                detailInstruction,
                lengthLevel,
                lengthInstruction
        );
    }

    private String buildGlossarySection(List<GlossaryResponse> glossaries) {
        if (glossaries.isEmpty()) {
            return "[SELECTED GLOSSARY ENTRIES]\n- None selected.";
        }

        StringBuilder section = new StringBuilder("[SELECTED GLOSSARY ENTRIES]\n");
        for (GlossaryResponse glossary : glossaries) {
            section.append("- Term: ").append(glossary.term()).append('\n')
                    .append("  Rule: ").append(glossary.rule()).append('\n')
                    .append("  Note: ").append(
                            glossary.note() == null || glossary.note().isBlank()
                                    ? "(none)"
                                    : glossary.note()
                    ).append('\n');
        }
        return section.toString().stripTrailing();
    }

    private String buildRuleSection(List<RuleResponse> rules) {
        if (rules.isEmpty()) {
            return "[SELECTED COMMUNICATION RULES]\n- None selected.";
        }

        StringBuilder section = new StringBuilder("[SELECTED COMMUNICATION RULES]\n");
        for (RuleResponse rule : rules) {
            section.append("- Name: ").append(rule.name()).append('\n')
                    .append("  Description: ").append(rule.description()).append('\n');
        }
        return section.toString().stripTrailing();
    }

    private List<Map<String, Object>> buildFallbackAppliedGlossary(
            String originalText,
            String refinedText,
            List<GlossaryResponse> glossaries
    ) {
        if (originalText == null || originalText.isBlank()) {
            return List.of();
        }

        String originalLower = originalText.toLowerCase(Locale.ROOT);
        String refinedLower = refinedText.toLowerCase(Locale.ROOT);
        return glossaries.stream()
                .filter(glossary -> originalLower.contains(glossary.term().toLowerCase(Locale.ROOT)))
                .map(glossary -> {
                    Map<String, Object> applied = new LinkedHashMap<>();
                    applied.put("term", glossary.term());
                    applied.put("rule", glossary.rule());
                    applied.put(
                            "matchedInRefined",
                            refinedLower.contains(glossary.term().toLowerCase(Locale.ROOT))
                    );
                    return applied;
                })
                .toList();
    }

    record RefineContext(
            List<GlossaryResponse> glossaries,
            List<RuleResponse> rules,
            CollaborationStyleResponse collaborationStyle,
            String lengthLevel,
            String systemPrompt
    ) {
    }
}
