package com.likelion.manyfast.domain.userstyle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CollaborationStyleRequest(
        @NotBlank(message = "tone is required")
        @Size(max = 50, message = "tone must be at most 50 characters")
        @Pattern(
                regexp = "polite|concise|friendly",
                message = "tone must be one of polite, concise, friendly"
        )
        String tone,

        @NotBlank(message = "directness is required")
        @Size(max = 50, message = "directness must be at most 50 characters")
        @Pattern(
                regexp = "balanced|direct",
                message = "directness must be one of balanced, direct"
        )
        String directness,

        @NotBlank(message = "detailLevel is required")
        @Size(max = 50, message = "detailLevel must be at most 50 characters")
        String detailLevel
) {
}
