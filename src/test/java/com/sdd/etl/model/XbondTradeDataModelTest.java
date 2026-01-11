package com.sdd.etl.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Unit tests for XbondTradeDataModel validation rules.
 * Tests validation logic including required fields, data format,
 * and business rules.
 */
public class XbondTradeDataModelTest {
    
    private XbondTradeDataModel model;
    
    @Before
    public void setUp() {
        model = new XbondTradeDataModel();
    }
    
    @Test
    public void testValidate_ValidModelWithTradePrice_ReturnsTrue() {
        // Setup a valid model with trade price
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        model.setSettleSpeed(1);
        model.setTradePrice(100.5);
        model.setTradeVolume(1000L);
        model.setTradeId("T20250101-001");
        model.setEventTime(LocalDateTime.of(2025, 1, 1, 10, 30, 0));
        model.setReceiveTime(LocalDateTime.of(2025, 1, 1, 10, 30, 5));
        
        assertTrue("Valid model with trade price should return true", model.validate());
    }
    
    @Test
    public void testValidate_ValidModelWithTradeYield_ReturnsTrue() {
        // Setup a valid model with trade yield (no price)
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        model.setSettleSpeed(1);
        model.setTradeYield(2.5);
        model.setTradeYieldType("YTM");
        model.setTradeVolume(1000L);
        model.setTradeId("T20250101-001");
        model.setEventTime(LocalDateTime.of(2025, 1, 1, 10, 30, 0));
        model.setReceiveTime(LocalDateTime.of(2025, 1, 1, 10, 30, 5));
        
        assertTrue("Valid model with trade yield should return true", model.validate());
    }
    
    @Test
    public void testValidate_ValidModelWithTradeVolumeOnly_ReturnsTrue() {
        // Setup a valid model with only trade volume (price and yield NaN)
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        model.setSettleSpeed(1);
        model.setTradeVolume(1000L);
        model.setTradeId("T20250101-001");
        model.setEventTime(LocalDateTime.of(2025, 1, 1, 10, 30, 0));
        model.setReceiveTime(LocalDateTime.of(2025, 1, 1, 10, 30, 5));
        
        // tradePrice and tradeYield remain NaN (default)
        assertTrue("Valid model with only trade volume should return true", model.validate());
    }
    
    @Test
    public void testValidate_MissingBusinessDate_ReturnsFalse() {
        // Missing business date
        model.setExchProductId("1021001.IB");
        model.setSettleSpeed(1);
        model.setTradePrice(100.5);
        model.setTradeVolume(1000L);
        model.setTradeId("T20250101-001");
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        
        assertFalse("Missing business date should return false", model.validate());
    }
    
    @Test
    public void testValidate_InvalidBusinessDateFormat_ReturnsFalse() {
        // Invalid business date format
        model.setBusinessDate("20250101"); // Missing dots
        model.setExchProductId("1021001.IB");
        model.setSettleSpeed(1);
        model.setTradePrice(100.5);
        model.setTradeVolume(1000L);
        model.setTradeId("T20250101-001");
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        
        assertFalse("Invalid business date format should return false", model.validate());
    }
    
    @Test
    public void testValidate_MissingExchProductId_ReturnsFalse() {
        // Missing exchange product ID
        model.setBusinessDate("2025.01.01");
        model.setSettleSpeed(1);
        model.setTradePrice(100.5);
        model.setTradeVolume(1000L);
        model.setTradeId("T20250101-001");
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        
        assertFalse("Missing exchProductId should return false", model.validate());
    }
    
    @Test
    public void testValidate_ExchProductIdMissingIbSuffix_ReturnsFalse() {
        // Missing .IB suffix
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001"); // No .IB suffix
        model.setSettleSpeed(1);
        model.setTradePrice(100.5);
        model.setTradeVolume(1000L);
        model.setTradeId("T20250101-001");
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        
        assertFalse("exchProductId without .IB suffix should return false", model.validate());
    }
    
