package com.likelion.manyfast.domain.timezone;

import com.likelion.manyfast.domain.timezone.dto.TimezoneErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = TimezoneController.class)
public class TimezoneExceptionHandler {

    @ExceptionHandler(InvalidTimezoneException.class)
    public ResponseEntity<TimezoneErrorResponse> handleInvalidTimezone(
            InvalidTimezoneException exception
    ) {
        return badRequest(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<TimezoneErrorResponse> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid timezone request");
        return badRequest(message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<TimezoneErrorResponse> handleUnreadableMessage() {
        return badRequest(
                "Invalid dateTime format. Use ISO-8601 UTC format such as 2026-07-15T12:00:00Z."
        );
    }

    private ResponseEntity<TimezoneErrorResponse> badRequest(String message) {
        return ResponseEntity.badRequest().body(new TimezoneErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                message
        ));
    }
}
