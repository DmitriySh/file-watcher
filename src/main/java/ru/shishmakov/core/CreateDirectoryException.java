package ru.shishmakov.core;

/**
 * @author Dmitriy Shishmakov
 */
public class CreateDirectoryException extends RuntimeException {

    public CreateDirectoryException() {
    }

    public CreateDirectoryException(String message) {
        super(message);
    }

    public CreateDirectoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
