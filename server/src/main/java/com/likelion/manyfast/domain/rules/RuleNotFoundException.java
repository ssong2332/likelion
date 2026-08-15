package com.likelion.manyfast.domain.rules;

public class RuleNotFoundException extends RuntimeException {

    public RuleNotFoundException(Long id) {
        super("Rule not found: " + id);
    }
}
