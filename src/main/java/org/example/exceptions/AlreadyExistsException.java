package org.example.exceptions;

public class AlreadyExistsException extends ApplicationException {
    public AlreadyExistsException(String message) {
        super(message);
    }

    @Override
    public int getStatusCode() {
        return 409;
    }
}
