package com.likelion.manyfast.domain.glossary;

public class GlossaryNotFoundException extends RuntimeException {

    public GlossaryNotFoundException(Long id) {
        super("Glossary not found: " + id);
    }
}
