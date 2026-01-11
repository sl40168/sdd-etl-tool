package com.sdd.etl.source.extract.cos;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.model.SourceDataModel;
import com.sdd.etl.source.extract.Extractor;
import com.sdd.etl.source.extract.cos.config.CosSourceConfig;
import com.sdd.etl.source.extract.cos.model.CosFileMetadata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.Instant;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sdd.etl.util.DateUtils;

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
public abstract class CosExtractor<R> implements Extractor {
    
    /** Logger instance */
    private static final Logger logger = LoggerFactory.getLogger(CosExtractor.class);
    
    /**
     * Creates a structured JSON log entry.
     *
     * @param level log level (INFO, WARN, ERROR)
     * @param category extractor category
     * @param event descriptive event name
     * @param fileCount number of files processed (optional)
     * @param recordCount number of records processed (optional)
     * @param errorDetails error details for ERROR logs (optional)
     */
    private void logStructured(String level, String category, String event, 
                               Integer fileCount, Integer recordCount, String errorDetails) {
        Map<String, Object> logData = new LinkedHashMap<>();
        logData.put("timestamp", Instant.now().toString());
        logData.put("level", level);
        logData.put("category", category);
        logData.put("event", event);
        if (fileCount != null) {
            logData.put("fileCount", fileCount);
        }
        if (recordCount != null) {
            logData.put("recordCount", recordCount);
        }
        if (errorDetails != null) {
            logData.put("errorDetails", errorDetails);
        }
        
        String json = toJson(logData);
        if ("INFO".equals(level)) {
            logger.info(json);
        } else if ("WARN".equals(level)) {
            logger.warn(json);
        } else if ("ERROR".equals(level)) {
            logger.error(json);
        }
    }
    
