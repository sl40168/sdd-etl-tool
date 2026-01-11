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

        // Mock Columns
        when(resultSet.getInt("trading_date")).thenReturn(20231201);
        when(resultSet.getString("code")).thenReturn("IF2403");
        when(resultSet.getDouble("price")).thenReturn(3500.0);
        when(resultSet.getInt("action_date")).thenReturn(20231201);
        when(resultSet.getInt("action_time")).thenReturn(93000500); // 09:30:00.500
        when(resultSet.getString("receive_time")).thenReturn("20231201 093000.500");
        when(resultSet.getString("bid_prices")).thenReturn("[3499.0, 0, 0, 0, 0]");

        // Init Extractor
        extractor.setup(context);

        // Execute
        List<SourceDataModel> updates = extractor.extract(context);

        // Verify
        assertEquals(1, updates.size());
        BondFutureQuoteDataModel model = (BondFutureQuoteDataModel) updates.get(0);

        assertEquals("2023.12.01", model.getBusinessDate());
        assertEquals("IF2403", model.getExchProductId());
        assertEquals(3500.0, model.getLastTradePrice(), 0.001);
        assertEquals(3499.0, model.getBid1Price(), 0.001);

        // Check timestamps
        assertNotNull(model.getEventTime());
        assertNotNull(model.getReceiveTime());
    }

    @Test(expected = ETLException.class)
    public void testExtractFailureNoConfig() throws Exception {
        when(config.findSourceConfigByCategory(anyString(), anyString())).thenReturn(null);
        when(config.findSourceConfig(anyString())).thenReturn(null);
        extractor.setup(context);
    }
}
