package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.context.SubprocessType;

/**
 * Abstract base class for Extract subprocess.
 * API definition only - no concrete implementation in this phase.
 */
public abstract class ExtractSubprocess implements SubprocessInterface {

    /**
     * Executes extract operation.
     * Concrete implementations will extract data from configured sources.
     *
     * @param context ETL context containing execution state
     * @return number of records extracted
     * @throws ETLException if extraction fails
     */
    @Override
    public abstract int execute(ETLContext context) throws ETLException;

    /**
     * Validates context state before extraction.
     *
     * @param context ETL context to validate
     * @throws ETLException if config is null or sources list is empty
     */
    @Override
    public void validateContext(ETLContext context) throws ETLException {
        if (context.getConfig() == null) {
            throw new ETLException("EXTRACT", context.getCurrentDate(),
                    "Configuration is null. Cannot extract data.");
        }

        if (context.getConfig().getSources() == null ||
            context.getConfig().getSources().isEmpty()) {
            throw new ETLException("EXTRACT", context.getCurrentDate(),
                    "No data sources configured. At least one source is required.");
        }
    }

    /**
     * Gets the type of this subprocess.
     *
     * @return SubprocessType.EXTRACT
     */
    @Override
    public SubprocessType getType() {
        return SubprocessType.EXTRACT;
    }
}
