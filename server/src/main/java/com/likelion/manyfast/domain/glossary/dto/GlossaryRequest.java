package com.likelion.manyfast.domain.glossary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GlossaryRequest(
        @NotBlank(message = "term is required")
        @Size(max = 100, message = "term must be at most 100 characters")
        String term,

        @NotBlank(message = "rule is required")
        @Size(max = 255, message = "rule must be at most 255 characters")
        String rule,

        @Size(max = 255, message = "note must be at most 255 characters")
        String note
) {
}
