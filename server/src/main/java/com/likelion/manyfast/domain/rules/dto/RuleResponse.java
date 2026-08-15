package com.likelion.manyfast.domain.rules.dto;

import com.likelion.manyfast.domain.rules.Rule;

public record RuleResponse(
        Long id,
        String name,
        String description
) {

    public static RuleResponse from(Rule rule) {
        return new RuleResponse(
                rule.getId(),
                rule.getName(),
                rule.getDescription()
        );
    }
}
