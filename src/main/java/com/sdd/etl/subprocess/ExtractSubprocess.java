package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.context.SubprocessType;
import com.sdd.etl.source.extract.ExtractorFactory;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.model.SourceDataModel;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

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
    
    /**
     * Concrete implementation of ExtractSubprocess that supports multiple extractors.
     * Uses ExecutorService for concurrent extraction and consolidates results.
     */
    public static class MultiSourceExtractSubprocess extends ExtractSubprocess {
        private ExecutorService executorService;
        private final int timeoutSeconds;
        private static final Logger logger = LoggerFactory.getLogger(MultiSourceExtractSubprocess.class);
        
        /**
         * Logs a structured JSON event for extraction monitoring.
         * @param level Log level (INFO, WARN, ERROR)
         * @param event Event name
         * @param sourceCount Number of sources (optional)
         * @param successCount Number of successful extractors (optional)
         * @param failureCount Number of failed extractors (optional)
         * @param totalRecords Total extracted records (optional)
         * @param durationMs Total extraction duration in milliseconds (optional)
         * @param errorDetails Error message (optional)
         */
        private void logExtractionEvent(String level, String event, 
                                       Integer sourceCount, Integer successCount, 
                                       Integer failureCount, Integer totalRecords,
                                       Long durationMs, String errorDetails) {
            Map<String, Object> logData = new LinkedHashMap<>();
            logData.put("timestamp", Instant.now().toString());
            logData.put("level", level);
            logData.put("category", "EXTRACT");
            logData.put("event", event);
            if (sourceCount != null) logData.put("sourceCount", sourceCount);
            if (successCount != null) logData.put("successCount", successCount);
            if (failureCount != null) logData.put("failureCount", failureCount);
            if (totalRecords != null) logData.put("totalRecords", totalRecords);
            if (durationMs != null) logData.put("durationMs", durationMs);
            if (errorDetails != null) logData.put("errorDetails", errorDetails);
            
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
         * Converts a map to JSON string. Simple implementation for structured logging.
         */
        private String toJson(Map<String, Object> data) {
            StringBuilder json = new StringBuilder();
            json.append("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (!first) json.append(",");
                first = false;
                json.append("\"").append(entry.getKey()).append("\":");
                Object value = entry.getValue();
                if (value instanceof String) {
                    json.append("\"").append(escapeJson((String) value)).append("\"");
                } else {
                    json.append(value);
                }
            }
            json.append("}");
            return json.toString();
        }
        
        /**
         * Escapes JSON special characters in a string.
         */
        private String escapeJson(String str) {
            if (str == null) return null;
            return str.replace("\\", "\\\\")
                     .replace("\"", "\\\"")
                     .replace("\b", "\\b")
                     .replace("\f", "\\f")
                     .replace("\n", "\\n")
                     .replace("\r", "\\r")
                     .replace("\t", "\\t");
        }
        
        /**
         * Creates a MultiSourceExtractSubprocess with default timeout (30 minutes).
         */
        public MultiSourceExtractSubprocess() {
            this(1800); // 30 minutes default timeout
        }
        
        /**
         * Creates a MultiSourceExtractSubprocess with custom timeout.
         * @param timeoutSeconds Timeout in seconds for concurrent extraction
         */
        public MultiSourceExtractSubprocess(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }
        
        @Override
        public int execute(ETLContext context) throws ETLException {
            validateContext(context);
            
            long startTime = System.nanoTime();
            logExtractionEvent("INFO", "extraction_started", 
                              null, null, null, null, null, null);
            
            List<ETConfiguration.SourceConfig> sources = context.getConfig().getSources();
            if (sources.isEmpty()) {
                throw new ETLException("EXTRACT", context.getCurrentDate(), 
                        "No data sources configured");
            }
            
            // Create executor service with thread pool size based on number of sources
            int poolSize = Math.min(sources.size(), Runtime.getRuntime().availableProcessors() * 2);
            executorService = Executors.newFixedThreadPool(poolSize);
            
            List<Future<List<SourceDataModel>>> futures = new ArrayList<>();
            List<Exception> errors = new ArrayList<>();
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);
            
            // Submit extraction tasks for each source
            for (ETConfiguration.SourceConfig source : sources) {
                futures.add(executorService.submit(() -> {
                    try {
                        return ExtractorFactory.createExtractor(source).extract(context);
                    } catch (ETLException e) {
                        throw new ExecutionException(e);
                    }
                }));
            }
            
            // Shutdown executor to prevent new tasks
            executorService.shutdown();
            
            // Wait for all tasks to complete with timeout
            try {
                if (!executorService.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    throw new ETLException("EXTRACT", context.getCurrentDate(),
                            "Extraction timeout after " + timeoutSeconds + " seconds");
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
                throw new ETLException("EXTRACT", context.getCurrentDate(),
                        "Extraction interrupted: " + e.getMessage());
            }
            
            // Collect results and handle errors
            List<SourceDataModel> allRecords = new ArrayList<>();
            AtomicInteger totalCount = new AtomicInteger(0);
            
            for (Future<List<SourceDataModel>> future : futures) {
                try {
                    List<SourceDataModel> records = future.get();
                    allRecords.addAll(records);
                    totalCount.addAndGet(records.size());
                    successCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    errors.add(e);
                    failureCount.incrementAndGet();
                    logExtractionEvent("WARN", "extractor_interrupted", 
                                      null, null, null, null, null, e.getMessage());
                } catch (ExecutionException e) {
                    errors.add(e);
                    failureCount.incrementAndGet();
                    logExtractionEvent("ERROR", "extractor_failed", 
                                      null, null, null, null, null, e.getMessage());
                }
            }
            
            // Set results in context
            context.setExtractedData(allRecords);
            context.setExtractedDataCount(totalCount.get());
            
            // Handle errors
            if (!errors.isEmpty()) {
                if (totalCount.get() == 0) {
                    // All extractors failed
                    throw new ETLException("EXTRACT", context.getCurrentDate(),
                            "All extractors failed: " + errors.get(0).getMessage());
                } else {
                    // Partial success - log warnings but continue
                    logExtractionEvent("WARN", "extraction_partial_success",
                                      sources.size(), successCount.get(), failureCount.get(),
                                      totalCount.get(), null, errors.size() + " extractor(s) failed");
                }
            }
            
            // Log extraction completion with performance metrics
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            if (errors.isEmpty()) {
                logExtractionEvent("INFO", "extraction_completed",
                                  sources.size(), successCount.get(), failureCount.get(),
                                  totalCount.get(), durationMs, null);
            } else {
                logExtractionEvent("INFO", "extraction_completed_with_errors",
                                  sources.size(), successCount.get(), failureCount.get(),
                                  totalCount.get(), durationMs, errors.size() + " extractor(s) failed");
            }
            
            return totalCount.get();
        }
    }
}
