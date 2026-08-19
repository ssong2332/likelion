package com.likelion.manyfast.ai.service;

import com.likelion.manyfast.ai.dto.RefineRequestDto;
import com.likelion.manyfast.ai.dto.RefineResponseDto;
import com.likelion.manyfast.domain.history.MessageHistoryService;
import com.likelion.manyfast.domain.timezone.TimezoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyzeRefineServiceTest {

    @Mock
    private AiService aiService;

    @Mock
    private TimezoneService timezoneService;

    @Mock
    private MessageHistoryService messageHistoryService;

    private AnalyzeRefineService analyzeRefineService;

    @BeforeEach
    void setUp() {
        analyzeRefineService = new AnalyzeRefineService(
                aiService,
                timezoneService,
                messageHistoryService
        );
    }

    @Test
    void savesExactlyOneHistoryAfterFinalResponseIsCreated() {
        RefineRequestDto request = request("오늘 안에 검토 부탁드립니다.");
        RefineResponseDto response = response("Could you please review this by today?");
        Map<String, Object> timezoneInfo = Map.of("isReceiverOffHours", false);
        when(aiService.refineMessage(request)).thenReturn(response);
        when(timezoneService.calculateTimezone("Asia/Seoul", "America/New_York"))
                .thenReturn(timezoneInfo);

        RefineResponseDto result = analyzeRefineService.analyzeAndRefine(request);

        assertThat(result).isSameAs(response);
        assertThat(result.getTimezoneInfo()).isEqualTo(timezoneInfo);
        InOrder inOrder = inOrder(aiService, timezoneService, messageHistoryService);
        inOrder.verify(aiService).refineMessage(request);
        inOrder.verify(timezoneService).calculateTimezone("Asia/Seoul", "America/New_York");
        inOrder.verify(messageHistoryService).save(
                "오늘 안에 검토 부탁드립니다.",
                "Could you please review this by today?"
        );
        verify(messageHistoryService).save(
                "오늘 안에 검토 부탁드립니다.",
                "Could you please review this by today?"
        );
    }

    @Test
    void doesNotSaveWhenAiProcessingFails() {
        RefineRequestDto request = request("실패 요청");
        when(aiService.refineMessage(request)).thenThrow(new IllegalStateException("AI failed"));

        assertThatThrownBy(() -> analyzeRefineService.analyzeAndRefine(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("AI failed");

        verifyNoInteractions(timezoneService, messageHistoryService);
    }

    @Test
    void doesNotSaveWhenFinalResponseAssemblyFails() {
        RefineRequestDto request = request("잘못된 시간대 요청");
        RefineResponseDto response = response("Refined");
        when(aiService.refineMessage(request)).thenReturn(response);
        when(timezoneService.calculateTimezone("Asia/Seoul", "America/New_York"))
                .thenThrow(new IllegalArgumentException("timezone failed"));

        assertThatThrownBy(() -> analyzeRefineService.analyzeAndRefine(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("timezone failed");

        verify(messageHistoryService, never()).save("잘못된 시간대 요청", "Refined");
    }

    @Test
    void returnsAiResultWhenHistorySaveFails() {
        RefineRequestDto request = request("저장 실패와 무관하게 반환할 요청");
        RefineResponseDto response = response("Result must still be returned");
        when(aiService.refineMessage(request)).thenReturn(response);
        when(timezoneService.calculateTimezone("Asia/Seoul", "America/New_York"))
                .thenReturn(Map.of("isReceiverOffHours", false));
        when(messageHistoryService.save(
                "저장 실패와 무관하게 반환할 요청",
                "Result must still be returned"
        )).thenThrow(new IllegalStateException("history unavailable"));

        RefineResponseDto result = analyzeRefineService.analyzeAndRefine(request);

        assertThat(result).isSameAs(response);
        verify(messageHistoryService).save(
                "저장 실패와 무관하게 반환할 요청",
                "Result must still be returned"
        );
    }

    private RefineRequestDto request(String originalText) {
        return RefineRequestDto.builder()
                .originalText(originalText)
                .senderTimezone("Asia/Seoul")
                .receiverTimezone("America/New_York")
                .build();
    }

    private RefineResponseDto response(String refinedText) {
        return RefineResponseDto.builder()
                .refinedText(refinedText)
                .build();
    }
}
