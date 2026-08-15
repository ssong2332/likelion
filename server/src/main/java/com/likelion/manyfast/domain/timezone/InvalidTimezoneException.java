package com.likelion.manyfast.domain.timezone;

public class InvalidTimezoneException extends RuntimeException {

    public InvalidTimezoneException(String timezone, Throwable cause) {
        super("Invalid IANA timezone: " + timezone, cause);
    }
}
