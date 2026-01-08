package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.context.ContextManager;
import com.sdd.etl.context.ETLContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for LoadSubprocess API.
 */
public class LoadSubprocessTest {

    private ETConfiguration config;
    private ETLContext context;

    @Before
    public void setUp() {
        config = new ETConfiguration();
        context = ContextManager.createContext("20250101", config);
    }

    @Test
    public void testValidateContext_TransformedDataMissing_Throws() {
        LoadSubprocess load = new LoadSubprocess() {
            @Override
            public int execute(ETLContext context) throws ETLException {
                return 0;
            }
        };

        // transformedData is null by default
        try {
            load.validateContext(context);
            fail("Expected ETLException");
        } catch (ETLException e) {
            assertTrue(e.getMessage().contains("No transformed data found"));
        }
    }

    @Test
    public void testValidateContext_TargetsEmpty_Throws() {
        LoadSubprocess load = new LoadSubprocess() {
            @Override
            public int execute(ETLContext context) throws ETLException {
                return 0;
            }
        };

        context.setTransformedData(new Object());

        // targets list empty by default
        try {
            load.validateContext(context);
            fail("Expected ETLException");
        } catch (ETLException e) {
            assertTrue(e.getMessage().contains("No data targets configured"));
        }
    }

    @Test
    public void testGetType_ReturnsLoad() {
        LoadSubprocess load = new LoadSubprocess() {
            @Override
            public int execute(ETLContext context) throws ETLException {
                return 0;
            }
        };

        assertEquals("Type should be LOAD", com.sdd.etl.context.SubprocessType.LOAD, load.getType());
    }

    @Test
    public void testImplementsSubprocessInterface() {
        LoadSubprocess load = new LoadSubprocess() {
            @Override
            public int execute(ETLContext context) throws ETLException {
                return 0;
            }
        };

        assertTrue("LoadSubprocess should implement SubprocessInterface", load instanceof SubprocessInterface);
    }
}
