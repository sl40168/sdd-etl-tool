package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.context.SubprocessType;

/**
 * Abstract base class for Transform subprocess.
 * API definition only - no concrete implementation in this phase.
 */
public abstract class TransformSubprocess implements SubprocessInterface {

    /**
     * Executes transform operation.
     * Concrete implementations will transform extracted data according to rules.
     *
     * @param context ETL context containing execution state
     * @return number of records transformed
     * @throws ETLException if transformation fails
     */
    @Override
    public abstract int execute(ETLContext context) throws ETLException;

    /**
     * Validates context state before transformation.
     *
     * @param context ETL context to validate
     * @throws ETLException if context is invalid
     */
    @Override
    public void validateContext(ETLContext context) throws ETLException {
        // Validation logic will be implemented in concrete classes
        // For now, ensure context has extracted data
        if (context.getExtractedData() == null) {
            throw new ETLException("TRANSFORM", context.getCurrentDate(),
                    "No extracted data found in context. Cannot transform data.");
        }
    }

    /**
     * Gets the type of this subprocess.
     *
     * @return SubprocessType.TRANSFORM
     */
    @Override
    public SubprocessType getType() {
        return SubprocessType.TRANSFORM;
    }
}
