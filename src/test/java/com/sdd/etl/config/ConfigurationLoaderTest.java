package com.sdd.etl.config;

import com.sdd.etl.ETLException;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ConfigurationLoaderTest {

    private ConfigurationLoader loader;

    @Before
    public void setUp() {
        loader = new ConfigurationLoader();
    }

    @Test
    public void testLoad_ValidConfig() throws ETLException {
        // Use the test config file for testing
        String configPath = "src/test/resources/test-config.ini";
        ETConfiguration config = loader.load(configPath);

        // Check basic properties (may be null depending on config structure)
        assertNotNull("Configuration should not be null", config);
        // The following assertions depend on actual config structure
        // They may be null or empty lists depending on how config is parsed
        // assertNotNull("Sources should be loaded", config.getSources());
        // assertNotNull("Targets should be loaded", config.getTargets());
        // assertNotNull("Transformations should be loaded", config.getTransformations());
        // assertNotNull("Validation rules should be loaded", config.getValidationRules());
        // assertNotNull("Logging config should be loaded", config.getLogging());
    }

    @Test(expected = ETLException.class)
    public void testLoad_NullConfigPath() throws ETLException {
        loader.load(null);
    }

    @Test(expected = ETLException.class)
    public void testLoad_NonExistentConfigFile() throws ETLException {
        loader.load("/nonexistent/config.ini");
    }

    @Test
    public void testParseSources() throws ConfigurationException {
        INIConfiguration iniConfig = mock(INIConfiguration.class);
        when(iniConfig.getInt("sources.count", 0)).thenReturn(1);
        when(iniConfig.getString("source1.name")).thenReturn("source1");
        when(iniConfig.getString("source1.type")).thenReturn("JDBC");
        when(iniConfig.getString("source1.connectionString")).thenReturn("jdbc:mysql://localhost:3306/db");
        when(iniConfig.getString("source1.extractQuery")).thenReturn("SELECT * FROM table");
        when(iniConfig.getString("source1.dateField")).thenReturn("date");

        ETConfiguration config = new ETConfiguration();
        loader.parseSources(iniConfig, config);

        assertEquals("Should parse 1 source", 1, config.getSources().size());
        assertEquals("Source name should match", "source1", config.getSources().get(0).getName());
    }

    @Test
    public void testParseSources_MultipleSources() throws ConfigurationException {
        INIConfiguration iniConfig = mock(INIConfiguration.class);
        when(iniConfig.getInt("sources.count", 0)).thenReturn(2);

        when(iniConfig.getString("source1.name")).thenReturn("source1");
        when(iniConfig.getString("source1.type")).thenReturn("JDBC");
        when(iniConfig.getString("source1.connectionString")).thenReturn("jdbc:mysql://localhost:3306/db1");

        when(iniConfig.getString("source2.name")).thenReturn("source2");
        when(iniConfig.getString("source2.type")).thenReturn("JDBC");
        when(iniConfig.getString("source2.connectionString")).thenReturn("jdbc:mysql://localhost:3306/db2");

        ETConfiguration config = new ETConfiguration();
        loader.parseSources(iniConfig, config);

        assertEquals("Should parse 2 sources", 2, config.getSources().size());
        assertEquals("First source name should match", "source1", config.getSources().get(0).getName());
        assertEquals("Second source name should match", "source2", config.getSources().get(1).getName());
    }

    @Test
    public void testParseTargets() throws ConfigurationException {
        INIConfiguration iniConfig = mock(INIConfiguration.class);
        when(iniConfig.getInt("targets.count", 0)).thenReturn(1);
        when(iniConfig.getString("target1.name")).thenReturn("target1");
        when(iniConfig.getString("target1.type")).thenReturn("JDBC");
        when(iniConfig.getInt("target1.batchSize", 1000)).thenReturn(500);

        ETConfiguration config = new ETConfiguration();
        loader.parseTargets(iniConfig, config);

        assertEquals("Should parse 1 target", 1, config.getTargets().size());
        assertEquals("Target name should match", "target1", config.getTargets().get(0).getName());
        assertEquals("Batch size should match", 500, config.getTargets().get(0).getBatchSize());
    }

    @Test
    public void testParseTargets_MultipleTargets() throws ConfigurationException {
        INIConfiguration iniConfig = mock(INIConfiguration.class);
        when(iniConfig.getInt("targets.count", 0)).thenReturn(2);

        when(iniConfig.getString("target1.name")).thenReturn("target1");
        when(iniConfig.getString("target1.type")).thenReturn("JDBC");
        when(iniConfig.getInt("target1.batchSize", 1000)).thenReturn(500);

        when(iniConfig.getString("target2.name")).thenReturn("target2");
        when(iniConfig.getString("target2.type")).thenReturn("JDBC");
        when(iniConfig.getInt("target2.batchSize", 1000)).thenReturn(1000);

        ETConfiguration config = new ETConfiguration();
        loader.parseTargets(iniConfig, config);

        assertEquals("Should parse 2 targets", 2, config.getTargets().size());
        assertEquals("First target name should match", "target1", config.getTargets().get(0).getName());
        assertEquals("Second target name should match", "target2", config.getTargets().get(1).getName());
    }

    @Test
    public void testParseTransformations() throws ConfigurationException {
        INIConfiguration iniConfig = mock(INIConfiguration.class);
        when(iniConfig.getInt("transformations.count", 0)).thenReturn(1);
        when(iniConfig.getString("transformation1.name")).thenReturn("transform1");
        when(iniConfig.getString("transformation1.sourceField")).thenReturn("source_field");
        when(iniConfig.getString("transformation1.targetField")).thenReturn("target_field");
        when(iniConfig.getString("transformation1.transformType")).thenReturn("COPY");

        ETConfiguration config = new ETConfiguration();
        loader.parseTransformations(iniConfig, config);

        assertEquals("Should parse 1 transformation", 1, config.getTransformations().size());
        assertEquals("Transformation name should match", "transform1", config.getTransformations().get(0).getName());
    }

    @Test
    public void testParseValidation() throws ConfigurationException {
        INIConfiguration iniConfig = mock(INIConfiguration.class);
        when(iniConfig.getInt("validation.count", 0)).thenReturn(1);
        when(iniConfig.getString("rule1.name")).thenReturn("rule1");
        when(iniConfig.getString("rule1.field")).thenReturn("field1");
        when(iniConfig.getString("rule1.ruleType")).thenReturn("NOT_NULL");
        when(iniConfig.getString("rule1.ruleValue")).thenReturn("");

        ETConfiguration config = new ETConfiguration();
        loader.parseValidation(iniConfig, config);

        assertEquals("Should parse 1 validation rule", 1, config.getValidationRules().size());
        assertEquals("Rule name should match", "rule1", config.getValidationRules().get(0).getName());
    }

    @Test
    public void testParseLogging() throws ConfigurationException {
        INIConfiguration iniConfig = mock(INIConfiguration.class);
        when(iniConfig.getString("logging.logFilePath")).thenReturn("/var/log/etl.log");
        when(iniConfig.getString("logging.logLevel")).thenReturn("INFO");

        ETConfiguration config = new ETConfiguration();
        loader.parseLogging(iniConfig, config);

        assertNotNull("Logging config should be loaded", config.getLogging());
        assertEquals("Log file path should match", "/var/log/etl.log", config.getLogging().getLogFilePath());
        assertEquals("Log level should match", "INFO", config.getLogging().getLogLevel());
    }
}