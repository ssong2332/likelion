package com.likelion.manyfast.ai;

import com.likelion.manyfast.ai.dto.RefineRequestDto;
import com.likelion.manyfast.ai.dto.RefineResponseDto;
import com.likelion.manyfast.ai.dto.ReplyDraftRequestDto;
import com.likelion.manyfast.ai.dto.ReplyDraftResponseDto;
import com.likelion.manyfast.ai.service.AiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AiServiceTest {

    private AiService aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiService();
    }

    @Test
    @DisplayName("F-2/F-3: 교정 메시지 생성 시 비즈니스 5대 핵심 정보가 모두 누락 없이 포함되어야 한다")
    void refineMessage_containsAll5CoreFacts() {
        RefineRequestDto request = RefineRequestDto.builder()
                .originalText("이거 리뷰 3일째 안 주셔서 배포 못 나갑니다")
                .targetLang("en")
                .collaborationStyle(Map.of("tone", "polite", "directness", "balanced"))
                .appliedGlossaryIds(List.of("Manyfast", "ASAP"))
                .build();

        RefineResponseDto response = aiService.refineMessage(request);

        assertThat(response).isNotNull();
        assertThat(response.getRefinedText()).isNotBlank();
        assertThat(response.getBackTranslation()).isNotBlank();

        Map<String, Object> extractedInfo = response.getExtractedInfo();
        assertThat(extractedInfo).containsKey("purpose");
        assertThat(extractedInfo).containsKey("assignee");
        assertThat(extractedInfo).containsKey("deadline");
        assertThat(extractedInfo).containsKey("urgency");
        assertThat(extractedInfo).containsKey("businessImpact");
    }

    @Test
    @DisplayName("F-8: 회신 초안 생성 시 정확히 3가지 방향(accept, request_details, schedule)이 생성되어야 한다")
    void generateReplyDrafts_produces3DistinctDirections() {
        ReplyDraftRequestDto request = ReplyDraftRequestDto.builder()
                .receivedMessage("Could you please review the architecture document?")
                .build();

        ReplyDraftResponseDto response = aiService.generateReplyDrafts(request);

        assertThat(response).isNotNull();
        assertThat(response.getAnalyzedRequest()).containsKey("summary");
        assertThat(response.getSuggestedReplies()).hasSize(3);

        List<String> directions = response.getSuggestedReplies().stream()
                .map(reply -> String.valueOf(reply.get("direction")))
                .toList();

        assertThat(directions).containsExactlyInAnyOrder("accept", "request_details", "schedule");
    }
}
