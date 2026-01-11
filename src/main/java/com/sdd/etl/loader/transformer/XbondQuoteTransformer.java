package com.sdd.etl.loader.transformer;

import com.sdd.etl.loader.model.XbondQuoteDataModel;
import com.sdd.etl.model.SourceDataModel;
import com.sdd.etl.model.TargetDataModel;

/**
 * Transformer for Xbond Quote data.
 * <p>
 * Transforms source XbondQuoteDataModel (from com.sdd.etl.model) to target XbondQuoteDataModel (from com.sdd.etl.loader.model).
 * Uses reflection-based field mapping with automatic type conversion.
 * </p>
 * <p>
 * Type conversions:
 * <ul>
 *   <li>String businessDate → LocalDate businessDate (format: YYYY.MM.DD → YYYYMMDD)</li>
 *   <li>Integer settleSpeed → int settleSpeed (null → -1 sentinel value)</li>
 *   <li>Long volumes → double volumes (null → NaN sentinel value)</li>
 *   <li>Double prices/yields → double prices/yields (NaN → NaN)</li>
 *   <li>LocalDateTime times → Instant times (system timezone conversion)</li>
 * </ul>
 * </p>
 * <p>
 * 19 DolphinDB supplementary fields (preClosePrice, preSettlePrice, preInterest,
 * openPrice, highPrice, lowPrice, closePrice, settlePrice, upperLimit,
 * lowerLimit, totalVolume, totalTurnover, openInterest) remain NaN
 * as they don't exist in the source model.
 * </p>
 *
 * @since 1.0.0
 */
public class XbondQuoteTransformer extends AbstractTransformer<
        com.sdd.etl.model.XbondQuoteDataModel,
        com.sdd.etl.loader.model.XbondQuoteDataModel> {

    /**
     * Returns the source data model class for Xbond Quote data.
     *
     * @return XbondQuoteDataModel class from com.sdd.etl.model package
     */
    @Override
    public Class<com.sdd.etl.model.XbondQuoteDataModel> getSourceType() {
        return com.sdd.etl.model.XbondQuoteDataModel.class;
    }

    /**
     * Returns the target data model class for Xbond Quote data.
     *
     * @return XbondQuoteDataModel class from com.sdd.etl.loader.model package
     */
    @Override
    public Class<com.sdd.etl.loader.model.XbondQuoteDataModel> getTargetType() {
        return com.sdd.etl.loader.model.XbondQuoteDataModel.class;
    }

    /**
     * Creates a new instance of the target XbondQuoteDataModel record.
     * <p>
     * The target record is initialized with default sentinel values:
     * <ul>
     *   <li>int fields: -1</li>
     *   <li>double fields: Double.NaN</li>
     *   <li>String/LocalDate/Instant fields: null</li>
     * </ul>
     * </p>
     *
     * @return New target XbondQuoteDataModel instance
     */
    @Override
    protected com.sdd.etl.loader.model.XbondQuoteDataModel createTargetRecord() {
        return new com.sdd.etl.loader.model.XbondQuoteDataModel();
    }
}
