package com.likelion.manyfast.domain.history;

public class MessageHistoryNotFoundException extends RuntimeException {

    public MessageHistoryNotFoundException(Long id) {
        super("Message history not found: " + id);
    }
}
