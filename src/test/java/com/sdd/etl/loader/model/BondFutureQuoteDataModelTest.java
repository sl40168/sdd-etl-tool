package com.sdd.etl.loader.model;

import com.sdd.etl.loader.annotation.ColumnOrder;
import com.sdd.etl.model.TargetDataModel;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Unit tests for BondFutureQuoteDataModel.
 */
public class BondFutureQuoteDataModelTest {

    @Test
    public void testGetDataTypeReturnsBondFutureQuote() {
        BondFutureQuoteDataModel model = new BondFutureQuoteDataModel();
        assertEquals("DataType should be BondFutureQuote", "BondFutureQuote", model.getDataType());
    }

    @Test
    public void testGetTargetTypeReturnsDolphinDB() {
        BondFutureQuoteDataModel model = new BondFutureQuoteDataModel();
        assertEquals("TargetType should be DolphinDB", "DolphinDB", model.getTargetType());
    }

    @Test
    public void testHasColumnOrderAnnotations() throws Exception {
        Class<?> clazz = BondFutureQuoteDataModel.class;
        Field[] fields = clazz.getDeclaredFields();

        int annotatedFieldCount = 0;
        for (Field field : fields) {
            ColumnOrder annotation = field.getAnnotation(ColumnOrder.class);
            if (annotation != null) {
                annotatedFieldCount++;
            }
        }

        assertTrue("Should have at least 1 annotated field", annotatedFieldCount >= 1);
    }

    @Test
    public void testHas96Fields() {
        Field[] fields = BondFutureQuoteDataModel.class.getDeclaredFields();
        // Filter out synthetic fields
        int actualFieldCount = 0;
        for (Field field : fields) {
            if (!field.isSynthetic()) {
                actualFieldCount++;
            }
        }
        assertEquals("Should have 96 non-synthetic fields", 96, actualFieldCount);
    }

    @Test
    public void testGetOrderedFieldNamesSize() {
        BondFutureQuoteDataModel model = new BondFutureQuoteDataModel();
        List<String> ordered = model.getOrderedFieldNames();
        assertEquals("Should return 96 ordered field names", 96, ordered.size());
    }

    @Test
    public void testValidateReturnsTrue() {
        BondFutureQuoteDataModel model = new BondFutureQuoteDataModel();
        assertTrue("validate should return true", model.validate());
    }

    @Test
    public void testSetAndGetBusinessDate() {
        BondFutureQuoteDataModel model = new BondFutureQuoteDataModel();
        LocalDate date = LocalDate.of(2026, 1, 11);

        model.setBusinessDate(date);
        assertEquals("BusinessDate should be set and retrieved", date, model.getBusinessDate());
    }

    @Test
    public void testSetAndGetExchProductId() {
        BondFutureQuoteDataModel model = new BondFutureQuoteDataModel();
        String productId = "FUTURE001";

        model.setExchProductId(productId);
        assertEquals("ExchProductId should be set and retrieved", productId, model.getExchProductId());
    }

    @Test
    public void testSetAndGetLastTradePrice() {
        BondFutureQuoteDataModel model = new BondFutureQuoteDataModel();
        double price = 99.5;

        model.setLastTradePrice(price);
        assertEquals("LastTradePrice should be set and retrieved", price, model.getLastTradePrice(), 0.001);
    }

    @Test
    public void testSetAndGetLastTradeSide() {
        BondFutureQuoteDataModel model = new BondFutureQuoteDataModel();
        String side = "BUY";

        model.setLastTradeSide(side);
        assertEquals("LastTradeSide should be set and retrieved", side, model.getLastTradeSide());
    }

    @Test
    public void testSetAndGetEventTimeTrade() {
        BondFutureQuoteDataModel model = new BondFutureQuoteDataModel();
        Instant eventTime = Instant.now();

        model.setEventTimeTrade(eventTime);
        assertEquals("EventTimeTrade should be set and retrieved", eventTime, model.getEventTimeTrade());
    }

    @Test
    public void testSetAndGetEventTimeQuote() {
        BondFutureQuoteDataModel model = new BondFutureQuoteDataModel();
        Instant eventTime = Instant.now();

        model.setEventTimeQuote(eventTime);
        assertEquals("EventTimeQuote should be set and retrieved", eventTime, model.getEventTimeQuote());
    }

    @Test
    public void testSetAndGetTickType() {
        BondFutureQuoteDataModel model = new BondFutureQuoteDataModel();
        String tickType = "QUOTE";

        model.setTickType(tickType);
        assertEquals("TickType should be set and retrieved", tickType, model.getTickType());
    }

    @Test
    public void testPrimitiveNumericFieldsHaveDefaultValues() {
        BondFutureQuoteDataModel model = new BondFutureQuoteDataModel();

        assertEquals("SettleSpeed should be initialized", -1, model.getSettleSpeed());
        assertTrue("LastTradePrice should be initialized as NaN", Double.isNaN(model.getLastTradePrice()));
        assertTrue("Bid0Price should be initialized as NaN", Double.isNaN(model.getBid0Price()));
    }
}