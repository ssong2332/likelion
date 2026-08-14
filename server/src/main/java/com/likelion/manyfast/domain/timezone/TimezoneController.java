package com.likelion.manyfast.domain.timezone;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/timezone")
@RequiredArgsConstructor
public class TimezoneController {

    private final TimezoneService timezoneService;

    @PostMapping("/convert")
    public ResponseEntity<Map<String, Object>> convertTimezone(@RequestBody Map<String, String> request) {
        String senderTz = request.get("senderTz");
        String receiverTz = request.get("receiverTz");
        return ResponseEntity.ok(timezoneService.calculateTimezone(senderTz, receiverTz));
    }
}