    /**
     * Converts a map to a simple JSON string.
     */
    private String toJson(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            Object value = entry.getValue();
            if (value == null) {
                sb.append("null");
            } else if (value instanceof String) {
                sb.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                sb.append(value.toString());
            } else {
                // fallback to string representation
                sb.append("\"").append(escapeJson(value.toString())).append("\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Escapes JSON special characters.
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /** COS client instance */
    protected CosClient cosClient;
    
    /** Source configuration */
    protected CosSourceConfig sourceConfig;
    
    /** Temporary directory for downloaded files */
    protected File tempDirectory;
    
    /** List of files selected for processing */
    protected List<CosFileMetadata> selectedFiles;
    
    /** Local storage path for downloaded files */
    private static final String LOCAL_STORAGE = System.getProperty("java.io.tmpdir", "/tmp");
    
    /** Date formatter for business date */
    private static final DateTimeFormatter BUSINESS_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
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
            throw new ETLException("COS_EXTRACTOR", DateUtils.formatDate(context.getCurrentDate()),
                    "Context configuration is null");
        }
        
        // Find COS source configuration
        this.sourceConfig = findCosSourceConfig(context);
        if (this.sourceConfig == null) {
            throw new ETLException("COS_EXTRACTOR", DateUtils.formatDate(context.getCurrentDate()),
                    "No COS source configuration found for extractor category: " + getCategory());
        }
        
        // Validate configuration
        if (!this.sourceConfig.isValid()) {
            throw new ETLException("COS_EXTRACTOR", DateUtils.formatDate(context.getCurrentDate()),
                    "Invalid COS configuration: " + this.sourceConfig);
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
            logStructured("INFO", getCategory(), "extraction_started", null, null, null);
            // Step 1: Select files
            this.selectedFiles = selectFiles(context);
            if (selectedFiles.isEmpty()) {
                // No files is not an error per requirements
                logStructured("INFO", getCategory(), "no_files_selected", 0, 0, null);
                return new ArrayList<>();
            }
            
            // Step 2: Download files
            List<File> downloadedFiles = downloadFiles(selectedFiles);
            logStructured("INFO", getCategory(), "files_downloaded", downloadedFiles.size(), null, null);
            
            // Step 3: Parse and convert
            List<R> allRawRecords = parseAllFiles(downloadedFiles);
            List<SourceDataModel> convertedRecords = convertRawRecords(allRawRecords);
            logStructured("INFO", getCategory(), "records_converted", null, convertedRecords.size(), null);
            
            return convertedRecords;
            
        } catch (Exception e) {
            // Ensure cleanup on failure
            cleanupTempFiles();
            logStructured("ERROR", getCategory(), "extraction_failed", null, null, e.getMessage());
            if (e instanceof ETLException) {
                throw e;
            }
            throw new ETLException("COS_EXTRACTOR", DateUtils.formatDate(context.getCurrentDate()),
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
                cosClient.close();
                cosClient = null;
            }
            
            cleanupTempFiles();
            
        } catch (Exception e) {
            // Log but don't propagate cleanup errors
            logStructured("WARN", getCategory(), "cleanup_error", null, null, e.getMessage());
        }
    }
    
    /**
     * Validates extractor configuration and context.
     */
    @Override
    public void validate(ETLContext context) throws ETLException {
        if (getCategory() == null || getCategory().trim().isEmpty()) {
            throw new ETLException("COS_EXTRACTOR", DateUtils.formatDate(context.getCurrentDate()),
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
    protected abstract List<SourceDataModel> convertRawRecords(List<R> rawRecords) 
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
        return "yyyyMMdd";
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
        List<ETConfiguration.SourceConfig> sources = context.getConfig().getSources();
        if (sources == null) {
            return null;
        }
        
        for (ETConfiguration.SourceConfig source : sources) {
            if ("cos".equals(source.getType()) && source instanceof CosSourceConfig) {
                return (CosSourceConfig) source;
            }
        }
        
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
        // Implementation should use Tencent COS SDK
        // This is a placeholder for the contract
        throw new ETLException("COS_EXTRACTOR", null,
                "createCosClient must be implemented by subclasses or use CosClientImpl");
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
        try {
            // Format business date for directory structure
            String businessDate = formatBusinessDateForDirectory(DateUtils.formatDate(context.getCurrentDate()));
            String category = getCategory();
            
            // Create path: LOCAL_STORAGE/businessDate/category/
            Path tempPath = Paths.get(LOCAL_STORAGE, businessDate, category);
            File tempDir = tempPath.toFile();
            
            // Create directory if it doesn't exist
            if (!tempDir.exists()) {
                Files.createDirectories(tempPath);
            }
            
            return tempDir;
            
        } catch (IOException e) {
            throw new ETLException("COS_EXTRACTOR", DateUtils.formatDate(context.getCurrentDate()),
                    "Failed to create temporary directory: " + e.getMessage(), e);
        }
    }
    
    /**
     * Formats business date from YYYYMMDD to YYYY-MM-DD for directory structure.
     * 
     * @param dateStr business date string in YYYYMMDD format
     * @return formatted date string in YYYY-MM-DD format
     */
    private String formatBusinessDateForDirectory(String dateStr) {
        if (dateStr == null || dateStr.length() != 8) {
            return dateStr; // Return as-is if invalid format
        }
        return dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6, 8);
    }
    
    /**
     * Selects files from COS matching the category and business date pattern.
     * 
     * @param context ETL context with business date
     * @return list of COS file metadata for selected files
     * @throws ETLException if file listing fails
     */
    protected List<CosFileMetadata> selectFiles(ETLContext context) throws ETLException {
        String category = getCategory();
        String businessDate = formatBusinessDateForPath(DateUtils.formatDate(context.getCurrentDate()));
        String prefix = category + "/" + businessDate + "/";
        
        return cosClient.listObjects(sourceConfig, prefix);
    }
    
    /**
     * Formats business date for COS path prefix.
     * 
     * @param dateStr business date string in YYYYMMDD format
     * @return formatted date string for COS path
     */
    private String formatBusinessDateForPath(String dateStr) {
        if (dateStr == null || dateStr.length() != 8) {
            return dateStr; // Return as-is if invalid format
        }
        return dateStr.substring(0, 4) + dateStr.substring(4, 6) + dateStr.substring(6, 8);
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
        List<File> downloadedFiles = new ArrayList<>();
        
        for (CosFileMetadata fileMetadata : files) {
            try {
                // Create local file path
                String fileName = Paths.get(fileMetadata.getKey()).getFileName().toString();
                File localFile = new File(tempDirectory, fileName);
                
                // Download file from COS
                downloadSingleFile(fileMetadata, localFile);
                
                downloadedFiles.add(localFile);
                
            } catch (Exception e) {
                throw new ETLException("COS_EXTRACTOR", null,
                        "Failed to download file: " + fileMetadata.getKey() + " - " + e.getMessage(), e);
            }
        }
        
        return downloadedFiles;
    }
    
    /**
     * Downloads a single file from COS to local storage.
     * 
     * @param fileMetadata COS file metadata
     * @param localFile local file to download to
     * @throws ETLException if download fails
     */
    private void downloadSingleFile(CosFileMetadata fileMetadata, File localFile) throws ETLException {
        // Validate file size against configured maximum
        long maxFileSize = sourceConfig.getMaxFileSizeOrDefault();
        long fileSize = fileMetadata.getSize();
        if (fileSize > maxFileSize) {
            throw new ETLException("COS_EXTRACTOR", null,
                    String.format("File size exceeds maximum allowed size: %d bytes > %d bytes (max). File: %s",
                            fileSize, maxFileSize, fileMetadata.getKey()));
        }
        
        try (InputStream inputStream = cosClient.downloadObject(sourceConfig, fileMetadata.getKey())) {
            Files.copy(inputStream, localFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ETLException("COS_EXTRACTOR", null,
                    "Failed to write file: " + localFile.getPath() + " - " + e.getMessage(), e);
        }
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
    protected List<R> parseAllFiles(List<File> files) throws ETLException {
        List<R> allRawRecords = new ArrayList<>();
        
        for (File file : files) {
            List<R> fileRecords = parseCsvFile(file);
            allRawRecords.addAll(fileRecords);
        }
        
        return allRawRecords;
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
    protected List<R> parseCsvFile(File csvFile) throws ETLException {
        // Implementation should parse CSV using OpenCSV
        // This is a placeholder for the contract
        throw new ETLException("COS_EXTRACTOR", null,
                "parseCsvFile must be implemented by subclasses or use CsvParser utility");
    }
    
    /**
     * Cleans up temporary downloaded files.
     */
    protected void cleanupTempFiles() {
        if (tempDirectory != null && tempDirectory.exists()) {
            try {
                deleteDirectory(tempDirectory);
            } catch (Exception e) {
                logStructured("WARN", getCategory(), "cleanup_failed", null, null, e.getMessage());
            }
        }
    }
    
    /**
     * Recursively deletes a directory and its contents.
     * 
     * @param directory directory to delete
     * @throws IOException if deletion fails
     */
    private void deleteDirectory(File directory) throws IOException {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        
        if (!directory.delete()) {
            throw new IOException("Failed to delete directory: " + directory.getAbsolutePath());
        }
    }
    
    // --- Getter for testing purposes ---
    
    /**
     * Gets the COS client instance (for testing).
     * 
     * @return COS client instance
     */
    protected CosClient getCosClient() {
        return cosClient;
    }
    
    /**
     * Sets the COS client instance (for testing).
     * 
     * @param cosClient COS client instance to set
     */
    protected void setCosClientForTesting(CosClient cosClient) {
        this.cosClient = cosClient;
    }
}
