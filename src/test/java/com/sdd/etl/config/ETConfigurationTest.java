package com.sdd.etl.config;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for ETConfiguration and its nested config classes.
 */
public class ETConfigurationTest {

    @Test
    public void testSourceConfig_RequiredFieldsValidation() {
        ETConfiguration.SourceConfig source = new ETConfiguration.SourceConfig();

        // Initially invalid
        assertFalse("SourceConfig should be invalid when required fields are missing", source.isValid());

        // Set required fields
        source.setName("source1");
        source.setType("JDBC");
        source.setConnectionString("jdbc:mysql://localhost:3306/db");
        source.setPrimaryKeyField("id");

        assertTrue("SourceConfig should be valid when required fields are set", source.isValid());
    }

    @Test
    public void testSourceConfig_MissingName_IsInvalid() {
        ETConfiguration.SourceConfig source = new ETConfiguration.SourceConfig();
        source.setType("JDBC");
        source.setConnectionString("jdbc:mysql://localhost:3306/db");
        source.setPrimaryKeyField("id");

        assertFalse("SourceConfig should be invalid when name is missing", source.isValid());
    }

    @Test
    public void testSourceConfig_MissingType_IsInvalid() {
        ETConfiguration.SourceConfig source = new ETConfiguration.SourceConfig();
        source.setName("source1");
        source.setConnectionString("jdbc:mysql://localhost:3306/db");
        source.setPrimaryKeyField("id");

        assertFalse("SourceConfig should be invalid when type is missing", source.isValid());
    }

    @Test
    public void testSourceConfig_MissingConnectionString_IsInvalid() {
        ETConfiguration.SourceConfig source = new ETConfiguration.SourceConfig();
        source.setName("source1");
        source.setType("JDBC");
        source.setPrimaryKeyField("id");

        assertFalse("SourceConfig should be invalid when connectionString is missing", source.isValid());
    }

    @Test
    public void testSourceConfig_MissingPrimaryKeyField_IsInvalid() {
        ETConfiguration.SourceConfig source = new ETConfiguration.SourceConfig();
        source.setName("source1");
        source.setType("JDBC");
        source.setConnectionString("jdbc:mysql://localhost:3306/db");

        assertFalse("SourceConfig should be invalid when primaryKeyField is missing", source.isValid());
    }

    @Test
    public void testTargetConfig_RequiredFieldsValidation() {
        ETConfiguration.TargetConfig target = new ETConfiguration.TargetConfig();

        // Initially invalid
        assertFalse("TargetConfig should be invalid when required fields are missing", target.isValid());

        // Set required fields
        target.setName("target1");
        target.setType("JDBC");
        target.setConnectionString("jdbc:mysql://localhost:3306/db");
        target.setBatchSize(500);

        assertTrue("TargetConfig should be valid when required fields are set", target.isValid());
    }

    @Test
    public void testTargetConfig_MissingBatchSize_IsInvalid() {
        ETConfiguration.TargetConfig target = new ETConfiguration.TargetConfig();
        target.setName("target1");
        target.setType("JDBC");
        target.setConnectionString("jdbc:mysql://localhost:3306/db");
        target.setBatchSize(0);

        assertFalse("TargetConfig should be invalid when batchSize <= 0", target.isValid());
    }
}

