package com.likelion.manyfast.domain.history;

import com.likelion.manyfast.domain.history.dto.MessageHistoryListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class HistoryController {

    private final MessageHistoryService messageHistoryService;

    @GetMapping("/history")
    public ResponseEntity<MessageHistoryListResponse> getHistory() {
        return ResponseEntity.ok(MessageHistoryListResponse.from(messageHistoryService.findHistory()));
    }

    @DeleteMapping("/all")
    public ResponseEntity<Map<String, String>> deleteAll() {
        messageHistoryService.deleteAll();
        return ResponseEntity.ok(Map.of("message", "All message history permanently deleted"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteItem(@PathVariable Long id) {
        validatePositiveId(id);
        messageHistoryService.delete(id);
        return ResponseEntity.ok(Map.of("message", "History item permanently deleted"));
    }

    private void validatePositiveId(Long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
    }
}
