package com.likelion.manyfast.domain.timezone.dto;

import java.time.Instant;
import java.time.OffsetDateTime;

public record TimezoneConvertResponse(
        Instant dateTime,
        String senderTimezone,
        OffsetDateTime senderLocalTime,
        String receiverTimezone,
        OffsetDateTime receiverLocalTime
) {
}
