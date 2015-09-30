package ru.shishmakov.core.exception;

/**
 * @author Dmitriy Shishmakov
 */
public class DirectoryException extends RuntimeException {

    public DirectoryException() {
    }

    public DirectoryException(String message) {
        super(message);
    }

    public DirectoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