    @Test
    public void testValidate_InvalidSettleSpeed_ReturnsFalse() {
        // Invalid settle speed (must be 0 or 1)
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        model.setSettleSpeed(2); // Invalid value
        model.setTradePrice(100.5);
        model.setTradeVolume(1000L);
        model.setTradeId("T20250101-001");
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        
        assertFalse("Invalid settleSpeed should return false", model.validate());
    }
    
    @Test
    public void testValidate_SettleSpeedZero_ReturnsTrue() {
        // Valid settle speed = 0 (T+0)
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        model.setSettleSpeed(0); // Valid T+0
        model.setTradePrice(100.5);
        model.setTradeVolume(1000L);
        model.setTradeId("T20250101-001");
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        
        assertTrue("settleSpeed = 0 should return true", model.validate());
    }
    
    @Test
    public void testValidate_SettleSpeedOne_ReturnsTrue() {
        // Valid settle speed = 1 (T+1)
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        model.setSettleSpeed(1); // Valid T+1
        model.setTradePrice(100.5);
        model.setTradeVolume(1000L);
        model.setTradeId("T20250101-001");
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        
        assertTrue("settleSpeed = 1 should return true", model.validate());
    }
    
    @Test
    public void testValidate_MissingEventTime_ReturnsFalse() {
        // Missing event time
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        model.setSettleSpeed(1);
        model.setTradePrice(100.5);
        model.setTradeVolume(1000L);
        model.setTradeId("T20250101-001");
        model.setReceiveTime(LocalDateTime.now());
        
        assertFalse("Missing eventTime should return false", model.validate());
    }
    
    @Test
    public void testValidate_MissingReceiveTime_ReturnsFalse() {
        // Missing receive time
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        model.setSettleSpeed(1);
        model.setTradePrice(100.5);
        model.setTradeVolume(1000L);
        model.setTradeId("T20250101-001");
        model.setEventTime(LocalDateTime.now());
        
        assertFalse("Missing receiveTime should return false", model.validate());
    }
    
    @Test
    public void testValidate_MissingTradeId_ReturnsFalse() {
        // Missing tradeId
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        model.setSettleSpeed(1);
        model.setTradePrice(100.5);
        model.setTradeVolume(1000L);
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        
        assertFalse("Missing tradeId should return false", model.validate());
    }
    
    @Test
    public void testValidate_EmptyTradeId_ReturnsFalse() {
        // Empty tradeId
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        model.setSettleSpeed(1);
        model.setTradePrice(100.5);
        model.setTradeVolume(1000L);
        model.setTradeId("");
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        
        assertFalse("Empty tradeId should return false", model.validate());
    }
    
    @Test
    public void testValidate_NoTradeFields_ReturnsFalse() {
        // No trade fields set (all null or NaN)
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        model.setSettleSpeed(1);
        model.setTradeId("T20250101-001");
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        
        // tradePrice and tradeYield NaN (default), tradeVolume null
        assertFalse("Model with no trade fields should return false", model.validate());
    }
    
    @Test
    public void testValidate_NegativeTradePrice_ReturnsFalse() {
        // Negative trade price should fail validation
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        model.setSettleSpeed(1);
        model.setTradePrice(-100.5); // Negative price
        model.setTradeVolume(1000L);
        model.setTradeId("T20250101-001");
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        
        assertFalse("Negative trade price should return false", model.validate());
    }
    
    @Test
    public void testValidate_ZeroTradePrice_ReturnsFalse() {
        // Zero trade price should fail validation (must be positive)
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        model.setSettleSpeed(1);
        model.setTradePrice(0.0); // Zero price
        model.setTradeVolume(1000L);
        model.setTradeId("T20250101-001");
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        
        assertFalse("Zero trade price should return false", model.validate());
    }
    
