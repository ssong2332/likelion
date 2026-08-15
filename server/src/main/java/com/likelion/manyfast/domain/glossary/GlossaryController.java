package com.likelion.manyfast.domain.glossary;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/glossaries")
public class GlossaryController {

    private final List<Map<String, Object>> glossaries = new ArrayList<>(List.of(
            Map.of("id", "1", "term", "Manyfast", "rule", "원문 유지 (Keep Original)", "note", "의역 금지"),
            Map.of("id", "2", "term", "ASAP", "rule", "오늘 EOD 18:00 전", "note", "팀 내 합의 기준")
    ));

    @GetMapping
    public ResponseEntity<Map<String, Object>> getGlossaries() {
        return ResponseEntity.ok(Map.of("data", glossaries));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createGlossary(@RequestBody Map<String, Object> request) {
        Map<String, Object> newEntry = new HashMap<>(request);
        newEntry.put("id", String.valueOf(System.currentTimeMillis()));
        glossaries.add(newEntry);
        return ResponseEntity.status(201).body(Map.of("data", newEntry));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGlossary(@PathVariable String id) {
        glossaries.removeIf(g -> Objects.equals(g.get("id"), id));
        return ResponseEntity.ok(Map.of("message", "Glossary entry deleted successfully"));
    }
}
