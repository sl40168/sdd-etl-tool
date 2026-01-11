package com.sdd.etl.loader.api.exceptions;

/**
 * Thrown when data loading fails due to data format issues or target system rejection.
 */
public class DataLoadingException extends LoaderException {

    public DataLoadingException(String message) {
        super(message);
    }

    public DataLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
