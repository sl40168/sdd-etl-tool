package com.sdd.etl.loader.api.exceptions;

/**
 * Thrown when the loader cannot establish a connection to the target system.
 */
public class ConnectionException extends LoaderException {

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
