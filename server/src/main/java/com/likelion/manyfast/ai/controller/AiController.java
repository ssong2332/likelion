package com.likelion.manyfast.ai.controller;

import com.likelion.manyfast.ai.dto.RefineRequestDto;
import com.likelion.manyfast.ai.dto.RefineResponseDto;
import com.likelion.manyfast.ai.dto.ReplyDraftRequestDto;
import com.likelion.manyfast.ai.dto.ReplyDraftResponseDto;
import com.likelion.manyfast.ai.service.AiService;
import com.likelion.manyfast.domain.timezone.TimezoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final TimezoneService timezoneService;

    @PostMapping("/analyze-refine")
    public ResponseEntity<RefineResponseDto> analyzeAndRefine(@RequestBody RefineRequestDto request) {
        if (request.getOriginalText() == null || request.getOriginalText().isBlank()) {
            throw new IllegalArgumentException("교정할 원문 메시지를 입력해 주세요.");
        }
        RefineResponseDto response = aiService.refineMessage(request);
        response.setTimezoneInfo(timezoneService.calculateTimezone(request.getSenderTimezone(), request.getReceiverTimezone()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reply-draft")
    public ResponseEntity<ReplyDraftResponseDto> generateReplyDraft(@RequestBody ReplyDraftRequestDto request) {
        if (request.getReceivedMessage() == null || request.getReceivedMessage().isBlank()) {
            throw new IllegalArgumentException("답장을 생성할 수신 메시지를 입력해 주세요.");
        }
        ReplyDraftResponseDto response = aiService.generateReplyDrafts(request);
        return ResponseEntity.ok(response);
    }
}
