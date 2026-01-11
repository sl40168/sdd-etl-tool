package com.sdd.etl.source.extract.db.quote;

import org.junit.Test;
import java.time.LocalDateTime;
import static org.junit.Assert.*;

public class BondFutureQuoteDataModelTest {

    @Test
    public void testPrimaryKeyGeneration() {
        BondFutureQuoteDataModel model = new BondFutureQuoteDataModel();
        model.setBusinessDate("2023.12.01");
        model.setExchProductId("IF2403");
        model.setEventTime(LocalDateTime.of(2023, 12, 1, 9, 30, 0));

        assertEquals("2023.12.01:IF2403:2023-12-01T09:30:00", model.getPrimaryKey().toString());
    }

    @Test
    public void testValidationSuccess() {
        BondFutureQuoteDataModel model = new BondFutureQuoteDataModel();
        model.setBusinessDate("2023.12.01");
        model.setExchProductId("IF2403");
        model.setEventTime(LocalDateTime.now());

        assertTrue(model.validate());
    }

    @Test
    public void testValidationFailureMissingDate() {
        BondFutureQuoteDataModel model = new BondFutureQuoteDataModel();
        model.setExchProductId("IF2403");
        model.setEventTime(LocalDateTime.now());

        assertFalse("Should fail when businessDate is null", model.validate());

        model.setBusinessDate("invalid-date");
        assertFalse("Should fail when businessDate format is wrong", model.validate());
    }

    @Test
    public void testValidationFailureMissingId() {
        BondFutureQuoteDataModel model = new BondFutureQuoteDataModel();
        model.setBusinessDate("2023.12.01");
        model.setEventTime(LocalDateTime.now());

        assertFalse("Should fail when exchProductId is null", model.validate());
    }
}
