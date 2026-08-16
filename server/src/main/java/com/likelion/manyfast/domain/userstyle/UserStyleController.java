package com.likelion.manyfast.domain.userstyle;

import com.likelion.manyfast.domain.userstyle.dto.CollaborationStyleRequest;
import com.likelion.manyfast.domain.userstyle.dto.CollaborationStyleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user/collaboration-style")
@RequiredArgsConstructor
public class UserStyleController {

    private final CollaborationStyleService collaborationStyleService;

    @GetMapping
    public ResponseEntity<Map<String, CollaborationStyleResponse>> getUserStyle() {
        return ResponseEntity.ok(Map.of("data", collaborationStyleService.get()));
    }

    @PutMapping
    public ResponseEntity<Map<String, CollaborationStyleResponse>> updateUserStyle(
            @Valid @RequestBody CollaborationStyleRequest request
    ) {
        return ResponseEntity.ok(Map.of("data", collaborationStyleService.update(request)));
    }
}
