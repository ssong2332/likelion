package com.likelion.manyfast.domain.glossary.dto;

import com.likelion.manyfast.domain.glossary.Glossary;

public record GlossaryResponse(
        Long id,
        String term,
        String rule,
        String note
) {

    public static GlossaryResponse from(Glossary glossary) {
        return new GlossaryResponse(
                glossary.getId(),
                glossary.getTerm(),
                glossary.getRule(),
                glossary.getNote()
        );
    }
}
