package com.sdd.etl.loader.transformer;

import com.sdd.etl.loader.model.BondFutureQuoteDataModel;

/**
 * Concrete implementation of {@link Transformer} for Bond Future Quote data.
 *
 * <p>Transforms source Bond Future Quote records (from database extraction)
 * into target DolphinDB stream table format.</p>
 *
 * <p><strong>Mapping Strategy:</strong></p>
 * <ul>
 *   <li>22 source fields → 22 target fields</li>
 *   <li>74 target fields have no source (remain as NaN/Null/Empty)</li>
 *   <li>Fields are mapped by name: Source.{fieldName} → Target.{fieldName}</li>
 * </ul>
 *
 * @see AbstractTransformer
 */
public class BondFutureQuoteTransformer extends AbstractTransformer<
        com.sdd.etl.source.extract.db.quote.BondFutureQuoteDataModel,
        com.sdd.etl.loader.model.BondFutureQuoteDataModel> {

    /**
     * Returns the source model class.
     *
     * @return BondFutureQuoteDataModel class from com.sdd.etl.source.extract.db.quote package
     */
    @Override
    public Class<com.sdd.etl.source.extract.db.quote.BondFutureQuoteDataModel> getSourceType() {
        return com.sdd.etl.source.extract.db.quote.BondFutureQuoteDataModel.class;
    }

    /**
     * Returns the target model class.
     *
     * @return BondFutureQuoteDataModel class from com.sdd.etl.loader.model package
     */
    @Override
    public Class<com.sdd.etl.loader.model.BondFutureQuoteDataModel> getTargetType() {
        return com.sdd.etl.loader.model.BondFutureQuoteDataModel.class;
    }

    /**
     * Creates a new target record instance.
     *
     * @return New empty TargetDataModel instance
     */
    @Override
    protected com.sdd.etl.loader.model.BondFutureQuoteDataModel createTargetRecord() {
        return new com.sdd.etl.loader.model.BondFutureQuoteDataModel();
    }
}
