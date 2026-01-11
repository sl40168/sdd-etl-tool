package com.sdd.etl.source.extract.cos.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDateTime;

/**
 * Unit tests for RawTradeRecord data mapping and validation.
 * Tests parsing and validation logic for raw CSV trade records.
 */
public class RawTradeRecordTest {
    
    private RawTradeRecord record;
    
    @Before
    public void setUp() {
        record = new RawTradeRecord();
    }
    
    @Test
    public void testIsValid_ValidRecord_ReturnsTrue() {
        // Setup a valid record
        record.setUnderlyingSecurityId("1021001");
        record.setUnderlyingSettlementType(1); // 1 for T+1
        record.setTradePrice(100.5);
        record.setTradeVolume(1000L);
        record.setTradeId("T20250101-001");
        record.setTransactTime(LocalDateTime.of(2025, 1, 1, 10, 30, 0));
        record.setMqOffset(500L);
        record.setRecvTime(LocalDateTime.of(2025, 1, 1, 10, 30, 5));
        
        assertTrue("Valid record should return true", record.isValid());
    }
    
    @Test
    public void testIsValid_MissingUnderlyingSecurityId_ReturnsFalse() {
        // Missing underlyingSecurityId
        record.setUnderlyingSettlementType(1);
        record.setTradePrice(100.5);
        record.setTradeVolume(1000L);
        record.setTradeId("T20250101-001");
        record.setTransactTime(LocalDateTime.now());
        record.setMqOffset(500L);
        record.setRecvTime(LocalDateTime.now());
        
        assertFalse("Missing underlyingSecurityId should return false", record.isValid());
    }
    
    @Test
    public void testIsValid_EmptyUnderlyingSecurityId_ReturnsFalse() {
        // Empty underlyingSecurityId
        record.setUnderlyingSecurityId("");
        record.setUnderlyingSettlementType(1);
        record.setTradePrice(100.5);
        record.setTradeVolume(1000L);
        record.setTradeId("T20250101-001");
        record.setTransactTime(LocalDateTime.now());
        record.setMqOffset(500L);
        record.setRecvTime(LocalDateTime.now());
        
        assertFalse("Empty underlyingSecurityId should return false", record.isValid());
    }
    
    @Test
    public void testIsValid_WhitespaceUnderlyingSecurityId_ReturnsFalse() {
        // Whitespace-only underlyingSecurityId
        record.setUnderlyingSecurityId("   ");
        record.setUnderlyingSettlementType(1);
        record.setTradePrice(100.5);
        record.setTradeVolume(1000L);
        record.setTradeId("T20250101-001");
        record.setTransactTime(LocalDateTime.now());
        record.setMqOffset(500L);
        record.setRecvTime(LocalDateTime.now());
        
        assertFalse("Whitespace-only underlyingSecurityId should return false", record.isValid());
    }
    
    @Test
    public void testIsValid_MissingUnderlyingSettlementType_ReturnsFalse() {
        // Missing underlyingSettlementType
        record.setUnderlyingSecurityId("1021001");
        record.setTradePrice(100.5);
        record.setTradeVolume(1000L);
        record.setTradeId("T20250101-001");
        record.setTransactTime(LocalDateTime.now());
        record.setMqOffset(500L);
        record.setRecvTime(LocalDateTime.now());
        
        assertFalse("Missing underlyingSettlementType should return false", record.isValid());
    }
    
    @Test
    public void testIsValid_InvalidUnderlyingSettlementType_ReturnsFalse() {
        // Invalid underlyingSettlementType (must be 0 or 1)
        record.setUnderlyingSecurityId("1021001");
        record.setUnderlyingSettlementType(2); // Invalid value
        record.setTradePrice(100.5);
        record.setTradeVolume(1000L);
        record.setTradeId("T20250101-001");
        record.setTransactTime(LocalDateTime.now());
        record.setMqOffset(500L);
        record.setRecvTime(LocalDateTime.now());
        
        assertFalse("Invalid underlyingSettlementType should return false", record.isValid());
    }
    
    @Test
    public void testIsValid_UnderlyingSettlementTypeZero_ReturnsTrue() {
        // Valid underlyingSettlementType = 0 (T+0)
        record.setUnderlyingSecurityId("1021001");
        record.setUnderlyingSettlementType(0); // Valid T+0
        record.setTradePrice(100.5);
        record.setTradeVolume(1000L);
        record.setTradeId("T20250101-001");
        record.setTransactTime(LocalDateTime.now());
        record.setMqOffset(500L);
        record.setRecvTime(LocalDateTime.now());
        
        assertTrue("underlyingSettlementType = 0 should return true", record.isValid());
    }
    
