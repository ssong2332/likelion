package com.likelion.manyfast.ai.service;

import com.likelion.manyfast.ai.dto.RefineRequestDto;
import com.likelion.manyfast.ai.dto.RefineResponseDto;
import com.likelion.manyfast.domain.history.MessageHistoryService;
import com.likelion.manyfast.domain.timezone.TimezoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzeRefineService {

    private final AiService aiService;
    private final TimezoneService timezoneService;
    private final MessageHistoryService messageHistoryService;

    public RefineResponseDto analyzeAndRefine(RefineRequestDto request) {
        RefineResponseDto response = aiService.refineMessage(request);
        response.setTimezoneInfo(timezoneService.calculateTimezone(
                request.getSenderTimezone(),
                request.getReceiverTimezone()
        ));

        saveHistoryBestEffort(request.getOriginalText(), response.getRefinedText());
        return response;
    }

    private void saveHistoryBestEffort(String originalText, String refinedText) {
        try {
            messageHistoryService.save(originalText, refinedText);
        } catch (RuntimeException exception) {
            log.error("[HistorySaveFailed] AI refinement succeeded, but its history could not be saved", exception);
        }
    }
}