    @Test
    public void testValidate_NanTradePrice_ReturnsFalseIfNoOtherFields() {
        // NaN trade price should be considered unset
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        model.setSettleSpeed(1);
        model.setTradePrice(Double.NaN); // NaN price
        model.setTradeId("T20250101-001");
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        
        // No tradeYield or tradeVolume either
        assertFalse("Model with NaN price and no other fields should return false", model.validate());
    }
    
    @Test
    public void testValidate_NegativeTradeVolume_ReturnsFalse() {
        // Negative trade volume should fail validation (must be ≥ 0)
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        model.setSettleSpeed(1);
        model.setTradePrice(100.5);
        model.setTradeVolume(-1000L); // Negative volume
        model.setTradeId("T20250101-001");
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        
        assertFalse("Negative trade volume should return false", model.validate());
    }
    
    @Test
    public void testValidate_ZeroTradeVolume_ReturnsTrue() {
        // Zero trade volume is NOT allowed (must be > 0)
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        model.setSettleSpeed(1);
        model.setTradePrice(100.5);
        model.setTradeVolume(0L); // Zero volume NOT allowed
        model.setTradeId("T20250101-001");
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        
        assertFalse("Zero trade volume should return false", model.validate());
    }
    
    @Test
    public void testValidate_TradeYieldWithType_ReturnsTrue() {
        // Valid trade yield with type
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        model.setSettleSpeed(1);
        model.setTradeYield(2.5);
        model.setTradeYieldType("YTM");
        model.setTradeId("T20250101-001");
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        
        assertTrue("Model with trade yield and type should return true", model.validate());
    }
    
    @Test
    public void testValidate_EventTimeAfterReceiveTime_ReturnsTrue() {
        // eventTime after receiveTime is allowed (optional consistency check not enforced)
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        model.setSettleSpeed(1);
        model.setTradePrice(100.5);
        model.setTradeVolume(1000L);
        model.setTradeId("T20250101-001");
        model.setEventTime(LocalDateTime.of(2025, 1, 1, 11, 0, 0));
        model.setReceiveTime(LocalDateTime.of(2025, 1, 1, 10, 0, 0)); // Earlier
        
        // Validation passes because consistency check is commented out
        assertTrue("Model with eventTime after receiveTime should return true (optional check)", model.validate());
    }
    
    @Test
    public void testGetPrimaryKey_ValidFields_ReturnsCompositeKey() {
        // Test primary key generation
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        model.setTradeId("T20250101-001");
        
        String expectedKey = "2025.01.01:1021001.IB:T20250101-001";
        assertEquals("Primary key should match expected format", expectedKey, model.getPrimaryKey());
    }
    
    @Test
    public void testGetPrimaryKey_MissingBusinessDate_ReturnsNull() {
        // Missing business date
        model.setExchProductId("1021001.IB");
        model.setTradeId("T20250101-001");
        
        assertNull("Primary key with missing businessDate should return null", model.getPrimaryKey());
    }
    
    @Test
    public void testGetPrimaryKey_MissingExchProductId_ReturnsNull() {
        // Missing exchProductId
        model.setBusinessDate("2025.01.01");
        model.setTradeId("T20250101-001");
        
        assertNull("Primary key with missing exchProductId should return null", model.getPrimaryKey());
    }
    
    @Test
    public void testGetPrimaryKey_MissingTradeId_ReturnsNull() {
        // Missing tradeId
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        
        assertNull("Primary key with missing tradeId should return null", model.getPrimaryKey());
    }
    
    @Test
    public void testGetSourceType_ReturnsXbondTradeCos() {
        assertEquals("Source type should be 'xbond_trade_cos'", "xbond_trade_cos", model.getSourceType());
    }
    
