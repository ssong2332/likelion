package com.likelion.manyfast.domain.glossary;

import com.likelion.manyfast.domain.glossary.dto.GlossaryErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = GlossaryController.class)
public class GlossaryExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlossaryErrorResponse> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid glossary request");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GlossaryErrorResponse> handleUnreadableMessage() {
        return error(HttpStatus.BAD_REQUEST, "Invalid glossary request body");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GlossaryErrorResponse> handleTypeMismatch() {
        return error(HttpStatus.BAD_REQUEST, "id must be a number");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GlossaryErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception
    ) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(GlossaryNotFoundException.class)
    public ResponseEntity<GlossaryErrorResponse> handleNotFound(
            GlossaryNotFoundException exception
    ) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(DuplicateGlossaryTermException.class)
    public ResponseEntity<GlossaryErrorResponse> handleDuplicateTerm(
            DuplicateGlossaryTermException exception
    ) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GlossaryErrorResponse> handleDataIntegrityViolation() {
        return error(HttpStatus.CONFLICT, "Glossary term already exists");
    }

    private ResponseEntity<GlossaryErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new GlossaryErrorResponse(
                status.value(),
                status.name(),
                message
        ));
    }
}
