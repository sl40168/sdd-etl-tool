package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.context.ContextManager;
import com.sdd.etl.context.ETLContext;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;

/**
 * Unit tests for ExtractSubprocess API.
 */
public class ExtractSubprocessTest {

    private ETConfiguration config;
    private ETLContext context;

    @Before
    public void setUp() {
        config = new ETConfiguration();
        context = ContextManager.createContext("20250101", config);
    }

    @Test
    public void testValidateContext_ConfigNull_Throws() {
        ExtractSubprocess extract = new ExtractSubprocess() {
            @Override
            public int execute(ETLContext context) throws ETLException {
                return 0;
            }
        };

        ETLContext badContext = new ETLContext();
        badContext.setCurrentDate("20250101");
        // config not set

        try {
            extract.validateContext(badContext);
            fail("Expected ETLException");
        } catch (ETLException e) {
            assertTrue(e.getMessage().contains("Configuration is null"));
        }
    }

    @Test
    public void testValidateContext_SourcesEmpty_Throws() {
        ExtractSubprocess extract = new ExtractSubprocess() {
            @Override
            public int execute(ETLContext context) throws ETLException {
                return 0;
            }
        };

        // Sources list empty by default
        try {
            extract.validateContext(context);
            fail("Expected ETLException");
        } catch (ETLException e) {
            assertTrue(e.getMessage().contains("No data sources configured"));
        }
    }

    @Test
    public void testGetType_ReturnsExtract() {
        ExtractSubprocess extract = new ExtractSubprocess() {
            @Override
            public int execute(ETLContext context) throws ETLException {
                return 0;
            }
        };

        assertEquals("Type should be EXTRACT", com.sdd.etl.context.SubprocessType.EXTRACT, extract.getType());
    }

    @Test
    public void testImplementsSubprocessInterface() {
        ExtractSubprocess extract = new ExtractSubprocess() {
            @Override
            public int execute(ETLContext context) throws ETLException {
                return 0;
            }
        };

        assertTrue("ExtractSubprocess should implement SubprocessInterface", extract instanceof SubprocessInterface);
    }
}
