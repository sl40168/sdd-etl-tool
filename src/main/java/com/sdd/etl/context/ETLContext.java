package com.sdd.etl.context;

import com.sdd.etl.config.ETConfiguration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime context containing state for a single day's ETL process.
 * All data transfer between subprocesses occurs through this context.
 */
public class ETLContext {

    private Map<String, Object> data;

    /**
     * Constructs a new ETLContext.
     */
    public ETLContext() {
        this.data = new HashMap<>();
    }

    /**
     * Gets current processing date.
     *
     * @return date as LocalDate object
     */
    public LocalDate getCurrentDate() {
        return (LocalDate) data.get(ContextConstants.CURRENT_DATE);
    }

    /**
     * Sets current processing date.
     *
     * @param date date as LocalDate object
     */
    public void setCurrentDate(LocalDate date) {
        data.put(ContextConstants.CURRENT_DATE, date);
    }

    /**
     * Gets currently executing subprocess.
     *
     * @return subprocess type
     */
    public SubprocessType getCurrentSubprocess() {
        return (SubprocessType) data.get(ContextConstants.CURRENT_SUBPROCESS);
    }

    /**
     * Sets currently executing subprocess.
     *
     * @param subprocess subprocess type
     */
    public void setCurrentSubprocess(SubprocessType subprocess) {
        data.put(ContextConstants.CURRENT_SUBPROCESS, subprocess);
    }

    /**
     * Gets ETL configuration.
     *
     * @return ETConfiguration object
     */
    public ETConfiguration getConfig() {
        return (ETConfiguration) data.get(ContextConstants.CONFIG);
    }

    /**
     * Sets ETL configuration.
     *
     * @param config ETConfiguration object
     */
    public void setConfig(ETConfiguration config) {
        data.put(ContextConstants.CONFIG, config);
    }

    /**
     * Gets count of extracted records.
     *
     * @return extracted data count
     */
    public int getExtractedDataCount() {
        Object value = data.get(ContextConstants.EXTRACTED_DATA_COUNT);
        return value != null ? (Integer) value : 0;
    }

    /**
     * Sets count of extracted records.
     *
     * @param count extracted data count
     */
    public void setExtractedDataCount(int count) {
        data.put(ContextConstants.EXTRACTED_DATA_COUNT, count);
    }

    /**
     * Gets actual extracted data objects.
     *
     * @return extracted data, or null if not set
     */
    public Object getExtractedData() {
        return data.get(ContextConstants.EXTRACTED_DATA);
    }

    /**
     * Sets actual extracted data objects.
     *
     * @param extractedData extracted data objects
     */
    public void setExtractedData(Object extractedData) {
        data.put(ContextConstants.EXTRACTED_DATA, extractedData);
    }

    /**
     * Gets count of transformed records.
     *
     * @return transformed data count
     */
    public int getTransformedDataCount() {
        Object value = data.get(ContextConstants.TRANSFORMED_DATA_COUNT);
        return value != null ? (Integer) value : 0;
    }

    /**
     * Sets count of transformed records.
     *
     * @param count transformed data count
     */
    public void setTransformedDataCount(int count) {
        data.put(ContextConstants.TRANSFORMED_DATA_COUNT, count);
    }

    /**
     * Gets actual transformed data objects.
     *
     * @return transformed data, or null if not set
     */
    public Object getTransformedData() {
        return data.get(ContextConstants.TRANSFORMED_DATA);
    }

    /**
     * Sets actual transformed data objects.
     *
     * @param transformedData transformed data objects
     */
    public void setTransformedData(Object transformedData) {
        data.put(ContextConstants.TRANSFORMED_DATA, transformedData);
    }

    /**
     * Gets count of loaded records.
     *
     * @return loaded data count
     */
    public int getLoadedDataCount() {
        Object value = data.get(ContextConstants.LOADED_DATA_COUNT);
        return value != null ? (Integer) value : 0;
    }

    /**
     * Sets count of loaded records.
     *
     * @param count loaded data count
     */
    public void setLoadedDataCount(int count) {
        data.put(ContextConstants.LOADED_DATA_COUNT, count);
    }

    /**
     * Gets whether validation passed.
     *
     * @return true if validation passed, false otherwise
     */
    public boolean isValidationPassed() {
        Object value = data.get(ContextConstants.VALIDATION_PASSED);
        return value != null && (Boolean) value;
    }

    /**
     * Sets whether validation passed.
     *
     * @param passed true if validation passed
     */
    public void setValidationPassed(boolean passed) {
        data.put(ContextConstants.VALIDATION_PASSED, passed);
    }

    /**
     * Gets list of validation errors.
     *
     * @return list of error messages, or empty list if not set
     */
    @SuppressWarnings("unchecked")
    public List<String> getValidationErrors() {
        List<String> errors = (List<String>) data.get(ContextConstants.VALIDATION_ERRORS);
        return errors != null ? errors : new ArrayList<String>();
    }

    /**
     * Sets list of validation errors.
     *
     * @param errors list of error messages
     */
    public void setValidationErrors(List<String> errors) {
        data.put(ContextConstants.VALIDATION_ERRORS, errors != null ? errors : new ArrayList<String>());
    }

    /**
     * Gets whether cleanup has been performed.
     *
     * @return true if cleanup performed, false otherwise
     */
    public boolean isCleanupPerformed() {
        Object value = data.get(ContextConstants.CLEANUP_PERFORMED);
        return value != null && (Boolean) value;
    }

    /**
     * Sets whether cleanup has been performed.
     *
     * @param performed true if cleanup performed
     */
    public void setCleanupPerformed(boolean performed) {
        data.put(ContextConstants.CLEANUP_PERFORMED, performed);
    }

    /**
     * Gets a value from context by key with type safety.
     *
     * @param <T>  type of value to return
     * @param key   context key constant
     * @return value, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    /**
     * Sets a value in context by key with type safety.
     *
     * @param key   context key constant
     * @param value value to set
     * @param <T>  type of value
     */
    public <T> void set(String key, T value) {
        data.put(key, value);
    }

    /**
     * Gets all context data as a copy.
     *
     * @return copy of internal data map
     */
    public Map<String, Object> getAll() {
        return new HashMap<>(data);
    }

    /**
     * Clears all context data.
     */
    public void clear() {
        data.clear();
    }
}