    @Test
    public void testIsValid_UnderlyingSettlementTypeOne_ReturnsTrue() {
        // Valid underlyingSettlementType = 1 (T+1)
        record.setUnderlyingSecurityId("1021001");
        record.setUnderlyingSettlementType(1); // Valid T+1
        record.setTradePrice(100.5);
        record.setTradeVolume(1000L);
        record.setTradeId("T20250101-001");
        record.setTransactTime(LocalDateTime.now());
        record.setMqOffset(500L);
        record.setRecvTime(LocalDateTime.now());
        
        assertTrue("underlyingSettlementType = 1 should return true", record.isValid());
    }
    
    @Test
    public void testIsValid_MissingTradePrice_ReturnsTrue() {
        // Missing tradePrice is now allowed (optional field)
        record.setUnderlyingSecurityId("1021001");
        record.setUnderlyingSettlementType(1);
        record.setTradeVolume(1000L);
        record.setTradeId("T20250101-001");
        record.setTransactTime(LocalDateTime.now());
        record.setMqOffset(500L);
        record.setRecvTime(LocalDateTime.now());
        
        assertTrue("Missing tradePrice should return true (optional field)", record.isValid());
    }
    
    @Test
    public void testIsValid_ZeroTradePrice_ReturnsTrue() {
        // Zero tradePrice is now allowed (optional field)
        record.setUnderlyingSecurityId("1021001");
        record.setUnderlyingSettlementType(1);
        record.setTradePrice(0.0);
        record.setTradeVolume(1000L);
        record.setTradeId("T20250101-001");
        record.setTransactTime(LocalDateTime.now());
        record.setMqOffset(500L);
        record.setRecvTime(LocalDateTime.now());
        
        assertTrue("Zero tradePrice should return true (optional field)", record.isValid());
    }
    
    @Test
    public void testIsValid_NegativeTradePrice_ReturnsTrue() {
        // Negative tradePrice is now allowed (optional field)
        record.setUnderlyingSecurityId("1021001");
        record.setUnderlyingSettlementType(1);
        record.setTradePrice(-10.5);
        record.setTradeVolume(1000L);
        record.setTradeId("T20250101-001");
        record.setTransactTime(LocalDateTime.now());
        record.setMqOffset(500L);
        record.setRecvTime(LocalDateTime.now());
        
        assertTrue("Negative tradePrice should return true (optional field)", record.isValid());
    }
    
    @Test
    public void testIsValid_MissingTradeVolume_ReturnsTrue() {
        // Missing tradeVolume is now allowed (optional field)
        record.setUnderlyingSecurityId("1021001");
        record.setUnderlyingSettlementType(1);
        record.setTradePrice(100.5);
        record.setTradeId("T20250101-001");
        record.setTransactTime(LocalDateTime.now());
        record.setMqOffset(500L);
        record.setRecvTime(LocalDateTime.now());
        
        assertTrue("Missing tradeVolume should return true (optional field)", record.isValid());
    }
    
    @Test
    public void testIsValid_ZeroTradeVolume_ReturnsTrue() {
        // Zero tradeVolume is now allowed (optional field)
        record.setUnderlyingSecurityId("1021001");
        record.setUnderlyingSettlementType(1);
        record.setTradePrice(100.5);
        record.setTradeVolume(0L);
        record.setTradeId("T20250101-001");
        record.setTransactTime(LocalDateTime.now());
        record.setMqOffset(500L);
        record.setRecvTime(LocalDateTime.now());
        
        assertTrue("Zero tradeVolume should return true (optional field)", record.isValid());
    }
    
    @Test
    public void testIsValid_NegativeTradeVolume_ReturnsTrue() {
        // Negative tradeVolume is now allowed (optional field)
        record.setUnderlyingSecurityId("1021001");
        record.setUnderlyingSettlementType(1);
        record.setTradePrice(100.5);
        record.setTradeVolume(-1000L);
        record.setTradeId("T20250101-001");
        record.setTransactTime(LocalDateTime.now());
        record.setMqOffset(500L);
        record.setRecvTime(LocalDateTime.now());
        
        assertTrue("Negative tradeVolume should return true (optional field)", record.isValid());
    }
    
    @Test
    public void testIsValid_MissingTradeId_ReturnsTrue() {
        // Missing tradeId is now allowed (optional field)
        record.setUnderlyingSecurityId("1021001");
        record.setUnderlyingSettlementType(1);
        record.setTradePrice(100.5);
        record.setTradeVolume(1000L);
        record.setTransactTime(LocalDateTime.now());
        record.setMqOffset(500L);
        record.setRecvTime(LocalDateTime.now());
        
        assertTrue("Missing tradeId should return true (optional field)", record.isValid());
    }
    
