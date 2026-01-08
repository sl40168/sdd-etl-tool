package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.context.SubprocessType;

/**
 * Interface for ETL subprocess components.
 * All subprocess implementations must implement this interface.
 */
public interface SubprocessInterface {

    /**
     * Executes the subprocess.
     *
     * <p>Performance note: for large daily volumes (1M+ records), implementations should avoid
     * loading all records into memory at once. Prefer batching/streaming where possible and use
     * configuration hints (e.g., {@code TargetConfig.batchSize}) to size I/O operations.</p>
     *
     * @param context ETL context containing execution state
     * @return number of records processed (depends on subprocess type)
     * @throws ETLException if subprocess execution fails
     */
    int execute(ETLContext context) throws ETLException;

    /**
     * Validates context state before subprocess execution.
     *
     * @param context ETL context to validate
     * @throws ETLException if context is invalid
     */
    void validateContext(ETLContext context) throws ETLException;

    /**
     * Gets the type of this subprocess.
     *
     * @return subprocess type (EXTRACT, TRANSFORM, LOAD, VALIDATE, CLEAN)
     */
    SubprocessType getType();
}
