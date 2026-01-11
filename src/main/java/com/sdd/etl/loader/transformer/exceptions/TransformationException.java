package com.sdd.etl.loader.transformer.exceptions;

import com.sdd.etl.ETLException;

/**
 * Custom exception thrown when data transformation fails.
 * <p>
 * This exception contains context about the transformation failure including:
 * <ul>
 *   <li>Source data type that failed</li>
 *   <li>Number of records processed before failure</li>
 *   <li>Detailed error message</li>
 *   <li>Optional root cause exception</li>
 * </ul>
 * </p>
 * 
 * @since 1.0.0
 */
public class TransformationException extends ETLException {

    private static final long serialVersionUID = 1L;
    
    private final String sourceDataType;
    private final int recordCount;

    /**
     * Constructs a new TransformationException.
     *
     * @param sourceDataType Name of source data type that failed
     * @param recordCount Number of records processed before failure
     * @param message Detailed error message
     */
    public TransformationException(String sourceDataType, int recordCount, String message) {
        super("TRANSFORM", null, message);
        this.sourceDataType = sourceDataType;
        this.recordCount = recordCount;
    }

    /**
     * Constructs a new TransformationException with cause.
     *
     * @param sourceDataType Name of source data type that failed
     * @param recordCount Number of records processed before failure
     * @param message Detailed error message
     * @param cause Root cause exception
     */
    public TransformationException(String sourceDataType, int recordCount,
                                   String message, Throwable cause) {
        super("TRANSFORM", null, message, cause);
        this.sourceDataType = sourceDataType;
        this.recordCount = recordCount;
    }

    /**
     * Gets the source data type that failed during transformation.
     *
     * @return Source data type name (may be null)
     */
    public String getSourceDataType() {
        return sourceDataType;
    }

    /**
     * Gets the number of records processed before the failure occurred.
     *
     * @return Record count at time of failure
     */
    public int getRecordCount() {
        return recordCount;
    }

    @Override
    public String toString() {
        return "TransformationException{" +
                "sourceDataType='" + sourceDataType + '\'' +
                ", recordCount=" + recordCount +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
