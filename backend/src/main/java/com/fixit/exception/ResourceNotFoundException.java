package com.fixit.exception;

/**
 * Resource Not Found Exception
 * 
 * Thrown when a requested resource is not found (404)
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
