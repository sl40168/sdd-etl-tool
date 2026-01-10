package com.sdd.etl.source.extract.cos.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDateTime;

/**
 * Unit tests for RawQuoteRecord data mapping and validation.
 * Tests parsing and validation logic for raw CSV records.
 */
public class RawQuoteRecordTest {
    
    private RawQuoteRecord record;
    
    @Before
    public void setUp() {
        record = new RawQuoteRecord();
    }
    
    @Test
    public void testIsValid_ValidRecord_ReturnsTrue() {
        // Setup a valid record
        record.setUnderlyingSecurityId("BOND001");
        record.setUnderlyingMdEntryType(0); // 0 for bid, 1 for offer
        record.setUnderlyingMdPriceLevel(1);
        record.setTransactTime(LocalDateTime.of(2025, 1, 1, 9, 30, 0));
        
        assertTrue("Valid record should return true", record.isValid());
    }
    
    @Test
    public void testIsValid_MissingUnderlyingSecurityId_ReturnsFalse() {
        // Missing underlyingSecurityId
        record.setUnderlyingMdEntryType(0);
        record.setUnderlyingMdPriceLevel(1);
        record.setTransactTime(LocalDateTime.now());
        
        assertFalse("Missing underlyingSecurityId should return false", record.isValid());
    }
    
    @Test
    public void testIsValid_EmptyUnderlyingSecurityId_ReturnsFalse() {
        // Empty underlyingSecurityId
        record.setUnderlyingSecurityId("");
        record.setUnderlyingMdEntryType(0);
        record.setUnderlyingMdPriceLevel(1);
        record.setTransactTime(LocalDateTime.now());
        
        assertFalse("Empty underlyingSecurityId should return false", record.isValid());
    }
    
    @Test
    public void testIsValid_WhitespaceUnderlyingSecurityId_ReturnsFalse() {
        // Whitespace-only underlyingSecurityId
        record.setUnderlyingSecurityId("   ");
        record.setUnderlyingMdEntryType(0);
        record.setUnderlyingMdPriceLevel(1);
        record.setTransactTime(LocalDateTime.now());
        
        assertFalse("Whitespace-only underlyingSecurityId should return false", record.isValid());
    }
    
    @Test
    public void testIsValid_MissingUnderlyingMdEntryType_ReturnsFalse() {
        // Missing underlyingMdEntryType
        record.setUnderlyingSecurityId("BOND001");
        record.setUnderlyingMdPriceLevel(1);
        record.setTransactTime(LocalDateTime.now());
        
        assertFalse("Missing underlyingMdEntryType should return false", record.isValid());
    }
    
    @Test
    public void testIsValid_InvalidUnderlyingMdEntryType_ReturnsFalse() {
        // Invalid underlyingMdEntryType (must be 0 or 1)
        record.setUnderlyingSecurityId("BOND001");
        record.setUnderlyingMdEntryType(2); // Invalid value
        record.setUnderlyingMdPriceLevel(1);
        record.setTransactTime(LocalDateTime.now());
        
        assertFalse("Invalid underlyingMdEntryType should return false", record.isValid());
    }
    
    @Test
    public void testIsValid_UnderlyingMdEntryTypeZero_ReturnsTrue() {
        // Valid underlyingMdEntryType = 0 (bid)
        record.setUnderlyingSecurityId("BOND001");
        record.setUnderlyingMdEntryType(0); // Valid bid
        record.setUnderlyingMdPriceLevel(1);
        record.setTransactTime(LocalDateTime.now());
        
        assertTrue("underlyingMdEntryType = 0 should return true", record.isValid());
    }
    
    @Test
    public void testIsValid_UnderlyingMdEntryTypeOne_ReturnsTrue() {
        // Valid underlyingMdEntryType = 1 (offer)
        record.setUnderlyingSecurityId("BOND001");
        record.setUnderlyingMdEntryType(1); // Valid offer
        record.setUnderlyingMdPriceLevel(1);
        record.setTransactTime(LocalDateTime.now());
        
        assertTrue("underlyingMdEntryType = 1 should return true", record.isValid());
    }
    
    @Test
    public void testIsValid_MissingUnderlyingMdPriceLevel_ReturnsFalse() {
        // Missing underlyingMdPriceLevel
        record.setUnderlyingSecurityId("BOND001");
        record.setUnderlyingMdEntryType(0);
        record.setTransactTime(LocalDateTime.now());
        
        assertFalse("Missing underlyingMdPriceLevel should return false", record.isValid());
    }
    
