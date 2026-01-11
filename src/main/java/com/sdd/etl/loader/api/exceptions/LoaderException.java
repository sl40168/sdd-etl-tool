package com.sdd.etl.loader.api.exceptions;

/**
 * Base exception class for all loader-related errors.
 * All loader implementations should throw this or its subclasses.
 */
public class LoaderException extends Exception {

    public LoaderException(String message) {
        super(message);
    }

    public LoaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
