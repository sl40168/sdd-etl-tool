package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.context.ContextManager;
import com.sdd.etl.context.ETLContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Integration tests for multi-source extraction API.
 * Uses a mock concrete ExtractSubprocess implementation.
 */
public class ExtractSubprocessIntegrationTest {

    private ETConfiguration config;
    private ETLContext context;

    @Before
    public void setUp() {
        config = new ETConfiguration();
        context = ContextManager.createContext("20250101", config);
    }

    @Test
    public void testMultiSourceExtraction_Success() throws ETLException {
        // Given 3 sources
        ETConfiguration.SourceConfig s1 = new ETConfiguration.SourceConfig();
        s1.setName("s1");
        s1.setType("JDBC");
        s1.setConnectionString("jdbc:mysql://localhost:3306/db1");
        s1.setPrimaryKeyField("id");

        ETConfiguration.SourceConfig s2 = new ETConfiguration.SourceConfig();
        s2.setName("s2");
        s2.setType("JDBC");
        s2.setConnectionString("jdbc:mysql://localhost:3306/db2");
        s2.setPrimaryKeyField("id");

        ETConfiguration.SourceConfig s3 = new ETConfiguration.SourceConfig();
        s3.setName("s3");
        s3.setType("JDBC");
        s3.setConnectionString("jdbc:mysql://localhost:3306/db3");
        s3.setPrimaryKeyField("id");

        config.addSource(s1);
        config.addSource(s2);
        config.addSource(s3);

        ExtractSubprocess extract = new ExtractSubprocess() {
            @Override
            public int execute(ETLContext context) throws ETLException {
                // Simulate extracting from all sources
                int total = context.getConfig().getSources().size() * 10;
                context.setExtractedDataCount(total);
                context.setExtractedData(new Object());
                return total;
            }
        };

        // When
        extract.validateContext(context);
        int count = extract.execute(context);

        // Then
        assertEquals("Total extracted count should be 30", 30, count);
        assertEquals("Context extracted count should be 30", 30, context.getExtractedDataCount());
        assertNotNull("Extracted data should be set", context.getExtractedData());
    }
}
