package com.likelion.manyfast.domain.rules;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    private final List<Map<String, Object>> rules = new ArrayList<>(List.of(
            Map.of("id", "1", "name", "보고서 마감", "description", "매주 목요일 17:00 KST까지 초안 공유")
    ));

    @GetMapping
    public ResponseEntity<Map<String, Object>> getRules() {
        return ResponseEntity.ok(Map.of("data", rules));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createRule(@RequestBody Map<String, Object> request) {
        Map<String, Object> newRule = new HashMap<>(request);
        newRule.put("id", String.valueOf(System.currentTimeMillis()));
        rules.add(newRule);
        return ResponseEntity.status(201).ok(Map.of("data", newRule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteRule(@PathVariable String id) {
        rules.removeIf(r -> Objects.equals(r.get("id"), id));
        return ResponseEntity.ok(Map.of("message", "Rule deleted successfully"));
    }
}
