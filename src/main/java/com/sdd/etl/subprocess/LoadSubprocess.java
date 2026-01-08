package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.context.SubprocessType;

/**
 * Abstract base class for Load subprocess.
 * API definition only - no concrete implementation in this phase.
 */
public abstract class LoadSubprocess implements SubprocessInterface {

    /**
     * Executes load operation.
     * Concrete implementations will load transformed data to configured targets.
     *
     * @param context ETL context containing execution state
     * @return number of records loaded
     * @throws ETLException if loading fails
     */
    @Override
    public abstract int execute(ETLContext context) throws ETLException;

    /**
     * Validates context state before loading.
     *
     * @param context ETL context to validate
     * @throws ETLException if context is invalid
     */
    @Override
    public void validateContext(ETLContext context) throws ETLException {
        // Validation logic will be implemented in concrete classes
        // For now, ensure context has transformed data
        if (context.getTransformedData() == null) {
            throw new ETLException("LOAD", context.getCurrentDate(),
                    "No transformed data found in context. Cannot load data.");
        }

        if (context.getConfig() == null ||
            context.getConfig().getTargets() == null ||
            context.getConfig().getTargets().isEmpty()) {
            throw new ETLException("LOAD", context.getCurrentDate(),
                    "No data targets configured. At least one target is required.");
        }
    }

    /**
     * Gets the type of this subprocess.
     *
     * @return SubprocessType.LOAD
     */
    @Override
    public SubprocessType getType() {
        return SubprocessType.LOAD;
    }
}
