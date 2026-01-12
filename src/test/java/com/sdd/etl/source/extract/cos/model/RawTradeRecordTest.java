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
        record.setSetDays("T+1"); // "T+1" for T+1
        record.setNetPrice(100.5);
        record.setDealSize(1000L);
        record.setDealTime(LocalDateTime.of(2025, 1, 1, 10, 30, 0));
        record.setRecvTime(LocalDateTime.of(2025, 1, 1, 10, 30, 5));

        assertTrue("Valid record should return true", record.isValid());
    }

    @Test
    public void testIsValid_MissingUnderlyingSecurityId_ReturnsFalse() {
        // Missing underlyingSecurityId
        record.setSetDays("T+1");
        record.setNetPrice(100.5);
        record.setDealSize(1000L);
        record.setDealTime(LocalDateTime.now());
        record.setRecvTime(LocalDateTime.now());

        assertFalse("Missing underlyingSecurityId should return false", record.isValid());
    }

    @Test
    public void testIsValid_EmptyUnderlyingSecurityId_ReturnsFalse() {
        // Empty underlyingSecurityId
        record.setUnderlyingSecurityId("");
        record.setSetDays("T+1");
        record.setNetPrice(100.5);
        record.setDealSize(1000L);
        record.setDealTime(LocalDateTime.now());
        record.setRecvTime(LocalDateTime.now());

        assertFalse("Empty underlyingSecurityId should return false", record.isValid());
    }

    @Test
    public void testIsValid_WhitespaceUnderlyingSecurityId_ReturnsFalse() {
        // Whitespace-only underlyingSecurityId
        record.setUnderlyingSecurityId("   ");
        record.setSetDays("T+1");
        record.setNetPrice(100.5);
        record.setDealSize(1000L);
        record.setDealTime(LocalDateTime.now());
        record.setRecvTime(LocalDateTime.now());

        assertFalse("Whitespace-only underlyingSecurityId should return false", record.isValid());
    }

    @Test
    public void testIsValid_MissingDealTime_ReturnsFalse() {
        // Missing dealTime
        record.setUnderlyingSecurityId("1021001");

        assertFalse("Missing dealTime should return false", record.isValid());
    }

    @Test
    public void testIsValid_MissingNetPrice_ReturnsTrue() {
        // Missing netPrice is allowed (optional field)
        record.setUnderlyingSecurityId("1021001");
        record.setDealTime(LocalDateTime.now());

        assertTrue("Missing netPrice should return true (optional field)", record.isValid());
    }

    @Test
    public void testIsValid_ZeroNetPrice_ReturnsTrue() {
        // Zero netPrice is allowed (optional field)
        record.setUnderlyingSecurityId("1021001");
        record.setNetPrice(0.0);
        record.setDealTime(LocalDateTime.now());

        assertTrue("Zero netPrice should return true (optional field)", record.isValid());
    }

    @Test
    public void testIsValid_NegativeNetPrice_ReturnsTrue() {
        // Negative netPrice is allowed (optional field)
        record.setUnderlyingSecurityId("1021001");
        record.setNetPrice(-10.5);
        record.setDealTime(LocalDateTime.now());

        assertTrue("Negative netPrice should return true (optional field)", record.isValid());
    }

    @Test
    public void testIsValid_MissingDealSize_ReturnsTrue() {
        // Missing dealSize is allowed (optional field)
        record.setUnderlyingSecurityId("1021001");
        record.setDealTime(LocalDateTime.now());

        assertTrue("Missing dealSize should return true (optional field)", record.isValid());
    }

    @Test
    public void testIsValid_ZeroDealSize_ReturnsTrue() {
        // Zero dealSize is allowed (optional field)
        record.setUnderlyingSecurityId("1021001");
        record.setDealSize(0L);
        record.setDealTime(LocalDateTime.now());

        assertTrue("Zero dealSize should return true (optional field)", record.isValid());
    }

    @Test
    public void testIsValid_NegativeDealSize_ReturnsTrue() {
        // Negative dealSize is allowed (optional field)
        record.setUnderlyingSecurityId("1021001");
        record.setDealSize(-1000L);
        record.setDealTime(LocalDateTime.now());

        assertTrue("Negative dealSize should return true (optional field)", record.isValid());
    }

    @Test
    public void testIsValid_MissingRecvTime_ReturnsTrue() {
        // Missing recvTime is allowed (optional field)
        record.setUnderlyingSecurityId("1021001");
        record.setDealTime(LocalDateTime.now());

        assertTrue("Missing recvTime should return true (optional field)", record.isValid());
    }

    @Test
    public void testIsValid_NullDealTime_ReturnsFalse() {
        // Null dealTime
        record.setUnderlyingSecurityId("1021001");
        record.setDealTime(null);

        assertFalse("Null dealTime should return false", record.isValid());
    }

    @Test
    public void testIsValid_NullRecvTime_ReturnsTrue() {
        // Null recvTime is allowed (optional field)
        record.setUnderlyingSecurityId("1021001");
        record.setDealTime(LocalDateTime.now());
        record.setRecvTime(null);

        assertTrue("Null recvTime should return true (optional field)", record.isValid());
    }

    @Test
    public void testIsValid_ValidRecordWithAllFields_ReturnsTrue() {
        // Valid record with all optional fields populated
        record.setId(12345L);
        record.setUnderlyingSecurityId("1021001");
        record.setBondCode("1021001");
        record.setSymbol("1021001");
        record.setDealTime(LocalDateTime.of(2025, 1, 1, 10, 30, 0));
        record.setActDt("20250101");
        record.setActTm("103000");
        record.setPreMarket(0);
        record.setTradeMethod(3);
        record.setSide("C001");
        record.setNetPrice(100.5);
        record.setSetDays("T+1");
        record.setYield(2.5);
        record.setYieldType("YTM");
        record.setDealSize(1000L);
        record.setRecvTime(LocalDateTime.of(2025, 1, 1, 10, 30, 5));
        record.setHlid("4455380029616468");

        assertTrue("Valid record with all fields should return true", record.isValid());
    }

    @Test
    public void testIsValid_OptionalFieldsNull_ReturnsTrue() {
        // Valid record with only required fields
        record.setUnderlyingSecurityId("1021001");
        record.setDealTime(LocalDateTime.now());

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
        LocalDateTime dealTime = LocalDateTime.of(2025, 1, 1, 10, 30, 0);
        LocalDateTime recvTime = LocalDateTime.of(2025, 1, 1, 10, 30, 5);

        RawTradeRecord record1 = new RawTradeRecord();
        record1.setId(12345L);
        record1.setUnderlyingSecurityId("1021001");
        record1.setBondCode("1021001");
        record1.setSymbol("1021001");
        record1.setDealTime(dealTime);
        record1.setActDt("20250101");
        record1.setActTm("103000");
        record1.setPreMarket(0);
        record1.setTradeMethod(3);
        record1.setSide("C001");
        record1.setNetPrice(100.5);
        record1.setSetDays("T+1");
        record1.setYield(2.5);
        record1.setYieldType("YTM");
        record1.setDealSize(1000L);
        record1.setRecvTime(recvTime);
        record1.setHlid("4455380029616468");

        RawTradeRecord record2 = new RawTradeRecord();
        record2.setId(12345L);
        record2.setUnderlyingSecurityId("1021001");
        record2.setBondCode("1021001");
        record2.setSymbol("1021001");
        record2.setDealTime(dealTime);
        record2.setActDt("20250101");
        record2.setActTm("103000");
        record2.setPreMarket(0);
        record2.setTradeMethod(3);
        record2.setSide("C001");
        record2.setNetPrice(100.5);
        record2.setSetDays("T+1");
        record2.setYield(2.5);
        record2.setYieldType("YTM");
        record2.setDealSize(1000L);
        record2.setRecvTime(recvTime);
        record2.setHlid("4455380029616468");

        assertTrue("Equal records should return true", record1.equals(record2));
    }

    @Test
    public void testEquals_DifferentIds_ReturnsFalse() {
        // Different id field
        LocalDateTime dealTime = LocalDateTime.of(2025, 1, 1, 10, 30, 0);
        LocalDateTime recvTime = LocalDateTime.of(2025, 1, 1, 10, 30, 5);

        RawTradeRecord record1 = new RawTradeRecord();
        record1.setId(12345L);
        record1.setUnderlyingSecurityId("1021001");
        record1.setDealTime(dealTime);

        RawTradeRecord record2 = new RawTradeRecord();
        record2.setId(67890L);
        record2.setUnderlyingSecurityId("1021001");
        record2.setDealTime(dealTime);

        assertFalse("Different ids should return false", record1.equals(record2));
    }

    @Test
    public void testEquals_DifferentUnderlyingSecurityId_ReturnsFalse() {
        // Different underlyingSecurityId field
        LocalDateTime dealTime = LocalDateTime.of(2025, 1, 1, 10, 30, 0);

        RawTradeRecord record1 = new RawTradeRecord();
        record1.setId(12345L);
        record1.setUnderlyingSecurityId("1021001");
        record1.setDealTime(dealTime);

        RawTradeRecord record2 = new RawTradeRecord();
        record2.setId(12345L);
        record2.setUnderlyingSecurityId("1021002");
        record2.setDealTime(dealTime);

        assertFalse("Different underlyingSecurityId should return false", record1.equals(record2));
    }

    @Test
    public void testHashCode_EqualRecords_SameHashCode() {
        // Two equal records should have same hashCode
        LocalDateTime dealTime = LocalDateTime.of(2025, 1, 1, 10, 30, 0);
        LocalDateTime recvTime = LocalDateTime.of(2025, 1, 1, 10, 30, 5);

        RawTradeRecord record1 = new RawTradeRecord();
        record1.setId(12345L);
        record1.setUnderlyingSecurityId("1021001");
        record1.setBondCode("1021001");
        record1.setSymbol("1021001");
        record1.setDealTime(dealTime);
        record1.setActDt("20250101");
        record1.setActTm("103000");
        record1.setPreMarket(0);
        record1.setTradeMethod(3);
        record1.setSide("C001");
        record1.setNetPrice(100.5);
        record1.setSetDays("T+1");
        record1.setYield(2.5);
        record1.setYieldType("YTM");
        record1.setDealSize(1000L);
        record1.setRecvTime(recvTime);
        record1.setHlid("4455380029616468");

        RawTradeRecord record2 = new RawTradeRecord();
        record2.setId(12345L);
        record2.setUnderlyingSecurityId("1021001");
        record2.setBondCode("1021001");
        record2.setSymbol("1021001");
        record2.setDealTime(dealTime);
        record2.setActDt("20250101");
        record2.setActTm("103000");
        record2.setPreMarket(0);
        record2.setTradeMethod(3);
        record2.setSide("C001");
        record2.setNetPrice(100.5);
        record2.setSetDays("T+1");
        record2.setYield(2.5);
        record2.setYieldType("YTM");
        record2.setDealSize(1000L);
        record2.setRecvTime(recvTime);
        record2.setHlid("4455380029616468");

        assertEquals("Equal records should have same hashCode", record1.hashCode(), record2.hashCode());
    }

    @Test
    public void testToString_ContainsExpectedFields() {
        // toString should include key fields
        LocalDateTime dealTime = LocalDateTime.of(2025, 1, 1, 10, 30, 0);
        LocalDateTime recvTime = LocalDateTime.of(2025, 1, 1, 10, 30, 5);

        RawTradeRecord record = new RawTradeRecord();
        record.setId(12345L);
        record.setUnderlyingSecurityId("1021001");
        record.setDealTime(dealTime);

        String str = record.toString();

        assertTrue("toString should contain record id", str.contains("id=12345"));
        assertTrue("toString should contain underlyingSecurityId", str.contains("underlyingSecurityId='1021001'"));
    }
}