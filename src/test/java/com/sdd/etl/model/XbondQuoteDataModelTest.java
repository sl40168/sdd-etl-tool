package com.sdd.etl.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Unit tests for XbondQuoteDataModel validation rules.
 * Tests validation logic including required fields, data format,
 * and business rules.
 */
public class XbondQuoteDataModelTest {
    
    private XbondQuoteDataModel model;
    
    @Before
    public void setUp() {
        model = new XbondQuoteDataModel();
    }
    
    @Test
    public void testValidate_ValidModel_ReturnsTrue() {
        // Setup a valid model
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("BOND001.IB");
        model.setSettleSpeed(1);
        model.setEventTime(LocalDateTime.of(2025, 1, 1, 9, 30, 0));
        model.setReceiveTime(LocalDateTime.of(2025, 1, 1, 9, 30, 1));
        
        // Set at least one price field
        model.setBid1Price(100.5);
        model.setBid1TradableVolume(1000L);
        
        assertTrue("Valid model should return true", model.validate());
    }
    
    @Test
    public void testValidate_MissingBusinessDate_ReturnsFalse() {
        // Missing business date
        model.setExchProductId("BOND001.IB");
        model.setSettleSpeed(1);
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        model.setBid1Price(100.5);
        
        assertFalse("Missing business date should return false", model.validate());
    }
    
    @Test
    public void testValidate_InvalidBusinessDateFormat_ReturnsFalse() {
        // Invalid business date format
        model.setBusinessDate("20250101"); // Missing dots
        model.setExchProductId("BOND001.IB");
        model.setSettleSpeed(1);
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        model.setBid1Price(100.5);
        
        assertFalse("Invalid business date format should return false", model.validate());
    }
    
    @Test
    public void testValidate_MissingExchProductId_ReturnsFalse() {
        // Missing exchange product ID
        model.setBusinessDate("2025.01.01");
        model.setSettleSpeed(1);
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        model.setBid1Price(100.5);
        
        assertFalse("Missing exchProductId should return false", model.validate());
    }
    
    @Test
    public void testValidate_ExchProductIdMissingIbSuffix_ReturnsFalse() {
        // Missing .IB suffix
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("BOND001"); // No .IB suffix
        model.setSettleSpeed(1);
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        model.setBid1Price(100.5);
        
        assertFalse("exchProductId without .IB suffix should return false", model.validate());
    }
    
    @Test
    public void testValidate_InvalidSettleSpeed_ReturnsFalse() {
        // Invalid settle speed (must be 0 or 1)
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("BOND001.IB");
        model.setSettleSpeed(2); // Invalid value
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        model.setBid1Price(100.5);
        
        assertFalse("Invalid settleSpeed should return false", model.validate());
    }
    
    @Test
    public void testValidate_MissingEventTime_ReturnsFalse() {
        // Missing event time
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("BOND001.IB");
        model.setSettleSpeed(1);
        model.setReceiveTime(LocalDateTime.now());
        model.setBid1Price(100.5);
        
        assertFalse("Missing eventTime should return false", model.validate());
    }
    
    @Test
    public void testValidate_MissingReceiveTime_ReturnsFalse() {
        // Missing receive time
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("BOND001.IB");
        model.setSettleSpeed(1);
        model.setEventTime(LocalDateTime.now());
        model.setBid1Price(100.5);
        
        assertFalse("Missing receiveTime should return false", model.validate());
    }
    
    @Test
    public void testValidate_NoPriceFields_ReturnsFalse() {
        // No price fields set (all null or NaN)
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("BOND001.IB");
        model.setSettleSpeed(1);
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        
        // All price fields remain null (default) or NaN
        assertFalse("Model with no price fields should return false", model.validate());
    }
    
    @Test
    public void testValidate_NegativePrice_ReturnsFalse() {
        // Negative price should fail validation
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("BOND001.IB");
        model.setSettleSpeed(1);
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        model.setBid1Price(-100.5); // Negative price
        
        assertFalse("Negative price should return false", model.validate());
    }
    
    @Test
    public void testValidate_ZeroPrice_ReturnsFalse() {
        // Zero price should fail validation (must be positive)
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("BOND001.IB");
        model.setSettleSpeed(1);
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        model.setBid1Price(0.0); // Zero price
        
        assertFalse("Zero price should return false", model.validate());
    }
    
    @Test
    public void testValidate_NegativeVolume_ReturnsFalse() {
        // Negative volume should fail validation
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("BOND001.IB");
        model.setSettleSpeed(1);
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        model.setBid1Price(100.5);
        model.setBid1TradableVolume(-1000L); // Negative volume
        
        assertFalse("Negative volume should return false", model.validate());
    }
    
    @Test
    public void testValidate_ZeroVolume_ReturnsTrue() {
        // Zero volume is allowed (non-negative)
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("BOND001.IB");
        model.setSettleSpeed(1);
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        model.setBid1Price(100.5);
        model.setBid1TradableVolume(0L); // Zero volume is allowed
        
        assertTrue("Zero volume should return true", model.validate());
    }
    
