package com.likelion.manyfast.ai.controller;

import com.likelion.manyfast.domain.glossary.GlossaryNotFoundException;
import com.likelion.manyfast.domain.rules.RuleNotFoundException;
import com.likelion.manyfast.global.exception.ErrorResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = AiController.class)
public class AiExceptionHandler {

    @ExceptionHandler(GlossaryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGlossaryNotFound(
            GlossaryNotFoundException exception
    ) {
        return notFound(exception.getMessage());
    }

    @ExceptionHandler(RuleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRuleNotFound(
            RuleNotFoundException exception
    ) {
        return notFound(exception.getMessage());
    }

    private ResponseEntity<ErrorResponse> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.name(),
                message
        ));
    }
}
