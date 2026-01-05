package com.synapsim.exception;

/**
 * Exception thrown when no relevant research is found for a simulation
 */
public class NoResearchFoundException extends RuntimeException {

    public NoResearchFoundException(String message) {
        super(message);
    }

    public NoResearchFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
