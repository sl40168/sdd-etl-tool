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
 * Unit tests for XbondTradeDataModel.
 */
public class XbondTradeDataModelTest {

    @Test
    public void testGetDataTypeReturnsXbondTrade() {
        XbondTradeDataModel model = new XbondTradeDataModel();
        assertEquals("DataType should be XbondTrade", "XbondTrade", model.getDataType());
    }

    @Test
    public void testGetTargetTypeReturnsDolphinDB() {
        XbondTradeDataModel model = new XbondTradeDataModel();
        assertEquals("TargetType should be DolphinDB", "DolphinDB", model.getTargetType());
    }

    @Test
    public void testHasColumnOrderAnnotations() throws Exception {
        Class<?> clazz = XbondTradeDataModel.class;
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
    public void testHas15Fields() {
        XbondTradeDataModel model = new XbondTradeDataModel();
        List<String> ordered = model.getOrderedFieldNames();
        assertEquals("Should have 15 ordered fields", 15, ordered.size());
    }

    @Test
    public void testGetOrderedFieldNamesSize() {
        XbondTradeDataModel model = new XbondTradeDataModel();
        List<String> ordered = model.getOrderedFieldNames();
        assertEquals("Should return 15 ordered field names", 15, ordered.size());
    }

    @Test
    public void testValidateReturnsTrue() {
        XbondTradeDataModel model = new XbondTradeDataModel();
        assertTrue("validate should return true", model.validate());
    }

    @Test
    public void testSetAndGetBusinessDate() {
        XbondTradeDataModel model = new XbondTradeDataModel();
        LocalDate date = LocalDate.of(2026, 1, 11);

        model.setBusinessDate(date);
        assertEquals("BusinessDate should be set and retrieved", date, model.getBusinessDate());
    }

    @Test
    public void testSetAndGetExchProductId() {
        XbondTradeDataModel model = new XbondTradeDataModel();
        String productId = "TRADE001";

        model.setExchProductId(productId);
        assertEquals("ExchProductId should be set and retrieved", productId, model.getExchProductId());
    }

    @Test
    public void testSetAndGetLastTradePrice() {
        XbondTradeDataModel model = new XbondTradeDataModel();
        double price = 99.5;

        model.setLastTradePrice(price);
        assertEquals("LastTradePrice should be set and retrieved", price, model.getLastTradePrice(), 0.001);
    }

    @Test
    public void testSetAndGetLastTradeVolume() {
        XbondTradeDataModel model = new XbondTradeDataModel();
        double volume = 1000.0;

        model.setLastTradeVolume(volume);
        assertEquals("LastTradeVolume should be set and retrieved", volume, model.getLastTradeVolume(), 0.001);
    }

    @Test
    public void testSetAndGetLastTradeSide() {
        XbondTradeDataModel model = new XbondTradeDataModel();
        String side = "BUY";

        model.setLastTradeSide(side);
        assertEquals("LastTradeSide should be set and retrieved", side, model.getLastTradeSide());
    }

    @Test
    public void testSetAndGetEventTime() {
        XbondTradeDataModel model = new XbondTradeDataModel();
        Instant eventTime = Instant.now();

        model.setEventTime(eventTime);
        assertEquals("EventTime should be set and retrieved", eventTime, model.getEventTime());
    }

    @Test
    public void testSetAndGetReceiveTime() {
        XbondTradeDataModel model = new XbondTradeDataModel();
        Instant receiveTime = Instant.now();

        model.setReceiveTime(receiveTime);
        assertEquals("ReceiveTime should be set and retrieved", receiveTime, model.getReceiveTime());
    }

    @Test
    public void testPrimitiveNumericFieldsHaveDefaultValues() {
        XbondTradeDataModel model = new XbondTradeDataModel();

        assertEquals("SettleSpeed should be initialized", -1, model.getSettleSpeed());
        assertTrue("LastTradePrice should be initialized as NaN", Double.isNaN(model.getLastTradePrice()));
        assertTrue("LastTradeVolume should be initialized as NaN", Double.isNaN(model.getLastTradeVolume()));
    }
}