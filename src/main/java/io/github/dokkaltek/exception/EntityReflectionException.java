package io.github.dokkaltek.exception;

import lombok.Getter;

/**
 * Entity reflection exception class.
 */
@Getter
public class EntityReflectionException extends RuntimeException {
    private static final String DEFAULT_ERROR = "Entity reflection error";

    /**
     * The error summary or error code.
     */
    private final String error;

    /**
     * The detailed error message.
     */
    private final String message;

    /**
     * The HTTP status code of the error.
     */
    private final int status;

    /**
     * Default {@link EntityReflectionException} constructor.
     * @param ex The underlying exception thrown.
     */
    public EntityReflectionException(Throwable ex) {
        super(ex);
        this.error = DEFAULT_ERROR;
        this.message = ex.getMessage();
        this.status = 500;
    }

    /**
     * Default {@link EntityReflectionException} constructor.
     * @param message The message to show.
     */
    public EntityReflectionException(String message) {
        super(message);
        this.error = DEFAULT_ERROR;
        this.message = message;
        this.status = 500;
    }
}
