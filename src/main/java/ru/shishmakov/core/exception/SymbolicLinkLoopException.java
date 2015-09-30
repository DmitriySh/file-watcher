package ru.shishmakov.core.exception;

/**
 * @author Dmitriy Shishmakov
 */
public class SymbolicLinkLoopException extends RuntimeException {

    public SymbolicLinkLoopException() {
    }

    public SymbolicLinkLoopException(String message) {
        super(message);
    }

    public SymbolicLinkLoopException(String message, Throwable cause) {
        super(message, cause);
    }
}