    @Test
    public void testDefaultConstructor_SetsDefaultValues() {
        // Test default values set in constructor
        assertEquals("Default productType should be 'BOND'", "BOND", model.getProductType());
        assertEquals("Default exchange should be 'CFETS'", "CFETS", model.getExchange());
        assertEquals("Default source should be 'XBOND'", "XBOND", model.getSource());
        assertEquals("Default level should be 'TRADE'", "TRADE", model.getLevel());
        assertEquals("Default status should be 'Normal'", "Normal", model.getStatus());
        
        // Double fields should be NaN initially
        assertTrue("tradePrice should be NaN initially", Double.isNaN(model.getTradePrice()));
        assertTrue("tradeYield should be NaN initially", Double.isNaN(model.getTradeYield()));
    }
    
    @Test
    public void testGetTradeSummary_WithFields_ReturnsNonEmptyMap() {
        // Setup model with trade fields
        model.setTradePrice(100.5);
        model.setTradeVolume(1000L);
        model.setCounterparty("C001");
        model.setTradeId("T20250101-001");
        
        Map<String, String> summary = model.getTradeSummary();
        assertNotNull("Trade summary should not be null", summary);
        assertTrue("Trade summary should contain tradeId", summary.containsKey("tradeId"));
        assertTrue("Trade summary should contain price", summary.containsKey("price"));
        assertTrue("Trade summary should contain volume", summary.containsKey("volume"));
        assertTrue("Trade summary should contain counterparty", summary.containsKey("counterparty"));
        assertEquals("tradeId should match", "T20250101-001", summary.get("tradeId"));
        assertEquals("price should match", "100.5", summary.get("price"));
        assertEquals("volume should match", "1000", summary.get("volume"));
    }
    
    @Test
    public void testGetTotalValue_ValidPriceAndVolume_ReturnsCorrectValue() {
        model.setTradePrice(100.5);
        model.setTradeVolume(1000L);
        
        Double expectedValue = 100500.0; // 100.5 * 1000
        assertEquals("Total value should be price × volume", expectedValue, model.getTotalValue(), 0.001);
    }
    
    @Test
    public void testGetTotalValue_ZeroVolume_ReturnsZero() {
        model.setTradePrice(100.5);
        model.setTradeVolume(0L);
        
        assertEquals("Total value with zero volume should be 0", 0.0, model.getTotalValue(), 0.001);
    }
    
    @Test
    public void testGetTotalValue_MissingPrice_ReturnsNan() {
        model.setTradeVolume(1000L);
        // tradePrice is NaN (default)
        
        assertTrue("Total value with missing price should be NaN", Double.isNaN(model.getTotalValue()));
    }
    
    @Test
    public void testGetTotalValue_MissingVolume_ReturnsNan() {
        model.setTradePrice(100.5);
        // tradeVolume is null
        
        assertTrue("Total value with missing volume should be NaN", Double.isNaN(model.getTotalValue()));
    }
    
    @Test
    public void testGetTotalValue_ZeroPrice_ReturnsZero() {
        model.setTradePrice(0.0); // Zero price (though validation would fail)
        model.setTradeVolume(1000L);
        
        assertEquals("Total value with zero price should be 0", 0.0, model.getTotalValue(), 0.001);
    }
    
    @Test
    public void testToString_ContainsExpectedFields() {
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("1021001.IB");
        model.setTradeId("T20250101-001");
        model.setTradePrice(100.5);
        model.setTradeVolume(1000L);
        model.setEventTime(LocalDateTime.of(2025, 1, 1, 10, 30, 0));
        model.setReceiveTime(LocalDateTime.of(2025, 1, 1, 10, 30, 5));
        
        String str = model.toString();
        assertTrue("toString should contain businessDate", str.contains("businessDate='2025.01.01'"));
        assertTrue("toString should contain exchProductId", str.contains("exchProductId='1021001.IB'"));
        assertTrue("toString should contain tradeId", str.contains("tradeId='T20250101-001'"));
        assertTrue("toString should contain tradePrice", str.contains("tradePrice=100.5"));
        assertTrue("toString should contain tradeVolume", str.contains("tradeVolume=1000"));
    }
}