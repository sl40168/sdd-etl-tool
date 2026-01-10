package com.sdd.etl.source.extract.cos;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.model.SourceDataModel;
import com.sdd.etl.source.extract.Extractor;
import com.sdd.etl.source.extract.cos.config.CosSourceConfig;
import com.sdd.etl.source.extract.cos.model.CosFileMetadata;
import com.sdd.etl.source.extract.cos.model.RawQuoteRecord;

import java.io.File;
import java.util.List;

/**
 * Abstract base class for extractors that retrieve data from Tencent COS.
 * 
 * <p>Provides common COS operations:
 * <ol>
 *   <li>COS client initialization and authentication</li>
 *   <li>File listing based on category and business date</li>
 *   <li>File download to local temporary storage</li>
 *   <li>CSV parsing with streaming support</li>
 *   <li>Basic error handling for COS operations</li>
 * </ol>
 * 
 * <p>Concrete implementations must provide:
 * <ul>
 *   <li>{@link #getCategory()} - The category identifier for file filtering</li>
 *   <li>{@link #convertRawRecords(List)} - Conversion of raw records to {@link SourceDataModel}</li>
 *   <li>{@link #parseCsvFile(File)} - CSV parsing logic (can override default implementation)</li>
 * </ul>
 * 
 * <p><strong>File Selection Pattern</strong>: Files are selected using the pattern
 * {@code /{category}/{businessDate}/*.csv} where:
 * <ul>
 *   <li>{@code category}: Value from {@link #getCategory()}</li>
 *   <li>{@code businessDate}: Date from context, formatted per {@link #getBusinessDateFormat()}</li>
 * </ul>
 * 
 * <p><strong>Local Storage</strong>: Downloaded files are stored in
 * {@code {LOCAL_STORAGE}/{businessDate}/{category}/} where {@code LOCAL_STORAGE}
 * is configured in the context and {@code businessDate} is formatted as {@code YYYYMMDD}.</p>
 */
public abstract class CosExtractor implements Extractor {
    
    /** COS client instance */
    protected CosClient cosClient;
    
    /** Source configuration */
    protected CosSourceConfig sourceConfig;
    
    /** Temporary directory for downloaded files */
    protected File tempDirectory;
    
    /** List of files selected for processing */
    protected List<CosFileMetadata> selectedFiles;
    
    /**
     * Initializes the COS client using configuration from context.
     * 
     * <p>Reads COS-specific configuration from the source configuration in context,
     * validates required parameters, and creates the COS client instance.</p>
     * 
     * @param context ETL context containing configuration
     * @throws ETLException if configuration is invalid or client initialization fails
     */
    @Override
    public void setup(ETLContext context) throws ETLException {
        // Validate context has configuration
        if (context.getConfig() == null) {
            throw new ETLException("COS_EXTRACTOR", context.getCurrentDate(),
                    "Context configuration is null");
        }
        
        // Find COS source configuration
        this.sourceConfig = findCosSourceConfig(context);
        if (this.sourceConfig == null) {
            throw new ETLException("COS_EXTRACTOR", context.getCurrentDate(),
                    "No COS source configuration found for extractor category: " + getCategory());
        }
        
        // Initialize COS client
        this.cosClient = createCosClient(sourceConfig);
        
        // Create temporary directory
        this.tempDirectory = createTempDirectory(context);
    }
    
    /**
     * Extracts data from COS based on business date and category.
     * 
     * <p>Process flow:
     * <ol>
     *   <li>Select files matching category and business date pattern</li>
     *   <li>Download selected files to local temporary storage</li>
     *   <li>Parse each CSV file into raw records</li>
     *   <li>Convert raw records to {@link SourceDataModel}</li>
     *   <li>Clean up temporary files</li>
     * </ol>
     * 
     * <p>If any file download fails, the entire extraction fails for that day.</p>
     * 
     * @param context ETL context containing business date and configuration
     * @return list of converted {@link SourceDataModel} records
     * @throws ETLException if extraction fails at any step
     */
    @Override
    public List<SourceDataModel> extract(ETLContext context) throws ETLException {
        try {
            // Step 1: Select files
            this.selectedFiles = selectFiles(context);
            if (selectedFiles.isEmpty()) {
                // No files is not an error per requirements
                context.getLogger().info("No files selected for extraction. Category: {}, Date: {}", 
                    getCategory(), context.getCurrentDate());
                return List.of();
            }
            
            // Step 2: Download files
            List<File> downloadedFiles = downloadFiles(selectedFiles);
            
            // Step 3: Parse and convert
            List<RawQuoteRecord> allRawRecords = parseAllFiles(downloadedFiles);
            List<SourceDataModel> convertedRecords = convertRawRecords(allRawRecords);
            
            return convertedRecords;
            
        } catch (Exception e) {
            // Ensure cleanup on failure
            cleanupTempFiles();
            if (e instanceof ETLException) {
                throw e;
            }
            throw new ETLException("COS_EXTRACTOR", context.getCurrentDate(),
                    "Extraction failed: " + e.getMessage(), e);
        } finally {
            // Cleanup temporary files after successful or failed processing
            cleanupTempFiles();
        }
    }
    
    /**
     * Cleans up resources including COS client and temporary files.
     */
    @Override
    public void cleanup() throws ETLException {
        try {
            if (cosClient != null) {
                cosClient.shutdown();
                cosClient = null;
            }
            
            cleanupTempFiles();
            
        } catch (Exception e) {
            // Log but don't propagate cleanup errors
            // (logger would be accessed differently in real implementation)
            System.err.println("Warning: Error during COS extractor cleanup: " + e.getMessage());
        }
    }
    
