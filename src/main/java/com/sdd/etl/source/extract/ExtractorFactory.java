package com.sdd.etl.source.extract;

import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.source.extract.cos.config.CosSourceConfig;
import com.sdd.etl.source.extract.cos.XbondQuoteExtractor;
import com.sdd.etl.source.extract.cos.XbondTradeExtractor;
import com.sdd.etl.source.extract.db.quote.BondFutureQuoteExtractor;
import com.sdd.etl.ETLException;
import com.sdd.etl.source.extract.cos.CosExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating {@link Extractor} instances based on source
 * configuration.
 * <p>
 * This factory centralizes the creation logic for different types of
 * extractors,
 * enabling source-agnostic instantiation as required by User Story 2.
 * </p>
 */
public class ExtractorFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ExtractorFactory.class);
    private static ExtractorFactory INSTANCE = new ExtractorFactory();

    // Protected constructor to allow subclassing for testing
    protected ExtractorFactory() {
    }

    // For testing - package-private
    public static void setInstance(ExtractorFactory factory) {
        INSTANCE = factory;
    }

    /**
     * Creates an extractor instance based on the provided source configuration.
     *
     * @param sourceConfig the source configuration
     * @return an extractor instance suitable for the given configuration
     * @throws ETLException if the source type is unsupported or configuration is
     *                      invalid
     */
    public static Extractor createExtractor(ETConfiguration.SourceConfig sourceConfig) throws ETLException {
        return INSTANCE.createExtractorInstance(sourceConfig);
    }

    protected Extractor createExtractorInstance(ETConfiguration.SourceConfig sourceConfig) throws ETLException {
        if (sourceConfig == null) {
            throw new ETLException("EXTRACT", "00000000", "Source configuration cannot be null");
        }

        String sourceType = sourceConfig.getType();
        if (sourceType == null || sourceType.trim().isEmpty()) {
            throw new ETLException("EXTRACT", "00000000", "Source type must be specified in configuration");
        }

        LOG.debug("Creating extractor for source type: {}", sourceType);

        switch (sourceType.toLowerCase()) {
            case "cos":
                return createCosExtractor(sourceConfig);
            case "database":
                return createDatabaseExtractor(sourceConfig);
            // Add cases for other source types here as needed
            default:
                throw new ETLException("EXTRACT", "00000000", "Unsupported source type: " + sourceType);
        }
    }

    /**
     * Creates a COS extractor based on configuration.
     *
     * @param sourceConfig the source configuration
     * @return a COS extractor instance
     * @throws ETLException if configuration is invalid
     */
    protected Extractor createCosExtractor(ETConfiguration.SourceConfig sourceConfig) throws ETLException {
        // Validate that the config is actually a CosSourceConfig
        if (!(sourceConfig instanceof CosSourceConfig)) {
            throw new ETLException("EXTRACT", "00000000", "Expected CosSourceConfig for source type 'cos', but got: " +
                    sourceConfig.getClass().getName());
        }

        CosSourceConfig cosConfig = (CosSourceConfig) sourceConfig;
        String category = cosConfig.getProperty("category");

        // Validate category is present
        if (category == null || category.trim().isEmpty()) {
            throw new ETLException("EXTRACT", "00000000", "COS source configuration must have a 'category' property");
        }

        // Determine which concrete COS extractor to create based on category
        if ("AllPriceDepth".equalsIgnoreCase(category)) {
            LOG.debug("Creating XbondQuoteExtractor for category: {}", category);
            XbondQuoteExtractor extractor = new XbondQuoteExtractor();
            // Verify extractor's category matches configuration category
            if (!category.equalsIgnoreCase(extractor.getCategory())) {
                throw new ETLException("EXTRACT", "00000000", "Extractor category mismatch: expected '" + category +
                        "', but extractor returns '" + extractor.getCategory() + "'");
            }
            return extractor;
        } else if ("XbondCfetsDeal".equalsIgnoreCase(category)) {
            LOG.debug("Creating XbondTradeExtractor for category: {}", category);
            XbondTradeExtractor extractor = new XbondTradeExtractor();
            // Verify extractor's category matches configuration category
            if (!category.equalsIgnoreCase(extractor.getCategory())) {
                throw new ETLException("EXTRACT", "00000000", "Extractor category mismatch: expected '" + category +
                        "', but extractor returns '" + extractor.getCategory() + "'");
            }
            return extractor;
        }
        // Add other category-specific extractors here as needed

        // Fallback to generic CosExtractor (abstract) - cannot instantiate directly
        // Instead, throw exception indicating no concrete extractor for this category
        throw new ETLException("EXTRACT", "00000000", "No concrete extractor available for COS category: " + category);
    }

    /**
     * Creates a Database extractor based on configuration.
     *
     * @param sourceConfig the source configuration
     * @return a Database extractor instance
     * @throws ETLException if configuration is invalid
     */
    protected Extractor createDatabaseExtractor(ETConfiguration.SourceConfig sourceConfig) throws ETLException {
        String category = sourceConfig.getProperty("category");

        // Validate category is present
        if (category == null || category.trim().isEmpty()) {
            throw new ETLException("EXTRACT", "00000000",
                    "Database source configuration must have a 'category' property");
        }

        if ("BondFutureQuote".equalsIgnoreCase(category)) {
            LOG.debug("Creating BondFutureQuoteExtractor for category: {}", category);
            return new BondFutureQuoteExtractor();
        }

        throw new ETLException("EXTRACT", "00000000",
                "No concrete extractor available for Database category: " + category);
    }

    /**
     * Convenience method to create an extractor directly from a CosSourceConfig.
     * This is useful when the caller already knows the configuration type.
     *
     * @param cosConfig the COS source configuration
     * @return a COS extractor instance
     * @throws ETLException if configuration is invalid
     */
    public static Extractor createCosExtractor(CosSourceConfig cosConfig) throws ETLException {
        return INSTANCE.createCosExtractorInstance(cosConfig);
    }

    protected Extractor createCosExtractorInstance(CosSourceConfig cosConfig) throws ETLException {
        return createCosExtractor((ETConfiguration.SourceConfig) cosConfig);
    }
}