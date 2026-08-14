package com.likelion.manyfast.domain.userstyle;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/collaboration-style")
public class UserStyleController {

    private Map<String, Object> style = new HashMap<>(Map.of(
            "tone", "polite",
            "directness", "balanced",
            "detailLevel", "concise"
    ));

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserStyle() {
        return ResponseEntity.ok(Map.of("data", style));
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateUserStyle(@RequestBody Map<String, Object> request) {
        style.putAll(request);
        return ResponseEntity.ok(Map.of("data", style));
    }
}
