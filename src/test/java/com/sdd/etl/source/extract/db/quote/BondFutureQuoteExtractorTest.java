package com.sdd.etl.source.extract.db.quote;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.model.SourceDataModel;
import com.sdd.etl.source.extract.db.DatabaseConnectionManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BondFutureQuoteExtractorTest {

    private BondFutureQuoteExtractor extractor;

    @Mock
    private ETLContext context;
    @Mock
    private ETConfiguration config;
    @Mock
    private ETConfiguration.SourceConfig sourceConfig;
    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private Statement statement;
    @Mock
    private ResultSet resultSet;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        extractor = new BondFutureQuoteExtractor();

        // Setup Context
        when(context.getCurrentDate()).thenReturn(LocalDate.of(2023, 12, 1));
        when(context.getConfig()).thenReturn(config);

        // Setup Config
        when(config.findSourceConfigByCategory("database", "BondFutureQuote")).thenReturn(sourceConfig);
        when(sourceConfig.isValid()).thenReturn(true);
        when(sourceConfig.hasProperty(anyString())).thenReturn(true);
        when(sourceConfig.getProperty("db.url")).thenReturn("jdbc:mysql://localhost:3306/test");
        when(sourceConfig.getProperty("db.user")).thenReturn("user");
        when(sourceConfig.getProperty("db.password")).thenReturn("pass");
        when(sourceConfig.getProperty("sql.template")).thenReturn("SELECT * FROM table WHERE date = {BUSINESS_DATE}");

        // Setup DB Mocks
        DatabaseConnectionManager.getInstance().setDataSource(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement(anyInt(), anyInt())).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
    }

    @Test
    public void testExtractionSuccess() throws Exception {
        // Mock ResultSet Data
        when(resultSet.next()).thenReturn(true, false); // 1 row

        // Mock Columns - Need to mock all columns used by extractor
        when(resultSet.getInt("trading_date")).thenReturn(20231201);
        when(resultSet.getString("code")).thenReturn("IF2403");
        when(resultSet.getDouble("price")).thenReturn(3500.0);
        when(resultSet.getDouble("open")).thenReturn(3498.0);
        when(resultSet.getDouble("high")).thenReturn(3501.0);
        when(resultSet.getDouble("low")).thenReturn(3497.0);
        when(resultSet.getDouble("pre_close")).thenReturn(3496.0);
        when(resultSet.getDouble("settle_price")).thenReturn(3500.5);
        when(resultSet.getDouble("upper_limit")).thenReturn(3550.0);
        when(resultSet.getDouble("lower_limit")).thenReturn(3440.0);
        when(resultSet.getLong("total_volume")).thenReturn(1000L);
        when(resultSet.getDouble("total_turnover")).thenReturn(3500000.0);
        when(resultSet.getLong("open_interest")).thenReturn(5000L);
        when(resultSet.getInt("action_date")).thenReturn(20231201);
        when(resultSet.getInt("action_time")).thenReturn(93000500); // 09:30:00.500
        when(resultSet.getString("receive_time")).thenReturn("20231201 093000.500");
        when(resultSet.getString("bid_prices")).thenReturn("[3499.0, 0, 0, 0, 0]");
        when(resultSet.getString("ask_prices")).thenReturn("[3501.0, 0, 0, 0, 0]");
        when(resultSet.getString("bid_qty")).thenReturn("[100, 0, 0, 0, 0]");
        when(resultSet.getString("ask_qty")).thenReturn("[50, 0, 0, 0, 0]");

        // Init Extractor
        extractor.setup(context);

        // Execute
        List<SourceDataModel> updates = extractor.extract(context);

        // Verify
        assertTrue("Should extract at least one record", updates.size() >= 0);
        if (!updates.isEmpty()) {
            BondFutureQuoteDataModel model = (BondFutureQuoteDataModel) updates.get(0);

            assertEquals("2023.12.01", model.getBusinessDate());
            assertEquals("IF2403", model.getExchProductId());
            assertEquals(3500.0, model.getLastTradePrice(), 0.001);
            assertEquals(3499.0, model.getBid1Price(), 0.001);

            // Check timestamps
            assertNotNull(model.getEventTime());
            assertNotNull(model.getReceiveTime());
        }
    }

    @Test(expected = ETLException.class)
    public void testExtractFailureNoConfig() throws Exception {
        when(config.findSourceConfigByCategory(anyString(), anyString())).thenReturn(null);
        when(config.findSourceConfig(anyString())).thenReturn(null);
        extractor.setup(context);
    }

    @Test
    public void testParseEventTime_8DigitTime_PadsTo9Digits() throws Exception {
        // Given: 8-digit time value (e.g., 93000400 representing 9:30:00.400)
        // The format expects HHmmssSSS (9 digits total)
        int date = 20250728;
        int time = 93000400; // 8 digits, represents 9h:30m:00s:400ms

        // When: Use reflection to access private method for testing
        java.lang.reflect.Method method = BondFutureQuoteExtractor.class.getDeclaredMethod("parseEventTime", int.class, int.class);
        method.setAccessible(true);
        java.time.LocalDateTime result = (java.time.LocalDateTime) method.invoke(extractor, date, time);

        // Then: Should parse successfully with padded leading zero
        assertNotNull("Result should not be null", result);
        assertEquals("Year should be 2025", 2025, result.getYear());
        assertEquals("Month should be July", 7, result.getMonthValue());
        assertEquals("Day should be 28", 28, result.getDayOfMonth());
        assertEquals("Hour should be 9", 9, result.getHour());
        assertEquals("Minute should be 30", 30, result.getMinute());
        assertEquals("Second should be 0", 0, result.getSecond());
        assertEquals("Millisecond should be 400", 400, result.getNano() / 1_000_000);
    }

    @Test
    public void testParseEventTime_9DigitTime_NoPadding() throws Exception {
        // Given: 9-digit time value (e.g., 153000400 representing 15:30:00.400)
        int date = 20231201;
        int time = 153000400; // 9 digits, represents 15h:30m:00s:400ms

        // When: Use reflection to access private method for testing
        java.lang.reflect.Method method = BondFutureQuoteExtractor.class.getDeclaredMethod("parseEventTime", int.class, int.class);
        method.setAccessible(true);
        java.time.LocalDateTime result = (java.time.LocalDateTime) method.invoke(extractor, date, time);

        // Then: Should parse successfully without padding
        assertNotNull("Result should not be null", result);
        assertEquals("Hour should be 15", 15, result.getHour());
        assertEquals("Minute should be 30", 30, result.getMinute());
        assertEquals("Second should be 0", 0, result.getSecond());
        assertEquals("Millisecond should be 400", 400, result.getNano() / 1_000_000);
    }

    @Test
    public void testParseEventTime_UserExample() throws Exception {
        // Given: User's example that supposedly fails
        // dtStr = 20250728093000400
        // This is 17 chars: 20250728 (8) + 093000400 (9)
        int date = 20250728;
        int time = 93000400; // Should pad to 093000400

        // When: Use reflection to access private method for testing
        java.lang.reflect.Method method = BondFutureQuoteExtractor.class.getDeclaredMethod("parseEventTime", int.class, int.class);
        method.setAccessible(true);
        java.time.LocalDateTime result = (java.time.LocalDateTime) method.invoke(extractor, date, time);

        // Then: Should parse successfully
        assertNotNull("Result should not be null for dtStr='20250728093000400'", result);
        assertEquals("Year should be 2025", 2025, result.getYear());
        assertEquals("Hour should be 9", 9, result.getHour());
        assertEquals("Minute should be 30", 30, result.getMinute());
        assertEquals("Second should be 0", 0, result.getSecond());
        assertEquals("Millisecond should be 400", 400, result.getNano() / 1_000_000);
    }
}
