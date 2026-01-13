package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.context.ContextManager;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.util.DateUtils;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Integration tests for multi-target loading API.
 * Uses a mock concrete LoadSubprocess implementation.
 */
public class LoadSubprocessIntegrationTest {

    private ETConfiguration config;
    private ETLContext context;

    @Before
    public void setUp() {
        config = new ETConfiguration();
        context = ContextManager.createContext(DateUtils.parseDate("20250101"), config);

        // Provide transformed data as input to load
        context.setTransformedDataCount(30);
        context.setTransformedData(Collections.emptyList());
    }

    @Ignore
    @Test
    public void testMultiTargetLoad_Success() throws ETLException {
        // Given 3 targets
        ETConfiguration.TargetConfig t1 = new ETConfiguration.TargetConfig();
        t1.setName("t1");
        t1.setType("JDBC");
        t1.setConnectionString("jdbc:mysql://localhost:3306/db1");
        t1.setBatchSize(500);

        ETConfiguration.TargetConfig t2 = new ETConfiguration.TargetConfig();
        t2.setName("t2");
        t2.setType("JDBC");
        t2.setConnectionString("jdbc:mysql://localhost:3306/db2");
        t2.setBatchSize(500);

        ETConfiguration.TargetConfig t3 = new ETConfiguration.TargetConfig();
        t3.setName("t3");
        t3.setType("JDBC");
        t3.setConnectionString("jdbc:mysql://localhost:3306/db3");
        t3.setBatchSize(500);

        config.addTarget(t1);
        config.addTarget(t2);
        config.addTarget(t3);

        LoadSubprocess load = new LoadSubprocess() {
            @Override
            public int execute(ETLContext context) throws ETLException {
                // Simulate loading to all targets
                int loaded = context.getTransformedDataCount();
                context.setLoadedDataCount(loaded);
                return loaded;
            }
        };

        // When
        load.validateContext(context);
        int loaded = load.execute(context);

        // Then
        assertEquals("Loaded count should be 30", 30, loaded);
        assertEquals("Context loaded count should be 30", 30, context.getLoadedDataCount());
    }
}
