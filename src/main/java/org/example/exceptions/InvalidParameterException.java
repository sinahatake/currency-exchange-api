package org.example.exceptions;

public class InvalidParameterException extends ApplicationException {
    public InvalidParameterException(String message) {
        super(message);
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}