package com.likelion.manyfast.domain.timezone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record OffHoursCheckResponse(
        String receiverTimezone,
        OffsetDateTime receiverLocalTime,
        @JsonProperty("isReceiverOffHours") boolean isReceiverOffHours,
        OffsetDateTime nextAvailableCheckingTime
) {
}
