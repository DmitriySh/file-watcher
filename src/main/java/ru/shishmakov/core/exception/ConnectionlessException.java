package ru.shishmakov.core.exception;

/**
 * @author Dmitriy Shishmakov
 */
public class ConnectionlessException extends RuntimeException {

    public ConnectionlessException() {
        super();
    }

    public ConnectionlessException(String message) {
        super(message);
    }

    public ConnectionlessException(String message, Throwable cause) {
        super(message, cause);
    }
}
