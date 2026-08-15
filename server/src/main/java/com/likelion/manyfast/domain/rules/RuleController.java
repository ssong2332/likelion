package com.likelion.manyfast.domain.rules;

import com.likelion.manyfast.domain.rules.dto.RuleRequest;
import com.likelion.manyfast.domain.rules.dto.RuleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @GetMapping
    public ResponseEntity<Map<String, List<RuleResponse>>> getRules() {
        return ResponseEntity.ok(Map.of("data", ruleService.findAll()));
    }

    @PostMapping
    public ResponseEntity<Map<String, RuleResponse>> createRule(
            @Valid @RequestBody RuleRequest request
    ) {
        return ResponseEntity.status(201).body(Map.of("data", ruleService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, RuleResponse>> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody RuleRequest request
    ) {
        validatePositiveId(id);
        return ResponseEntity.ok(Map.of("data", ruleService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteRule(@PathVariable Long id) {
        validatePositiveId(id);
        ruleService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Rule deleted successfully"));
    }

    private void validatePositiveId(Long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
    }
}
