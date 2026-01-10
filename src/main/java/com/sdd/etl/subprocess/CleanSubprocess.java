package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.context.SubprocessType;
import com.sdd.etl.util.DateUtils;

/**
 * Abstract base class for Clean subprocess.
 * API definition only - no concrete implementation in this phase.
 */
public abstract class CleanSubprocess implements SubprocessInterface {

    /**
     * Executes cleanup operation.
     * Concrete implementations will perform cleanup after validation.
     *
     * @param context ETL context containing execution state
     * @return 0 (cleanup operations typically return 0)
     * @throws ETLException if cleanup fails
     */
    @Override
    public abstract int execute(ETLContext context) throws ETLException;

    /**
     * Validates context state before cleanup.
     *
     * @param context ETL context to validate
     * @throws ETLException if context is invalid
     */
    @Override
    public void validateContext(ETLContext context) throws ETLException {
        // Validation logic will be implemented in concrete classes
        // For now, ensure context has validation results
        if (!context.isValidationPassed()) {
            throw new ETLException("CLEAN", DateUtils.formatDate(context.getCurrentDate()),
                    "Validation failed. Cannot perform cleanup.");
        }
    }

    /**
     * Gets the type of this subprocess.
     *
     * @return SubprocessType.CLEAN
     */
    @Override
    public SubprocessType getType() {
        return SubprocessType.CLEAN;
    }
}
