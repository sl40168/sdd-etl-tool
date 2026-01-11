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
 * Unit tests for XbondQuoteDataModel.
 */
public class XbondQuoteDataModelTest {

    @Test
    public void testGetDataTypeReturnsXbondQuote() {
        XbondQuoteDataModel model = new XbondQuoteDataModel();
        assertEquals("DataType should be XbondQuote", "XbondQuote", model.getDataType());
    }

    @Test
    public void testGetTargetTypeReturnsDolphinDB() {
        XbondQuoteDataModel model = new XbondQuoteDataModel();
        assertEquals("TargetType should be DolphinDB", "DolphinDB", model.getTargetType());
    }

    @Test
    public void testHasColumnOrderAnnotations() throws Exception {
        Class<?> clazz = XbondQuoteDataModel.class;
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
    public void testHas83Fields() {
        XbondQuoteDataModel model = new XbondQuoteDataModel();
        List<String> ordered = model.getOrderedFieldNames();
        assertEquals("Should have 83 ordered fields", 83, ordered.size());
    }

    @Test
    public void testGetOrderedFieldNamesSize() {
        XbondQuoteDataModel model = new XbondQuoteDataModel();
        List<String> ordered = model.getOrderedFieldNames();
        assertEquals("Should return 83 ordered field names", 83, ordered.size());
    }

    @Test
    public void testValidateReturnsTrue() {
        XbondQuoteDataModel model = new XbondQuoteDataModel();
        assertTrue("validate should return true", model.validate());
    }

    @Test
    public void testSetAndGetBusinessDate() {
        XbondQuoteDataModel model = new XbondQuoteDataModel();
        LocalDate date = LocalDate.of(2026, 1, 11);

        model.setBusinessDate(date);
        assertEquals("BusinessDate should be set and retrieved", date, model.getBusinessDate());
    }

    @Test
    public void testSetAndGetExchProductId() {
        XbondQuoteDataModel model = new XbondQuoteDataModel();
        String productId = "TEST001";

        model.setExchProductId(productId);
        assertEquals("ExchProductId should be set and retrieved", productId, model.getExchProductId());
    }

    @Test
    public void testSetAndGetProductType() {
        XbondQuoteDataModel model = new XbondQuoteDataModel();
        String productType = "BOND";

        model.setProductType(productType);
        assertEquals("ProductType should be set and retrieved", productType, model.getProductType());
    }

    @Test
    public void testSetAndGetExchange() {
        XbondQuoteDataModel model = new XbondQuoteDataModel();
        String exchange = "SSE";

        model.setExchange(exchange);
        assertEquals("Exchange should be set and retrieved", exchange, model.getExchange());
    }

    @Test
    public void testSetAndGetSettleSpeed() {
        XbondQuoteDataModel model = new XbondQuoteDataModel();
        int settleSpeed = 1;

        model.setSettleSpeed(settleSpeed);
        assertEquals("SettleSpeed should be set and retrieved", settleSpeed, model.getSettleSpeed());
    }

    @Test
    public void testSetAndGetBid0Price() {
        XbondQuoteDataModel model = new XbondQuoteDataModel();
        double price = 99.5;

        model.setBid0Price(price);
        assertEquals("Bid0Price should be set and retrieved", price, model.getBid0Price(), 0.001);
    }

    @Test
    public void testSetAndGetOffer0Price() {
        XbondQuoteDataModel model = new XbondQuoteDataModel();
        double price = 99.6;

        model.setOffer0Price(price);
        assertEquals("Offer0Price should be set and retrieved", price, model.getOffer0Price(), 0.001);
    }

    @Test
    public void testSetAndGetEventTime() {
        XbondQuoteDataModel model = new XbondQuoteDataModel();
        Instant eventTime = Instant.now();

        model.setEventTime(eventTime);
        assertEquals("EventTime should be set and retrieved", eventTime, model.getEventTime());
    }

    @Test
    public void testSetAndGetReceiveTime() {
        XbondQuoteDataModel model = new XbondQuoteDataModel();
        Instant receiveTime = Instant.now();

        model.setReceiveTime(receiveTime);
        assertEquals("ReceiveTime should be set and retrieved", receiveTime, model.getReceiveTime());
    }

    @Test
    public void testPrimitiveNumericFieldsHaveDefaultValues() {
        XbondQuoteDataModel model = new XbondQuoteDataModel();

        assertEquals("SettleSpeed should be initialized", -1, model.getSettleSpeed());
        assertTrue("PreClosePrice should be initialized as NaN", Double.isNaN(model.getPreClosePrice()));
        assertTrue("Bid0Price should be initialized as NaN", Double.isNaN(model.getBid0Price()));
    }
}