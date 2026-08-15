package com.likelion.manyfast.domain.glossary;

public class DuplicateGlossaryTermException extends RuntimeException {

    public DuplicateGlossaryTermException(String term) {
        super("Glossary term already exists: " + term);
    }

    public DuplicateGlossaryTermException(String term, Throwable cause) {
        super("Glossary term already exists: " + term, cause);
    }
}
