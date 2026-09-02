package com.fixit.exception;

/**
 * Forbidden Exception
 * 
 * Thrown when user doesn't have permission to access resource (403)
 */
public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
