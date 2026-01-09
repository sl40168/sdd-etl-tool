package com.sdd.etl.source.extract;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.model.SourceDataModel;
import java.util.List;

/**
 * Interface for data extractors that retrieve data from various source systems.
 * 
 * <p>Extractors are responsible for:
 * <ol>
 *   <li>Establishing connection to source system using configuration from context</li>
 *   <li>Selecting relevant data based on filtering criteria from context</li>
 *   <li>Retrieving raw data from the source</li>
 *   <li>Converting raw data into standardized {@link SourceDataModel} records</li>
 *   <li>Returning the converted records for downstream processing</li>
 * </ol>
 * 
 * <p>Extractor implementations MUST NOT embed source-specific details in the API.
 * Different source systems (COS, MySQL, etc.) should have their own implementations
 * that follow this common interface.</p>
 * 
 * <p><strong>Thread Safety</strong>: Extractors may be executed concurrently by the
 * extraction subprocess. Implementations must be thread-safe or document their
 * thread safety limitations.</p>
 */
public interface Extractor {
    
    /**
     * Gets the unique category identifier for this extractor.
     * 
     * <p>The category is used for:
     * <ul>
     *   <li>File path filtering in COS extractors</li>
     *   <li>Configuration lookup</li>
     *   <li>Logging and monitoring</li>
     * </ul>
     * 
     * @return extractor category (e.g., "AllPriceDepth" for Xbond Quote extractor)
     */
    String getCategory();
    
    /**
     * Sets up connection to the source system using configuration from context.
     * 
     * <p>This method should:
     * <ol>
     *   <li>Parse source-specific configuration from context</li>
     *   <li>Initialize connection/client objects</li>
     *   <li>Validate connectivity and permissions</li>
     * </ol>
     * 
     * <p>Called once before extraction begins. If setup fails, extraction
     * should not proceed.</p>
     * 
     * @param context ETL context containing configuration and execution state
     * @throws ETLException if setup fails (configuration invalid, connection failed, etc.)
     */
    void setup(ETLContext context) throws ETLException;
    
    /**
     * Extracts data from the source system based on the provided context.
     * 
     * <p>Implementation should:
     * <ol>
     *   <li>Apply filtering based on context (e.g., business date, category)</li>
     *   <li>Retrieve raw data from source</li>
     *   <li>Convert raw data to {@link SourceDataModel} records</li>
     *   <li>Return the converted records</li>
     * </ol>
     * 
     * <p><strong>Performance Considerations</strong>:
     * <ul>
     *   <li>For large data volumes, use streaming/batching to control memory usage</li>
     *   <li>Implement timeouts to prevent indefinite blocking</li>
     *   <li>Clean up temporary resources (files, connections) after processing</li>
     * </ul>
     * 
     * @param context ETL context containing filtering criteria and configuration
     * @return list of converted {@link SourceDataModel} records, never null
     * @throws ETLException if extraction fails (connection lost, parsing error, etc.)
     */
    List<SourceDataModel> extract(ETLContext context) throws ETLException;
    
    /**
     * Cleans up resources used by this extractor.
     * 
     * <p>Called after extraction completes (successfully or with failure).
     * Implementations should release connections, close files, delete temporary
     * data, etc.</p>
     * 
     * <p>This method should be idempotent (safe to call multiple times).</p>
     * 
     * @throws ETLException if cleanup encounters unexpected errors (should be logged but not propagated)
     */
    void cleanup() throws ETLException;
    
    /**
     * Gets a descriptive name for this extractor instance.
     * 
     * <p>Used for logging, monitoring, and error reporting. Should include
     * both the extractor type and configuration-specific details (e.g.,
     * "XbondQuoteExtractor[AllPriceDepth]").</p>
     * 
     * @return descriptive name for this extractor
     */
    String getName();
    
    /**
     * Validates that the extractor is properly configured for the given context.
     * 
     * <p>This method checks that:
     * <ol>
     *   <li>Required configuration parameters are present</li>
     *   <li>Source system is accessible with current credentials</li>
     *   <li>Filter criteria from context are valid for this extractor</li>
     * </ol>
     * 
     * <p>Called before {@link #extract(ETLContext)} to ensure extraction can proceed.</p>
     * 
     * @param context ETL context to validate against
     * @throws ETLException if extractor configuration is invalid for the given context
     */
    void validate(ETLContext context) throws ETLException;
}