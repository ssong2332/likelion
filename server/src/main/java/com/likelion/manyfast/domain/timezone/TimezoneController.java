package com.likelion.manyfast.domain.timezone;

import com.likelion.manyfast.domain.timezone.dto.OffHoursCheckRequest;
import com.likelion.manyfast.domain.timezone.dto.OffHoursCheckResponse;
import com.likelion.manyfast.domain.timezone.dto.TimezoneConvertRequest;
import com.likelion.manyfast.domain.timezone.dto.TimezoneConvertResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/timezone")
@RequiredArgsConstructor
public class TimezoneController {

    private final TimezoneService timezoneService;

    @PostMapping("/convert")
    public ResponseEntity<TimezoneConvertResponse> convertTimezone(
            @Valid @RequestBody TimezoneConvertRequest request
    ) {
        return ResponseEntity.ok(timezoneService.convert(
                request.dateTime(),
                request.senderTimezone(),
                request.receiverTimezone()
        ));
    }

    @PostMapping("/check-offhours")
    public ResponseEntity<OffHoursCheckResponse> checkOffHours(
            @Valid @RequestBody OffHoursCheckRequest request
    ) {
        return ResponseEntity.ok(timezoneService.checkOffHours(
                request.dateTime(),
                request.receiverTimezone()
        ));
    }
}
