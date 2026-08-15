package com.likelion.manyfast.domain.timezone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record OffHoursCheckRequest(
        @NotNull(message = "dateTime is required")
        Instant dateTime,

        @NotBlank(message = "receiverTimezone is required")
        String receiverTimezone
) {
}