    @Test
    public void testIsValid_EmptyTradeId_ReturnsTrue() {
        // Empty tradeId is now allowed (optional field)
        record.setUnderlyingSecurityId("1021001");
        record.setUnderlyingSettlementType(1);
        record.setTradePrice(100.5);
        record.setTradeVolume(1000L);
        record.setTradeId("");
        record.setTransactTime(LocalDateTime.now());
        record.setMqOffset(500L);
        record.setRecvTime(LocalDateTime.now());
        
        assertTrue("Empty tradeId should return true (optional field)", record.isValid());
    }
    
    @Test
    public void testIsValid_MissingTransactTime_ReturnsFalse() {
        // Missing transactTime
        record.setUnderlyingSecurityId("1021001");
        record.setUnderlyingSettlementType(1);
        record.setTradePrice(100.5);
        record.setTradeVolume(1000L);
        record.setTradeId("T20250101-001");
        record.setMqOffset(500L);
        record.setRecvTime(LocalDateTime.now());
        
        assertFalse("Missing transactTime should return false", record.isValid());
    }
    
    @Test
    public void testIsValid_NullTransactTime_ReturnsFalse() {
        // Null transactTime
        record.setUnderlyingSecurityId("1021001");
        record.setUnderlyingSettlementType(1);
        record.setTradePrice(100.5);
        record.setTradeVolume(1000L);
        record.setTradeId("T20250101-001");
        record.setTransactTime(null);
        record.setMqOffset(500L);
        record.setRecvTime(LocalDateTime.now());
        
        assertFalse("Null transactTime should return false", record.isValid());
    }
    
    @Test
    public void testIsValid_MissingMqOffset_ReturnsTrue() {
        // Missing mqOffset is now allowed (optional field)
        record.setUnderlyingSecurityId("1021001");
        record.setUnderlyingSettlementType(1);
        record.setTradePrice(100.5);
        record.setTradeVolume(1000L);
        record.setTradeId("T20250101-001");
        record.setTransactTime(LocalDateTime.now());
        record.setRecvTime(LocalDateTime.now());
        
        assertTrue("Missing mqOffset should return true (optional field)", record.isValid());
    }
    
    @Test
    public void testIsValid_NegativeMqOffset_ReturnsTrue() {
        // Negative mqOffset is now allowed (optional field)
        record.setUnderlyingSecurityId("1021001");
        record.setUnderlyingSettlementType(1);
        record.setTradePrice(100.5);
        record.setTradeVolume(1000L);
        record.setTradeId("T20250101-001");
        record.setTransactTime(LocalDateTime.now());
        record.setMqOffset(-1L);
        record.setRecvTime(LocalDateTime.now());
        
        assertTrue("Negative mqOffset should return true (optional field)", record.isValid());
    }
    
    @Test
    public void testIsValid_MissingRecvTime_ReturnsTrue() {
        // Missing recvTime is now allowed (optional field)
        record.setUnderlyingSecurityId("1021001");
        record.setUnderlyingSettlementType(1);
        record.setTradePrice(100.5);
        record.setTradeVolume(1000L);
        record.setTradeId("T20250101-001");
        record.setTransactTime(LocalDateTime.now());
        record.setMqOffset(500L);
        
        assertTrue("Missing recvTime should return true (optional field)", record.isValid());
    }
    
    @Test
    public void testIsValid_NullRecvTime_ReturnsTrue() {
        // Null recvTime is now allowed (optional field)
        record.setUnderlyingSecurityId("1021001");
        record.setUnderlyingSettlementType(1);
        record.setTradePrice(100.5);
        record.setTradeVolume(1000L);
        record.setTradeId("T20250101-001");
        record.setTransactTime(LocalDateTime.now());
        record.setMqOffset(500L);
        record.setRecvTime(null);
        
        assertTrue("Null recvTime should return true (optional field)", record.isValid());
    }
    
    @Test
    public void testIsValid_ValidRecordWithAllFields_ReturnsTrue() {
        // Valid record with all optional fields populated
        record.setId(12345L);
        record.setUnderlyingSecurityId("1021001");
        record.setUnderlyingSettlementType(1);
        record.setTradePrice(100.5);
        record.setTradeYield(2.5);
        record.setTradeYieldType("YTM");
        record.setTradeVolume(1000L);
        record.setCounterparty("C001");
        record.setTradeId("T20250101-001");
        record.setTransactTime(LocalDateTime.of(2025, 1, 1, 10, 30, 0));
        record.setMqOffset(500L);
        record.setRecvTime(LocalDateTime.of(2025, 1, 1, 10, 30, 5));
        
        assertTrue("Valid record with all fields should return true", record.isValid());
    }
    
