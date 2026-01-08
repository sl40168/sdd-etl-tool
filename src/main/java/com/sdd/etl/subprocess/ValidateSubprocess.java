package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.context.SubprocessType;

/**
 * Abstract base class for Validate subprocess.
 * API definition only - no concrete implementation in this phase.
 */
public abstract class ValidateSubprocess implements SubprocessInterface {

    /**
     * Executes validation operation.
     * Concrete implementations will validate loaded data against rules.
     *
     * @param context ETL context containing execution state
     * @return number of records validated (0 if validation fails)
     * @throws ETLException if validation process fails
     */
    @Override
    public abstract int execute(ETLContext context) throws ETLException;

    /**
     * Validates context state before validation.
     *
     * @param context ETL context to validate
     * @throws ETLException if context is invalid
     */
    @Override
    public void validateContext(ETLContext context) throws ETLException {
        // Validation logic will be implemented in concrete classes
        // For now, ensure context has loaded data count
        if (context.getLoadedDataCount() == 0) {
            throw new ETLException("VALIDATE", context.getCurrentDate(),
                    "No loaded data found in context. Cannot validate data.");
        }

        if (context.getConfig() == null ||
            context.getConfig().getValidationRules() == null ||
            context.getConfig().getValidationRules().isEmpty()) {
            throw new ETLException("VALIDATE", context.getCurrentDate(),
                    "No validation rules configured. At least one rule is required.");
        }
    }

    /**
     * Gets the type of this subprocess.
     *
     * @return SubprocessType.VALIDATE
     */
    @Override
    public SubprocessType getType() {
        return SubprocessType.VALIDATE;
    }
}
