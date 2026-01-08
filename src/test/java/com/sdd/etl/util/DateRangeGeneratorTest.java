package com.sdd.etl.util;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.List;

/**
 * Unit tests for DateRangeGenerator.
 */
public class DateRangeGeneratorTest {

    @Test
    public void testGenerate_SameDay() {
        List<String> dates = DateRangeGenerator.generate("20250101", "20250101");
        assertEquals("Single day should return one date", 1, dates.size());
        assertEquals("Date should match input", "20250101", dates.get(0));
    }

    @Test
    public void testGenerate_MultipleDays() {
        List<String> dates = DateRangeGenerator.generate("20250101", "20250103");
        assertEquals("Three days should return three dates", 3, dates.size());
        assertEquals("First date should be start date", "20250101", dates.get(0));
        assertEquals("Second date should be next day", "20250102", dates.get(1));
        assertEquals("Third date should be end date", "20250103", dates.get(2));
    }

    @Test
    public void testGenerate_LeapYear() {
        List<String> dates = DateRangeGenerator.generate("20240228", "20240301");
        assertEquals("Three days including leap day should return three dates", 3, dates.size());
        assertEquals("First date should be February 28", "20240228", dates.get(0));
        assertEquals("Second date should be February 29", "20240229", dates.get(1));
        assertEquals("Third date should be March 1", "20240301", dates.get(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerate_InvalidFromDate() {
        DateRangeGenerator.generate("invalid", "20250101");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerate_InvalidToDate() {
        DateRangeGenerator.generate("20250101", "invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerate_FromAfterTo() {
        DateRangeGenerator.generate("20250102", "20250101");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerate_NullFromDate() {
        DateRangeGenerator.generate(null, "20250101");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerate_NullToDate() {
        DateRangeGenerator.generate("20250101", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerate_EmptyFromDate() {
        DateRangeGenerator.generate("", "20250101");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerate_EmptyToDate() {
        DateRangeGenerator.generate("20250101", "");
    }
}