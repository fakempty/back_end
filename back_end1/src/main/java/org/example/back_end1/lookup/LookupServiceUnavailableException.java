package org.example.back_end1.lookup;

public class LookupServiceUnavailableException extends RuntimeException {
    public LookupServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}