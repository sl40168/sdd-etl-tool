package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.context.SubprocessType;
import com.sdd.etl.loader.transformer.Transformer;
import com.sdd.etl.loader.transformer.TransformerFactory;
import com.sdd.etl.loader.transformer.exceptions.TransformationException;
import com.sdd.etl.model.SourceDataModel;
import com.sdd.etl.model.TargetDataModel;
import com.sdd.etl.util.DateUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Subprocess for transforming source data models into target data models.
 *
 * <p>This subprocess orchestrates the concurrent transformation of multiple
 * data types. It retrieves extracted data from ETLContext, applies
 * appropriate transformations using {@link Transformer} implementations,
 * and stores results back into context for loading.</p>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Concurrent transformation using fixed thread pool</li>
 *   <li>Automatic transformer selection via {@link TransformerFactory}</li>
 *   <li>Immediate failure halt on any exception</li>
 *   <li>Results aggregation in {@link TransformResult}</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class TransformSubprocess implements SubprocessInterface {

    private Map<String, Class<?>> dataTypeClassMap;

    /**
     * Constructs a new TransformSubprocess.
     */
    public TransformSubprocess() {
        this.dataTypeClassMap = new HashMap<>();
        initializeDataTypeMap();
    }

    /**
     * Initializes mapping between data type names and their source classes.
     */
    private void initializeDataTypeMap() {
        dataTypeClassMap.put("XBOND_QUOTE", com.sdd.etl.model.XbondQuoteDataModel.class);
        dataTypeClassMap.put("XBOND_TRADE", com.sdd.etl.model.XbondTradeDataModel.class);
        dataTypeClassMap.put("BOND_FUTURE_QUOTE", com.sdd.etl.source.extract.db.quote.BondFutureQuoteDataModel.class);
    }

    /**
     * Validates context state before transformation.
     *
     * @param context ETL context to validate
     * @throws ETLException if context is invalid
     */
    @Override
    public void validateContext(ETLContext context) throws ETLException {
        if (context == null) {
            throw new ETLException("TRANSFORM", "N/A", "Context cannot be null");
        }
        if (context.getExtractedData() == null) {
            LocalDate date = context.getCurrentDate();
            throw new ETLException("TRANSFORM", DateUtils.formatDate(date),
                "No extracted data found in context");
        }
    }

    /**
     * Gets the type of this subprocess.
     *
     * @return TRANSFORM
     */
    @Override
    public SubprocessType getType() {
        return SubprocessType.TRANSFORM;
    }

    /**
     * Executes transform subprocess.
     *
     * <p>This method:
     * <ol>
     *   <li>Validates context state</li>
     *   <li>Retrieves extracted data from context</li>
     *   <li>Creates thread pool for concurrent transformation</li>
     *   <li>Submits transformation tasks for each data type</li>
     *   <li>Aggregates results and stores in context</li>
     *   <li>Shuts down thread pool</li>
     * </ol>
     * </p>
     *
     * @param context ETL context containing execution state
     * @return number of records transformed
     * @throws ETLException if transformation fails
     */
    @Override
    public int execute(ETLContext context) throws ETLException {
        // Get extracted data from context
        Object extractedData = context.getExtractedData();
        if (extractedData == null) {
            throw new TransformationException(
                "TransformSubprocess",
                0,
                "No extracted data found in context"
            );
        }

        // Extract data by type
        Map<String, List<SourceDataModel>> dataByType = extractDataByType(extractedData);

        // Count non-empty data types
        int nonEmptyTypes = countNonEmptyTypes(dataByType);
        if (nonEmptyTypes == 0) {
            throw new TransformationException(
                "TransformSubprocess",
                0,
                "No data to transform (all data types are empty)"
            );
        }

        // Create thread pool
        ExecutorService executor = Executors.newFixedThreadPool(nonEmptyTypes);

        try {
            // Submit transformation tasks
            List<Callable<TransformResult>> tasks = createTransformationTasks(dataByType);
            List<Future<TransformResult>> futures;

            try {
                futures = executor.invokeAll(tasks);
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
                throw new TransformationException(
                    "TransformSubprocess",
                    0,
                    "Transformation interrupted",
                    e
                );
            }

            // Collect results
            Map<String, List<? extends TargetDataModel>> allTransformedData = new HashMap<>();
            int totalTransformed = 0;

            for (Future<TransformResult> future : futures) {
                try {
                    TransformResult result = future.get();
                    if (result.isSuccess()) {
                        allTransformedData.put(result.getDataType(), result.getTransformedData());
                        totalTransformed += result.getRecordCount();
                    } else {
                        throw new TransformationException(
                            "TransformSubprocess",
                            result.getRecordCount(),
                            "Transformation failed: " + result.getError(),
                            result.getException()
                        );
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new TransformationException(
                        "TransformSubprocess",
                        totalTransformed,
                        "Transformation interrupted",
                        e
                    );
                } catch (ExecutionException e) {
                    throw new TransformationException(
                        "TransformSubprocess",
                        totalTransformed,
                        "Transformation execution failed",
                        e.getCause()
                    );
                }
            }

            // Store results in context
            context.setTransformedData(allTransformedData);
            context.setTransformedDataCount(totalTransformed);

            return totalTransformed;

        } finally {
            executor.shutdown();
        }
    }

    /**
     * Extracts data grouped by data type from raw extracted data object.
     *
     * @param extractedData raw extracted data object
     * @return map of data type to list of source data models
     * @throws TransformationException if data extraction fails
     */
    @SuppressWarnings("unchecked")
    private Map<String, List<SourceDataModel>> extractDataByType(Object extractedData) throws TransformationException {
        Map<String, List<SourceDataModel>> dataByType = new HashMap<>();

        // If extracted data is a map, use it directly
        if (extractedData instanceof Map) {
            Map<?, ?> extractedMap = (Map<?, ?>) extractedData;
            for (Map.Entry<?, ?> entry : extractedMap.entrySet()) {
                if (entry.getKey() instanceof String && entry.getValue() instanceof List) {
                    String dataType = (String) entry.getKey();
                    List<?> list = (List<?>) entry.getValue();
                    List<SourceDataModel> sourceData = (List<SourceDataModel>) list;
                    dataByType.put(dataType, sourceData);
                }
            }
        } else {
            // If not a map, try to determine type from object
            // This is a fallback - actual implementation should extract from context
            throw new TransformationException(
                "TransformSubprocess",
                0,
                "Unexpected extracted data type: " + extractedData.getClass().getName()
            );
        }

        return dataByType;
    }

    /**
     * Counts number of non-empty data types.
     *
     * @param dataByType map of data type to list of source data models
     * @return count of data types with non-empty lists
     */
    private int countNonEmptyTypes(Map<String, List<SourceDataModel>> dataByType) {
        int count = 0;
        for (List<SourceDataModel> data : dataByType.values()) {
            if (data != null && !data.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Creates transformation tasks for each data type.
     *
     * @param dataByType map of data type to list of source data models
     * @return list of transformation tasks
     */
    private List<Callable<TransformResult>> createTransformationTasks(
            Map<String, List<SourceDataModel>> dataByType) {

        List<Callable<TransformResult>> tasks = new ArrayList<>();

        for (Map.Entry<String, List<SourceDataModel>> entry : dataByType.entrySet()) {
            String dataType = entry.getKey();
            List<SourceDataModel> sourceData = entry.getValue();

            if (sourceData == null || sourceData.isEmpty()) {
                continue;
            }

            tasks.add(new TransformationTask(dataType, sourceData));
        }

        return tasks;
    }

    /**
     * Inner class representing a transformation task.
     */
    private class TransformationTask implements Callable<TransformResult> {

        private final String dataType;
        private final List<SourceDataModel> sourceData;

        /**
         * Constructs a new TransformationTask.
         *
         * @param dataType data type name
         * @param sourceData source data to transform
         */
        TransformationTask(String dataType, List<SourceDataModel> sourceData) {
            this.dataType = dataType;
            this.sourceData = sourceData;
        }

        /**
         * Executes transformation.
         *
         * @return transformation result
         */
        @Override
        public TransformResult call() {
            try {
                Class<?> sourceClass = dataTypeClassMap.get(dataType);
                if (sourceClass == null) {
                    throw new TransformationException(
                        "TransformationTask",
                        0,
                        "No source class registered for data type: " + dataType
                    );
                }

                @SuppressWarnings("unchecked")
                Transformer<SourceDataModel, TargetDataModel> transformer =
                    (Transformer<SourceDataModel, TargetDataModel>)
                        TransformerFactory.getTransformer(sourceClass);

                List<? extends TargetDataModel> transformedData = transformer.transform(sourceData);

                return new TransformResult(
                    dataType,
                    transformedData,
                    transformedData.size(),
                    null,
                    true
                );

            } catch (Exception e) {
                return new TransformResult(
                    dataType,
                    null,
                    sourceData.size(),
                    e.getMessage(),
                    false,
                    e
                );
            }
        }
    }

    /**
     * Inner class representing a transformation result.
     */
    private static class TransformResult {

        private final String dataType;
        private final List<? extends TargetDataModel> transformedData;
        private final int recordCount;
        private final String error;
        private final boolean success;
        private final Exception exception;

        /**
         * Constructs a successful TransformResult.
         *
         * @param dataType the data type name
         * @param transformedData the transformed data
         * @param recordCount the number of records transformed
         */
        TransformResult(String dataType,
                        List<? extends TargetDataModel> transformedData,
                        int recordCount) {
            this(dataType, transformedData, recordCount, null, true);
        }

        /**
         * Constructs a TransformResult with all fields.
         *
         * @param dataType the data type name
         * @param transformedData the transformed data
         * @param recordCount the number of records
         * @param error the error message (if failed)
         * @param success whether transformation was successful
         */
        TransformResult(String dataType,
                        List<? extends TargetDataModel> transformedData,
                        int recordCount,
                        String error,
                        boolean success) {
            this(dataType, transformedData, recordCount, error, success, null);
        }

        /**
         * Constructs a TransformResult with all fields including exception.
         *
         * @param dataType the data type name
         * @param transformedData the transformed data
         * @param recordCount the number of records
         * @param error the error message (if failed)
         * @param success whether transformation was successful
         * @param exception the exception (if failed)
         */
        TransformResult(String dataType,
                        List<? extends TargetDataModel> transformedData,
                        int recordCount,
                        String error,
                        boolean success,
                        Exception exception) {
            this.dataType = dataType;
            this.transformedData = transformedData;
            this.recordCount = recordCount;
            this.error = error;
            this.success = success;
            this.exception = exception;
        }

        /**
         * Gets the data type name.
         *
         * @return data type name
         */
        String getDataType() {
            return dataType;
        }

        /**
         * Gets the transformed data.
         *
         * @return transformed data, or null if failed
         */
        List<? extends TargetDataModel> getTransformedData() {
            return transformedData;
        }

        /**
         * Gets the record count.
         *
         * @return number of records
         */
        int getRecordCount() {
            return recordCount;
        }

        /**
         * Gets the error message.
         *
         * @return error message, or null if successful
         */
        String getError() {
            return error;
        }

        /**
         * Gets the exception.
         *
         * @return exception, or null if successful
         */
        Exception getException() {
            return exception;
        }

        /**
         * Checks if transformation was successful.
         *
         * @return true if successful, false otherwise
         */
        boolean isSuccess() {
            return success;
        }
    }
}
