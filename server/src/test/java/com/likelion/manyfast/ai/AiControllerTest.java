package com.likelion.manyfast.ai;

import com.likelion.manyfast.ai.controller.AiController;
import com.likelion.manyfast.ai.dto.RefineRequestDto;
import com.likelion.manyfast.ai.dto.RefineResponseDto;
import com.likelion.manyfast.ai.dto.ReplyDraftRequestDto;
import com.likelion.manyfast.ai.dto.ReplyDraftResponseDto;
import com.likelion.manyfast.ai.service.AiService;
import com.likelion.manyfast.domain.timezone.TimezoneService;
import com.likelion.manyfast.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
@Import(GlobalExceptionHandler.class)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiService aiService;

    @MockBean
    private TimezoneService timezoneService;

    @Test
    @DisplayName("F-2/F-3: 유효한 원문 입력 시 200 OK 및 교정 결과 반환")
    void analyzeAndRefine_success() throws Exception {
        RefineResponseDto mockResponse = RefineResponseDto.builder()
                .refinedText("Could you please review PR #142 by EOD?")
                .backTranslation("PR #142를 오늘 EOD까지 검토해 주시겠어요?")
                .extractedInfo(Map.of(
                        "purpose", "PR 코드 리뷰",
                        "assignee", "수신자",
                        "deadline", "오늘 EOD",
                        "urgency", "critical",
                        "businessImpact", "배포 일정 지연"
                ))
                .build();

        given(aiService.refineMessage(any(RefineRequestDto.class))).willReturn(mockResponse);
        given(timezoneService.calculateTimezone(any(), any())).willReturn(Map.of("isReceiverOffHours", true));

        mockMvc.perform(post("/api/ai/analyze-refine")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originalText": "이거 리뷰 3일째 안 주셔서 배포 못 나갑니다",
                                  "targetLang": "en"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refinedText").value("Could you please review PR #142 by EOD?"))
                .andExpect(jsonPath("$.extractedInfo.urgency").value("critical"))
                .andExpect(jsonPath("$.timezoneInfo.isReceiverOffHours").value(true));
    }

    @Test
    @DisplayName("F-2/F-3: 원문이 비어있을 경우 400 Bad Request 에러 반환")
    void analyzeAndRefine_blankText_returns400() throws Exception {
        mockMvc.perform(post("/api/ai/analyze-refine")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originalText": "   ",
                                  "targetLang": "en"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("교정할 원문 메시지를 입력해 주세요."));
    }

    @Test
    @DisplayName("F-8: 수신 메시지 입력 시 3가지 회신 초안 반환")
    void generateReplyDraft_success() throws Exception {
        ReplyDraftResponseDto mockResponse = ReplyDraftResponseDto.builder()
                .analyzedRequest(Map.of("summary", "피드백 논의 요청", "urgency", "normal"))
                .suggestedReplies(List.of(
                        Map.of("direction", "accept", "title", "즉시 수락", "draftText", "I would be glad to sync."),
                        Map.of("direction", "request_details", "title", "서면 요청", "draftText", "Please leave notes."),
                        Map.of("direction", "schedule", "title", "일정 조율", "draftText", "I will follow up.")
                ))
                .build();

        given(aiService.generateReplyDrafts(any(ReplyDraftRequestDto.class))).willReturn(mockResponse);

        mockMvc.perform(post("/api/ai/reply-draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receivedMessage": "Could you check my architecture draft?"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestedReplies.length()").value(3))
                .andExpect(jsonPath("$.suggestedReplies[0].direction").value("accept"));
    }

    @Test
    @DisplayName("F-8: 수신 메시지가 비어있을 경우 400 Bad Request 반환")
    void generateReplyDraft_blankMessage_returns400() throws Exception {
        mockMvc.perform(post("/api/ai/reply-draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receivedMessage": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("답장을 생성할 수신 메시지를 입력해 주세요."));
    }
}
