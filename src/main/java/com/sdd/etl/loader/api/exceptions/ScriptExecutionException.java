package com.sdd.etl.loader.api.exceptions;

/**
 * Thrown when script execution fails on the target system.
 */
public class ScriptExecutionException extends LoaderException {

    public ScriptExecutionException(String message) {
        super(message);
    }

    public ScriptExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