    /**
     * Validates extractor configuration and context.
     */
    @Override
    public void validate(ETLContext context) throws ETLException {
        if (getCategory() == null || getCategory().trim().isEmpty()) {
            throw new ETLException("COS_EXTRACTOR", context.getCurrentDate(),
                    "Extractor category cannot be null or empty");
        }
        
        if (context.getCurrentDate() == null) {
            throw new ETLException("COS_EXTRACTOR", null,
                    "Context business date cannot be null");
        }
        
        // Additional validation can be added by concrete implementations
    }
    
    /**
     * Gets the descriptive name for this extractor.
     */
    @Override
    public String getName() {
        return String.format("%s[%s]", getClass().getSimpleName(), getCategory());
    }
    
    // --- Abstract methods for concrete implementations ---
    
    /**
     * Converts raw records to standardized {@link SourceDataModel} records.
     * 
     * <p>Implementations should:
     * <ol>
     *   <li>Group records by {@code mqOffset} (primary grouping key)</li>
     *   <li>Map raw fields to output fields based on price level and entry type</li>
     *   <li>Apply business rules (e.g., add ".IB" suffix, map settlement types)</li>
     *   <li>Create {@link SourceDataModel} instances with populated metadata</li>
     * </ol>
     * 
     * @param rawRecords list of raw records from all processed files
     * @return list of converted {@link SourceDataModel} records
     * @throws ETLException if conversion fails (data validation errors, etc.)
     */
    protected abstract List<SourceDataModel> convertRawRecords(List<RawQuoteRecord> rawRecords) 
            throws ETLException;
    
    /**
     * Gets the date format used in COS file paths.
     * 
     * <p>Default implementation returns "YYYYMMDD". Override if different format
     * is needed for a specific extractor.</p>
     * 
     * @return date format string for file path construction
     */
    protected String getBusinessDateFormat() {
        return "YYYYMMDD";
    }
    
    // --- Protected helper methods (can be overridden) ---
    
    /**
     * Finds COS source configuration from context.
     * 
     * <p>Default implementation looks for a source configuration where:
     * <ul>
     *   <li>Source type is "cos"</li>
     *   <li>Configuration contains required COS parameters</li>
     * </ul>
     * 
     * @param context ETL context with configuration
     * @return COS source configuration, or null if not found
     */
    protected CosSourceConfig findCosSourceConfig(ETLContext context) {
        // Implementation would parse configuration and create CosSourceConfig
        // This is a placeholder for the contract
        return null;
    }
    
    /**
     * Creates COS client instance from configuration.
     * 
     * @param config COS source configuration
     * @return initialized COS client
     * @throws ETLException if client creation fails
     */
    protected CosClient createCosClient(CosSourceConfig config) throws ETLException {
        // Implementation would use Tencent COS SDK
        // This is a placeholder for the contract
        return null;
    }
    
    /**
     * Creates temporary directory for downloaded files.
     * 
     * <p>Directory structure: {@code {LOCAL_STORAGE}/{businessDate}/{category}/}</p>
     * 
     * @param context ETL context containing paths and dates
     * @return temporary directory File object
     * @throws ETLException if directory creation fails
     */
    protected File createTempDirectory(ETLContext context) throws ETLException {
        // Implementation would construct path and create directory
        // This is a placeholder for the contract
        return null;
    }
    
    /**
     * Selects files from COS matching the category and business date pattern.
     * 
     * @param context ETL context with business date
     * @return list of COS file metadata for selected files
     * @throws ETLException if file listing fails
     */
    protected List<CosFileMetadata> selectFiles(ETLContext context) throws ETLException {
        // Implementation would list objects in COS using pattern
        // This is a placeholder for the contract
        return null;
    }
    
    /**
     * Downloads selected files to local temporary storage.
     * 
     * <p>If any file download fails, throws {@link ETLException}.</p>
     * 
     * @param files list of COS file metadata to download
     * @return list of downloaded File objects
     * @throws ETLException if any download fails
     */
    protected List<File> downloadFiles(List<CosFileMetadata> files) throws ETLException {
        // Implementation would download each file
        // This is a placeholder for the contract
        return null;
    }
    
    /**
     * Parses all downloaded CSV files into raw records.
     * 
     * <p>Default implementation calls {@link #parseCsvFile(File)} for each file
     * and combines the results. Override for custom batch processing.</p>
     * 
     * @param files list of downloaded CSV files
     * @return combined list of raw records from all files
     * @throws ETLException if any file parsing fails
     */
    protected List<RawQuoteRecord> parseAllFiles(List<File> files) throws ETLException {
        // Implementation would parse each file and combine results
        // This is a placeholder for the contract
        return null;
    }
    
    /**
     * Parses a single CSV file into raw records.
     * 
     * <p>Implementations should use streaming parsing to handle large files.
     * Default implementation would use OpenCSV library.</p>
     * 
     * @param csvFile CSV file to parse
     * @return list of raw records from the file
     * @throws ETLException if parsing fails
     */
    protected List<RawQuoteRecord> parseCsvFile(File csvFile) throws ETLException {
        // Implementation would parse CSV using OpenCSV
        // This is a placeholder for the contract
        return null;
    }
    
    /**
     * Cleans up temporary downloaded files.
     */
    protected void cleanupTempFiles() {
        // Implementation would delete temporary directory and contents
        // This is a placeholder for the contract
    }
    
    // --- Inner classes for COS operations ---
    
    /**
     * Interface for COS client operations.
     */
    protected interface CosClient {
        /** Lists objects matching the given prefix pattern */
        List<CosFileMetadata> listObjects(String prefix) throws ETLException;
        
        /** Downloads object to local file */
        void downloadObject(String key, File localFile) throws ETLException;
        
        /** Shuts down the client and releases resources */
        void shutdown();
    }
}