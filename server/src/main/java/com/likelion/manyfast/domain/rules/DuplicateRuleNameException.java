package com.likelion.manyfast.domain.rules;

public class DuplicateRuleNameException extends RuntimeException {

    public DuplicateRuleNameException(String name) {
        super("Rule name already exists: " + name);
    }

    public DuplicateRuleNameException(String name, Throwable cause) {
        super("Rule name already exists: " + name, cause);
    }
}
