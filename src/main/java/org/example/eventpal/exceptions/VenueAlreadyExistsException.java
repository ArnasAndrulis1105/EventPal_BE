package org.example.eventpal.exceptions;

public class VenueAlreadyExistsException extends RuntimeException {
    public VenueAlreadyExistsException(String message) {
        super(message);
    }
}