    @Test
    public void testIsValid_UnderlyingMdPriceLevelZero_ReturnsFalse() {
        // Invalid underlyingMdPriceLevel = 0 (must be 1-6)
        record.setUnderlyingSecurityId("BOND001");
        record.setUnderlyingMdEntryType(0);
        record.setUnderlyingMdPriceLevel(0); // Invalid value
        record.setTransactTime(LocalDateTime.now());
        
        assertFalse("underlyingMdPriceLevel = 0 should return false", record.isValid());
    }
    
    @Test
    public void testIsValid_UnderlyingMdPriceLevelSeven_ReturnsFalse() {
        // Invalid underlyingMdPriceLevel = 7 (must be 1-6)
        record.setUnderlyingSecurityId("BOND001");
        record.setUnderlyingMdEntryType(0);
        record.setUnderlyingMdPriceLevel(7); // Invalid value
        record.setTransactTime(LocalDateTime.now());
        
        assertFalse("underlyingMdPriceLevel = 7 should return false", record.isValid());
    }
    
    @Test
    public void testIsValid_ValidPriceLevelsOneToSix_AllReturnTrue() {
        // Test all valid price levels 1-6
        for (int level = 1; level <= 6; level++) {
            RawQuoteRecord r = new RawQuoteRecord();
            r.setUnderlyingSecurityId("BOND001");
            r.setUnderlyingMdEntryType(0);
            r.setUnderlyingMdPriceLevel(level);
            r.setTransactTime(LocalDateTime.now());
            
            assertTrue("underlyingMdPriceLevel = " + level + " should return true", r.isValid());
        }
    }
    
    @Test
    public void testIsValid_MissingTransactTime_ReturnsFalse() {
        // Missing transactTime
        record.setUnderlyingSecurityId("BOND001");
        record.setUnderlyingMdEntryType(0);
        record.setUnderlyingMdPriceLevel(1);
        
        assertFalse("Missing transactTime should return false", record.isValid());
    }
    
    @Test
    public void testIsValid_NullTransactTime_ReturnsFalse() {
        // Null transactTime
        record.setUnderlyingSecurityId("BOND001");
        record.setUnderlyingMdEntryType(0);
        record.setUnderlyingMdPriceLevel(1);
        record.setTransactTime(null);
        
        assertFalse("Null transactTime should return false", record.isValid());
    }
    
    @Test
    public void testIsValid_ValidRecordWithAllFields_ReturnsTrue() {
        // Valid record with all optional fields populated
        record.setId(12345L);
        record.setUnderlyingSecurityId("BOND001");
        record.setUnderlyingSettlementType(1);
        record.setUnderlyingMdEntryType(1);
        record.setUnderlyingMdEntryPx(101.5);
        record.setUnderlyingMdPriceLevel(2);
        record.setUnderlyingMdEntrySize(1000L);
        record.setUnderlyingYieldType("YTM");
        record.setUnderlyingYield(3.5);
        record.setTransactTime(LocalDateTime.of(2025, 1, 1, 9, 30, 0));
        record.setMqOffset(5000L);
        record.setRecvTime(LocalDateTime.of(2025, 1, 1, 9, 30, 1));
        
        assertTrue("Valid record with all fields should return true", record.isValid());
    }
    
    @Test
    public void testIsValid_OptionalFieldsNull_ReturnsTrue() {
        // Valid record with only required fields
        record.setUnderlyingSecurityId("BOND001");
        record.setUnderlyingMdEntryType(0);
        record.setUnderlyingMdPriceLevel(1);
        record.setTransactTime(LocalDateTime.now());
        
        // Optional fields remain null
        assertTrue("Record with only required fields should return true", record.isValid());
    }
    
    @Test
    public void testIsValid_NegativeUnderlyingMdEntrySize_ReturnsTrue() {
        // Negative underlyingMdEntrySize is allowed (size validation not specified)
        record.setUnderlyingSecurityId("BOND001");
        record.setUnderlyingMdEntryType(0);
        record.setUnderlyingMdPriceLevel(1);
        record.setTransactTime(LocalDateTime.now());
        record.setUnderlyingMdEntrySize(-1000L); // Negative size allowed
        
        assertTrue("Negative underlyingMdEntrySize should not fail validation", record.isValid());
    }
    
    @Test
    public void testIsValid_ZeroUnderlyingMdEntrySize_ReturnsTrue() {
        // Zero underlyingMdEntrySize is allowed

        record.setUnderlyingSecurityId("BOND001");
        record.setUnderlyingMdEntryType(0);
        record.setUnderlyingMdPriceLevel(1);
        record.setTransactTime(LocalDateTime.now());
        record.setUnderlyingMdEntrySize(0L); // Zero size allowed
        
        assertTrue("Zero underlyingMdEntrySize should return true", record.isValid());
    }
    
