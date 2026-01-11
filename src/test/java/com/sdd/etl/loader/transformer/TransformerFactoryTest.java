package com.sdd.etl.loader.transformer;

import com.sdd.etl.loader.transformer.exceptions.TransformationException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for TransformerFactory.
 *
 * Verifies that factory correctly selects and instantiates transformers
 * based on data type.
 */
public class TransformerFactoryTest {

    /**
     * Test that factory returns XbondQuoteTransformer for XbondQuoteDataModel.
     */
    @Test
    public void testGetTransformer_xbondQuote() throws TransformationException {
        @SuppressWarnings("unchecked")
        Transformer<com.sdd.etl.model.XbondQuoteDataModel, com.sdd.etl.loader.model.XbondQuoteDataModel> transformer =
            (Transformer<com.sdd.etl.model.XbondQuoteDataModel, com.sdd.etl.loader.model.XbondQuoteDataModel>)
                TransformerFactory.getTransformer(com.sdd.etl.model.XbondQuoteDataModel.class);

        assertNotNull("Transformer should not be null", transformer);
        assertTrue("Should be XbondQuoteTransformer",
                  transformer instanceof XbondQuoteTransformer);
    }

    /**
     * Test that factory returns XbondTradeTransformer for XbondTradeDataModel.
     */
    @Test
    public void testGetTransformer_xbondTrade() throws TransformationException {
        @SuppressWarnings("unchecked")
        Transformer<com.sdd.etl.model.XbondTradeDataModel, com.sdd.etl.loader.model.XbondTradeDataModel> transformer =
            (Transformer<com.sdd.etl.model.XbondTradeDataModel, com.sdd.etl.loader.model.XbondTradeDataModel>)
                TransformerFactory.getTransformer(com.sdd.etl.model.XbondTradeDataModel.class);

        assertNotNull("Transformer should not be null", transformer);
        assertTrue("Should be XbondTradeTransformer",
                  transformer instanceof XbondTradeTransformer);
    }

    /**
     * Test that factory returns BondFutureQuoteTransformer for BondFutureQuoteDataModel.
     */
    @Test
    public void testGetTransformer_bondFutureQuote() throws TransformationException {
        @SuppressWarnings("unchecked")
        Transformer<com.sdd.etl.source.extract.db.quote.BondFutureQuoteDataModel, com.sdd.etl.loader.model.BondFutureQuoteDataModel> transformer =
            (Transformer<com.sdd.etl.source.extract.db.quote.BondFutureQuoteDataModel, com.sdd.etl.loader.model.BondFutureQuoteDataModel>)
                TransformerFactory.getTransformer(com.sdd.etl.source.extract.db.quote.BondFutureQuoteDataModel.class);

        assertNotNull("Transformer should not be null", transformer);
        assertTrue("Should be BondFutureQuoteTransformer",
                  transformer instanceof BondFutureQuoteTransformer);
    }

    /**
     * Test that factory throws IllegalArgumentException for null input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetTransformer_nullInput() throws TransformationException {
        TransformerFactory.getTransformer(null);
    }

    /**
     * Test that factory throws TransformationException for unknown data type.
     */
    @Test(expected = com.sdd.etl.loader.transformer.exceptions.TransformationException.class)
    public void testGetTransformer_unknownType() throws TransformationException {
        TransformerFactory.getTransformer(String.class);
    }
}
