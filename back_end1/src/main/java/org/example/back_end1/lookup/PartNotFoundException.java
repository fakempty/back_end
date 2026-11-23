package org.example.back_end1.lookup;

public class PartNotFoundException extends RuntimeException {
    public PartNotFoundException(String message) {
        super(message);
    }
}