    @Test
    public void testValidate_OfferPriceOnly_ReturnsTrue() {
        // Only offer price fields set (no bid fields)
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("BOND001.IB");
        model.setSettleSpeed(1);
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        model.setOffer1Price(101.5);
        model.setOffer1TradableVolume(500L);
        
        assertTrue("Model with only offer price should return true", model.validate());
    }
    
    @Test
    public void testValidate_BothBidAndOfferPrices_ReturnsTrue() {
        // Both bid and offer price fields set
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("BOND001.IB");
        model.setSettleSpeed(1);
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        model.setBid1Price(100.5);
        model.setBid1TradableVolume(1000L);
        model.setOffer1Price(101.5);
        model.setOffer1TradableVolume(500L);
        
        assertTrue("Model with both bid and offer prices should return true", model.validate());
    }
    
    @Test
    public void testValidate_Bid0PriceValid_ReturnsTrue() {
        // Level 0 price fields (best quotes, may not be tradable)
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("BOND001.IB");
        model.setSettleSpeed(1);
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        model.setBid0Price(99.5);
        model.setBid0Volume(2000L);
        
        assertTrue("Model with bid0 price should return true", model.validate());
    }
    
    @Test
    public void testValidate_NanPrice_ReturnsFalse() {
        // NaN price should be considered unset
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("BOND001.IB");
        model.setSettleSpeed(1);
        model.setEventTime(LocalDateTime.now());
        model.setReceiveTime(LocalDateTime.now());
        model.setBid1Price(Double.NaN); // NaN price
        
        assertFalse("Model with NaN price should return false", model.validate());
    }
    
    @Test
    public void testGetPrimaryKey_ValidFields_ReturnsCompositeKey() {
        // Test primary key generation
        LocalDateTime eventTime = LocalDateTime.of(2025, 1, 1, 9, 30, 0);
        model.setBusinessDate("2025.01.01");
        model.setExchProductId("BOND001.IB");
        model.setEventTime(eventTime);
        
        String expectedKey = "2025.01.01:BOND001.IB:2025-01-01T09:30:00";
        assertEquals("Primary key should match expected format", expectedKey, model.getPrimaryKey());
    }
    
    @Test
    public void testGetPrimaryKey_MissingField_ReturnsNull() {
        // Missing business date
        model.setExchProductId("BOND001.IB");
        model.setEventTime(LocalDateTime.now());
        
        assertNull("Primary key with missing field should return null", model.getPrimaryKey());
    }
    
    @Test
    public void testGetSourceType_ReturnsXbondQuoteCos() {
        assertEquals("Source type should be 'xbond_quote_cos'", "xbond_quote_cos", model.getSourceType());
    }
    
    @Test
    public void testDefaultConstructor_SetsDefaultValues() {
        // Test default values set in constructor
        assertEquals("Default productType should be 'BOND'", "BOND", model.getProductType());
        assertEquals("Default exchange should be 'CFETS'", "CFETS", model.getExchange());
        assertEquals("Default source should be 'XBOND'", "XBOND", model.getSource());
        assertEquals("Default level should be 'L2'", "L2", model.getLevel());
        assertEquals("Default status should be 'Normal'", "Normal", model.getStatus());
    }
    
    @Test
    public void testGetQuoteSummary_WithPrices_ReturnsNonEmptyMap() {
        // Setup model with prices
        model.setBid1Price(100.5);
        model.setOffer1Price(101.5);
        model.setBid3Price(99.5);
        
        Map<String, String> summary = model.getQuoteSummary();
        assertNotNull("Quote summary should not be null", summary);
        assertTrue("Quote summary should contain bid_1", summary.containsKey("bid_1"));
        assertTrue("Quote summary should contain offer_1", summary.containsKey("offer_1"));
        assertTrue("Quote summary should contain bid_3", summary.containsKey("bid_3"));
        assertEquals("bid_1 price should match", "100.5", summary.get("bid_1"));
    }
    
    @Test
    public void testGetTotalBidTradableVolume_WithVolumes_ReturnsSum() {
        // Set multiple bid volumes
        model.setBid1TradableVolume(1000L);
        model.setBid2TradableVolume(2000L);
        model.setBid3TradableVolume(3000L);
        
        Long expectedTotal = 6000L;
        assertEquals("Total bid tradable volume should sum correctly", expectedTotal, model.getTotalBidTradableVolume());
    }
    
    @Test
    public void testGetTotalOfferTradableVolume_WithVolumes_ReturnsSum() {
        // Set multiple offer volumes
        model.setOffer1TradableVolume(500L);
        model.setOffer2TradableVolume(700L);
        model.setOffer4TradableVolume(300L);
        
        Long expectedTotal = 1500L;
        assertEquals("Total offer tradable volume should sum correctly", expectedTotal, model.getTotalOfferTradableVolume());
    }
}