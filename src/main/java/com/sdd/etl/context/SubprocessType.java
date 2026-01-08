package com.sdd.etl.context;

/**
 * Enumeration of ETL subprocess types.
 * Represents the five main subprocesses in the ETL pipeline.
 */
public enum SubprocessType {

    /**
     * Extract subprocess - retrieves data from sources
     */
    EXTRACT("EXTRACT"),

    /**
     * Transform subprocess - transforms data according to rules
     */
    TRANSFORM("TRANSFORM"),

    /**
     * Load subprocess - loads data to targets
     */
    LOAD("LOAD"),

    /**
     * Validate subprocess - validates loaded data
     */
    VALIDATE("VALIDATE"),

    /**
     * Clean subprocess - performs cleanup operations
     */
    CLEAN("CLEAN");

    private final String value;

    /**
     * Constructs a SubprocessType with the specified value.
     *
     * @param value the string representation of the subprocess type
     */
    SubprocessType(String value) {
        this.value = value;
    }

    /**
     * Gets the string value of this subprocess type.
     *
     * @return the string value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the string value of this subprocess type.
     *
     * @return the string value
     */
    @Override
    public String toString() {
        return value;
    }
}