    @Test
    public void testEquals_SameInstance_ReturnsTrue() {
        // Same instance
        assertTrue("Same instance should be equal", record.equals(record));
    }
    
    @Test
    public void testEquals_Null_ReturnsFalse() {
        // Null comparison
        assertFalse("Null comparison should return false", record.equals(null));
    }
    
    @Test
    public void testEquals_DifferentClass_ReturnsFalse() {
        // Different class
        String differentClass = "string";
        assertFalse("Different class should return false", record.equals(differentClass));
    }
    
    @Test
    public void testEquals_EqualRecords_ReturnsTrue() {
        // Two equal records
        LocalDateTime transactTime = LocalDateTime.of(2025, 1, 1, 9, 30, 0);
        LocalDateTime recvTime = LocalDateTime.of(2025, 1, 1, 9, 30, 1);
        
        RawQuoteRecord record1 = new RawQuoteRecord(12345L, "BOND001", 1, 0, 101.5, 2, 1000L, "YTM", 3.5, transactTime, 5000L, recvTime);
        RawQuoteRecord record2 = new RawQuoteRecord(12345L, "BOND001", 1, 0, 101.5, 2, 1000L, "YTM", 3.5, transactTime, 5000L, recvTime);
        
        assertTrue("Equal records should return true", record1.equals(record2));
    }
    
    @Test
    public void testEquals_DifferentIds_ReturnsFalse() {
        // Different id field
        LocalDateTime transactTime = LocalDateTime.of(2025, 1, 1, 9, 30, 0);
        LocalDateTime recvTime = LocalDateTime.of(2025, 1, 1, 9, 30, 1);
        
        RawQuoteRecord record1 = new RawQuoteRecord(12345L, "BOND001", 1, 0, 101.5, 2, 1000L, "YTM", 3.5, transactTime, 5000L, recvTime);
        RawQuoteRecord record2 = new RawQuoteRecord(67890L, "BOND001", 1, 0, 101.5, 2, 1000L, "YTM", 3.5, transactTime, 5000L, recvTime);
        
        assertFalse("Different ids should return false", record1.equals(record2));
    }
    
    @Test
    public void testEquals_DifferentUnderlyingSecurityId_ReturnsFalse() {
        // Different underlyingSecurityId field
        LocalDateTime transactTime = LocalDateTime.of(2025, 1, 1, 9, 30, 0);
        LocalDateTime recvTime = LocalDateTime.of(2025, 1, 1, 9, 30, 1);
        
        RawQuoteRecord record1 = new RawQuoteRecord(12345L, "BOND001", 1, 0, 101.5, 2, 1000L, "YTM", 3.5, transactTime, 5000L, recvTime);
        RawQuoteRecord record2 = new RawQuoteRecord(12345L, "BOND002", 1, 0, 101.5, 2, 1000L, "YTM", 3.5, transactTime, 5000L, recvTime);
        
        assertFalse("Different underlyingSecurityId should return false", record1.equals(record2));
    }
    
    @Test
    public void testHashCode_EqualRecords_SameHashCode() {
        // Two equal records should have same hashCode
        LocalDateTime transactTime = LocalDateTime.of(2025, 1, 1, 9, 30, 0);
        LocalDateTime recvTime = LocalDateTime.of(2025, 1, 1, 9, 30, 1);
        
        RawQuoteRecord record1 = new RawQuoteRecord(12345L, "BOND001", 1, 0, 101.5, 2, 1000L, "YTM", 3.5, transactTime, 5000L, recvTime);
        RawQuoteRecord record2 = new RawQuoteRecord(12345L, "BOND001", 1, 0, 101.5, 2, 1000L, "YTM", 3.5, transactTime, 5000L, recvTime);
        
        assertEquals("Equal records should have same hashCode", record1.hashCode(), record2.hashCode());
    }
    
    @Test
    public void testToString_ContainsExpectedFields() {
        // toString should include key fields
        LocalDateTime transactTime = LocalDateTime.of(2025, 1, 1, 9, 30, 0);
        LocalDateTime recvTime = LocalDateTime.of(2025, 1, 1, 9, 30, 1);
        
        RawQuoteRecord record = new RawQuoteRecord(12345L, "BOND001", 1, 0, 101.5, 2, 1000L, "YTM", 3.5, transactTime, 5000L, recvTime);
        String str = record.toString();
        
        assertTrue("toString should contain record id", str.contains("id=12345"));
        assertTrue("toString should contain underlyingSecurityId", str.contains("underlyingSecurityId='BOND001'"));
        assertTrue("toString should contain underlyingMdEntryType", str.contains("underlyingMdEntryType=0"));
        assertTrue("toString should contain underlyingMdPriceLevel", str.contains("underlyingMdPriceLevel=2"));
    }
}