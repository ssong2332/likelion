package com.likelion.manyfast.domain.history;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/messages")
public class HistoryController {

    private final List<Map<String, Object>> messageHistory = new ArrayList<>();

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getHistory() {
        return ResponseEntity.ok(Map.of(
                "totalCount", messageHistory.size(),
                "history", messageHistory
        ));
    }

    @DeleteMapping("/all")
    public ResponseEntity<Map<String, String>> deleteAll() {
        messageHistory.clear();
        return ResponseEntity.ok(Map.of("message", "All message history permanently deleted"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteItem(@PathVariable String id) {
        messageHistory.removeIf(m -> Objects.equals(m.get("id"), id));
        return ResponseEntity.ok(Map.of("message", "History item permanently deleted"));
    }
}
