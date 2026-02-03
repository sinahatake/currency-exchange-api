package org.example.exceptions;

public class DatabaseException extends ApplicationException {
    public DatabaseException(String message) {
        super(message);
    }

    @Override
    public int getStatusCode() {
        return 500;
    }
}