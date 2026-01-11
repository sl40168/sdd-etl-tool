package com.sdd.etl.loader.transformer;

import com.sdd.etl.loader.model.XbondTradeDataModel;
import com.sdd.etl.model.SourceDataModel;
import com.sdd.etl.model.TargetDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * Transformer for Xbond Trade data.
 * <p>
 * Transforms source XbondTradeDataModel (from com.sdd.etl.model) to target XbondTradeDataModel (from com.sdd.etl.loader.model).
 * Uses reflection-based field mapping with automatic type conversion.
 * </p>
 * <p>
 * Type conversions:
 * <ul>
 *   <li>String businessDate → LocalDate businessDate (format: YYYY.MM.DD → YYYYMMDD)</li>
 *   <li>Integer settleSpeed → int settleSpeed (null → -1 sentinel value)</li>
 *   <li>LocalDateTime times → Instant times (system timezone conversion)</li>
 * </ul>
 * </p>
 * <p>
 * Special field name mapping:
 * <ul>
 *   <li>Source field {@code tradeSide} → Target field {@code lastTradeSide}</li>
 * </ul>
 * </p>
 * <p>
 * Target fields not in source (lastTradePrice, lastTradeYield, lastTradeYieldType,
 * lastTradeVolume, lastTradeTurnover, lastTradeInterest) remain NaN.
 * </p>
 * <p>
 * Source fields not in target (tradePrice, tradeYield, tradeYieldType,
 * tradeVolume, tradeId) are ignored.
 * </p>
 *
 * @since 1.0.0
 */
public class XbondTradeTransformer extends AbstractTransformer<
        com.sdd.etl.model.XbondTradeDataModel,
        com.sdd.etl.loader.model.XbondTradeDataModel> {

    private static final Logger logger = LoggerFactory.getLogger(XbondTradeTransformer.class);

    /**
     * Returns the source data model class for Xbond Trade data.
     *
     * @return XbondTradeDataModel class from com.sdd.etl.model package
     */
    @Override
    public Class<com.sdd.etl.model.XbondTradeDataModel> getSourceType() {
        return com.sdd.etl.model.XbondTradeDataModel.class;
    }

    /**
     * Returns the target data model class for Xbond Trade data.
     *
     * @return XbondTradeDataModel class from com.sdd.etl.loader.model package
     */
    @Override
    public Class<com.sdd.etl.loader.model.XbondTradeDataModel> getTargetType() {
        return com.sdd.etl.loader.model.XbondTradeDataModel.class;
    }

    /**
     * Creates a new instance of the target XbondTradeDataModel record.
     * <p>
     * The target record is initialized with default sentinel values:
     * <ul>
     *   <li>int fields: -1</li>
     *   <li>double fields: Double.NaN</li>
     *   <li>String/Instant fields: null</li>
     * </ul>
     * </p>
     *
     * @return New target XbondTradeDataModel instance
     */
    @Override
    protected com.sdd.etl.loader.model.XbondTradeDataModel createTargetRecord() {
        return new com.sdd.etl.loader.model.XbondTradeDataModel();
    }

    /**
     * Transforms a single source record to target record with special field name mapping.
     * <p>
     * This method overrides the default implementation to handle the
     * {@code tradeSide} → {@code lastTradeSide} field name mapping.
     * </p>
     *
     * @param sourceRecord Source record to transform
     * @return Transformed target record
     * @throws RuntimeException if field mapping fails
     */
    @Override
    protected com.sdd.etl.loader.model.XbondTradeDataModel transformSingle(
            com.sdd.etl.model.XbondTradeDataModel sourceRecord) {
        try {
            com.sdd.etl.loader.model.XbondTradeDataModel targetRecord = createTargetRecord();

            // Handle special mapping: tradeSide → lastTradeSide
            Field sourceTradeSideField = sourceFieldsCache.get("tradeSide");
            Field targetLastTradeSideField = targetFieldsCache.get("lastTradeSide");

            if (sourceTradeSideField != null && targetLastTradeSideField != null) {
                Object tradeSideValue = sourceTradeSideField.get(sourceRecord);
                if (tradeSideValue != null) {
                    targetLastTradeSideField.set(targetRecord, tradeSideValue);
                    logger.debug("Mapped field: tradeSide ({}) → lastTradeSide ({})",
                            tradeSideValue, tradeSideValue);
                }
            }

            // Map all other fields by name
            for (String fieldName : sourceFieldsCache.keySet()) {
                // Skip tradeSide as it's already handled
                if ("tradeSide".equals(fieldName)) {
                    continue;
                }

                Field sourceField = sourceFieldsCache.get(fieldName);
                Field targetField = targetFieldsCache.get(fieldName);

                // Skip if target field doesn't exist
                if (targetField == null) {
                    logger.debug("Target field not found: {}, skipping", fieldName);
                    continue;
                }

                // Get source value and convert to target type
                Object sourceValue = sourceField.get(sourceRecord);
                Object convertedValue = convertValue(sourceValue, targetField.getType());

                if (convertedValue != null) {
                    targetField.set(targetRecord, convertedValue);
                }

                logger.debug("Mapped field: {} ({}) → {} ({})",
                        fieldName,
                        sourceValue != null ? sourceValue.getClass().getSimpleName() : "null",
                        fieldName,
                        convertedValue != null ? convertedValue.getClass().getSimpleName() : "null");
            }

            return targetRecord;

        } catch (Exception e) {
            throw new RuntimeException("XbondTrade field mapping failed", e);
        }
    }
}
