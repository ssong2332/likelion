package com.likelion.manyfast.domain.history;

import com.likelion.manyfast.domain.history.dto.MessageHistoryErrorResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = HistoryController.class)
public class HistoryExceptionHandler {

    @ExceptionHandler(MessageHistoryNotFoundException.class)
    public ResponseEntity<MessageHistoryErrorResponse> handleNotFound(
            MessageHistoryNotFoundException exception
    ) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<MessageHistoryErrorResponse> handleTypeMismatch() {
        return error(HttpStatus.BAD_REQUEST, "id must be a number");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageHistoryErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception
    ) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    private ResponseEntity<MessageHistoryErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new MessageHistoryErrorResponse(
                status.value(),
                status.name(),
                message
        ));
    }
}