    @Test
    public void testIsValid_OptionalFieldsNull_ReturnsTrue() {
        // Valid record with only required fields
        record.setUnderlyingSecurityId("1021001");
        record.setUnderlyingSettlementType(1);
        record.setTradePrice(100.5);
        record.setTradeVolume(1000L);
        record.setTradeId("T20250101-001");
        record.setTransactTime(LocalDateTime.now());
        record.setMqOffset(500L);
        record.setRecvTime(LocalDateTime.now());
        
        // Optional fields remain null
        assertTrue("Record with only required fields should return true", record.isValid());
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
        LocalDateTime transactTime = LocalDateTime.of(2025, 1, 1, 10, 30, 0);
        LocalDateTime recvTime = LocalDateTime.of(2025, 1, 1, 10, 30, 5);
        
        RawTradeRecord record1 = new RawTradeRecord(12345L, "1021001", 1, 100.5, 2.5, "YTM", 1000L, "C001", "T20250101-001", transactTime, 500L, recvTime);
        RawTradeRecord record2 = new RawTradeRecord(12345L, "1021001", 1, 100.5, 2.5, "YTM", 1000L, "C001", "T20250101-001", transactTime, 500L, recvTime);
        
        assertTrue("Equal records should return true", record1.equals(record2));
    }
    
    @Test
    public void testEquals_DifferentIds_ReturnsFalse() {
        // Different id field
        LocalDateTime transactTime = LocalDateTime.of(2025, 1, 1, 10, 30, 0);
        LocalDateTime recvTime = LocalDateTime.of(2025, 1, 1, 10, 30, 5);
        
        RawTradeRecord record1 = new RawTradeRecord(12345L, "1021001", 1, 100.5, 2.5, "YTM", 1000L, "C001", "T20250101-001", transactTime, 500L, recvTime);
        RawTradeRecord record2 = new RawTradeRecord(67890L, "1021001", 1, 100.5, 2.5, "YTM", 1000L, "C001", "T20250101-001", transactTime, 500L, recvTime);
        
        assertFalse("Different ids should return false", record1.equals(record2));
    }
    
    @Test
    public void testEquals_DifferentUnderlyingSecurityId_ReturnsFalse() {
        // Different underlyingSecurityId field
        LocalDateTime transactTime = LocalDateTime.of(2025, 1, 1, 10, 30, 0);
        LocalDateTime recvTime = LocalDateTime.of(2025, 1, 1, 10, 30, 5);
        
        RawTradeRecord record1 = new RawTradeRecord(12345L, "1021001", 1, 100.5, 2.5, "YTM", 1000L, "C001", "T20250101-001", transactTime, 500L, recvTime);
        RawTradeRecord record2 = new RawTradeRecord(12345L, "1021002", 1, 100.5, 2.5, "YTM", 1000L, "C001", "T20250101-001", transactTime, 500L, recvTime);
        
        assertFalse("Different underlyingSecurityId should return false", record1.equals(record2));
    }
    
    @Test
    public void testHashCode_EqualRecords_SameHashCode() {
        // Two equal records should have same hashCode
        LocalDateTime transactTime = LocalDateTime.of(2025, 1, 1, 10, 30, 0);
        LocalDateTime recvTime = LocalDateTime.of(2025, 1, 1, 10, 30, 5);
        
        RawTradeRecord record1 = new RawTradeRecord(12345L, "1021001", 1, 100.5, 2.5, "YTM", 1000L, "C001", "T20250101-001", transactTime, 500L, recvTime);
        RawTradeRecord record2 = new RawTradeRecord(12345L, "1021001", 1, 100.5, 2.5, "YTM", 1000L, "C001", "T20250101-001", transactTime, 500L, recvTime);
        
        assertEquals("Equal records should have same hashCode", record1.hashCode(), record2.hashCode());
    }
    
    @Test
    public void testToString_ContainsExpectedFields() {
        // toString should include key fields
        LocalDateTime transactTime = LocalDateTime.of(2025, 1, 1, 10, 30, 0);
        LocalDateTime recvTime = LocalDateTime.of(2025, 1, 1, 10, 30, 5);
        
        RawTradeRecord record = new RawTradeRecord(12345L, "1021001", 1, 100.5, 2.5, "YTM", 1000L, "C001", "T20250101-001", transactTime, 500L, recvTime);
        String str = record.toString();
        
        assertTrue("toString should contain record id", str.contains("id=12345"));
        assertTrue("toString should contain underlyingSecurityId", str.contains("underlyingSecurityId='1021001'"));
        assertTrue("toString should contain underlyingSettlementType", str.contains("underlyingSettlementType=1"));
        assertTrue("toString should contain tradePrice", str.contains("tradePrice=100.5"));
        assertTrue("toString should contain tradeVolume", str.contains("tradeVolume=1000"));
    }
}