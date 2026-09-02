package com.fixit.exception;

/**
 * Unauthorized Exception
 * 
 * Thrown when user is not authenticated (401)
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
