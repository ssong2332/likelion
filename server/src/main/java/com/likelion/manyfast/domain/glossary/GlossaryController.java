package com.likelion.manyfast.domain.glossary;

import com.likelion.manyfast.domain.glossary.dto.GlossaryRequest;
import com.likelion.manyfast.domain.glossary.dto.GlossaryResponse;
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
@RequestMapping("/api/glossaries")
@RequiredArgsConstructor
public class GlossaryController {

    private final GlossaryService glossaryService;

    @GetMapping
    public ResponseEntity<Map<String, List<GlossaryResponse>>> getGlossaries() {
        return ResponseEntity.ok(Map.of("data", glossaryService.findAll()));
    }

    @PostMapping
    public ResponseEntity<Map<String, GlossaryResponse>> createGlossary(
            @Valid @RequestBody GlossaryRequest request
    ) {
        return ResponseEntity.status(201).body(Map.of("data", glossaryService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, GlossaryResponse>> updateGlossary(
            @PathVariable Long id,
            @Valid @RequestBody GlossaryRequest request
    ) {
        validatePositiveId(id);
        return ResponseEntity.ok(Map.of("data", glossaryService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGlossary(@PathVariable Long id) {
        validatePositiveId(id);
        glossaryService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Glossary entry deleted successfully"));
    }

    private void validatePositiveId(Long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
    }
}
