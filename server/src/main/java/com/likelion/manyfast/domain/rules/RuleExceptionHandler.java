package com.likelion.manyfast.domain.rules;

import com.likelion.manyfast.domain.rules.dto.RuleErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = RuleController.class)
public class RuleExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RuleErrorResponse> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid rule request");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RuleErrorResponse> handleUnreadableMessage() {
        return error(HttpStatus.BAD_REQUEST, "Invalid rule request body");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RuleErrorResponse> handleTypeMismatch() {
        return error(HttpStatus.BAD_REQUEST, "id must be a number");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RuleErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception
    ) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(RuleNotFoundException.class)
    public ResponseEntity<RuleErrorResponse> handleNotFound(
            RuleNotFoundException exception
    ) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(DuplicateRuleNameException.class)
    public ResponseEntity<RuleErrorResponse> handleDuplicateName(
            DuplicateRuleNameException exception
    ) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RuleErrorResponse> handleDataIntegrityViolation() {
        return error(HttpStatus.CONFLICT, "Rule name already exists");
    }

    private ResponseEntity<RuleErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new RuleErrorResponse(
                status.value(),
                status.name(),
                message
        ));
    }
}
