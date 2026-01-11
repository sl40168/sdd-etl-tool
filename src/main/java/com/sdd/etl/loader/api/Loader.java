package com.sdd.etl.loader.api;

import com.sdd.etl.model.TargetDataModel;
import com.sdd.etl.loader.api.exceptions.LoaderException;
import java.util.List;

/**
 * Core interface for loading data to various target systems.
 * Implementations must handle connection management, data transformation,
 * and loading operations for their specific target systems.
 *
 * Implementations are expected to:
 * 1. Establish and manage connections to the target system
 * 2. Convert data to the target system's expected format
 * 3. Load data in batches for performance
 * 4. Handle errors appropriately and propagate exceptions
 * 5. Provide cleanup via shutdown()
 *
 * @see com.sdd.etl.loader.dolphin.DolphinDBLoader
 */
public interface Loader {

    /**
     * Sorts the input data based on specified criteria.
     * This is typically required for time-series databases to ensure
     * proper indexing and query performance.
     *
     * @param data the data to sort
     * @param sortFieldName the field name to sort by (e.g., timestamp)
     * @throws LoaderException if sorting fails
     */
    void sortData(List<? extends TargetDataModel> data, String sortFieldName) throws LoaderException;

    /**
     * Loads the sorted data to the target system.
     * Data should be loaded in batches based on configuration.
     * Each record should be routed to the appropriate table based on
     * its dataType property.
     *
     * @param data the sorted data to load
     * @throws LoaderException if loading fails
     */
    void loadData(List<? extends TargetDataModel> data) throws LoaderException;

    /**
     * Shuts down the loader and releases all resources including:
     * - Database connections
     * - File handles
     * - Temporary files
     * - Thread pools
     *
     * Must be safe to call multiple times.
     */
    void shutdown();
}
