package com.likelion.manyfast.domain.timezone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record TimezoneConvertRequest(
        @NotNull(message = "dateTime is required")
        Instant dateTime,

        @NotBlank(message = "senderTimezone is required")
        String senderTimezone,

        @NotBlank(message = "receiverTimezone is required")
        String receiverTimezone
) {
}
