package com.likelion.manyfast.domain.rules.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RuleRequest(
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be at most 100 characters")
        String name,

        @NotBlank(message = "description is required")
        @Size(max = 500, message = "description must be at most 500 characters")
        String description
) {
}
