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

        assertFalse("SourceConfig should be invalid when name is missing", source.isValid());
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

    @Test
    public void testFindSourceConfig_ReturnsMatchingConfig() {
        ETConfiguration config = new ETConfiguration();
        ETConfiguration.SourceConfig cosSource = new ETConfiguration.SourceConfig();
        cosSource.setName("cos1");
        cosSource.setType("cos");
        cosSource.setConnectionString("cos://");
        cosSource.setPrimaryKeyField("id");
        config.addSource(cosSource);

        ETConfiguration.SourceConfig jdbcSource = new ETConfiguration.SourceConfig();
        jdbcSource.setName("jdbc1");
        jdbcSource.setType("JDBC");
        jdbcSource.setConnectionString("jdbc:mysql://");
        jdbcSource.setPrimaryKeyField("id");
        config.addSource(jdbcSource);

        ETConfiguration.SourceConfig found = config.findSourceConfig("cos");
        assertNotNull(found);
        assertEquals("cos1", found.getName());
        assertEquals("cos", found.getType());
    }

    @Test
    public void testFindSourceConfig_ReturnsNullWhenNoMatch() {
        ETConfiguration config = new ETConfiguration();
        config.addSource(new ETConfiguration.SourceConfig());

        ETConfiguration.SourceConfig found = config.findSourceConfig("unknown");
        assertNull(found);
    }

    @Test
    public void testFindSourceConfig_ReturnsNullWhenSourcesNull() {
        ETConfiguration config = new ETConfiguration();
        // sources is initialized as empty list, not null
        config.setSources(null);

        ETConfiguration.SourceConfig found = config.findSourceConfig("cos");
        assertNull(found);
    }

    @Test
    public void testFindSourceConfigByCategory_ReturnsMatchingConfig() {
        ETConfiguration config = new ETConfiguration();
        ETConfiguration.SourceConfig cosSource1 = new ETConfiguration.SourceConfig();
        cosSource1.setName("cos1");
        cosSource1.setType("cos");
        cosSource1.setConnectionString("cos://");
        cosSource1.setPrimaryKeyField("id");
        cosSource1.setProperty("category", "AllPriceDepth");
        config.addSource(cosSource1);

        ETConfiguration.SourceConfig cosSource2 = new ETConfiguration.SourceConfig();
        cosSource2.setName("cos2");
        cosSource2.setType("cos");
        cosSource2.setConnectionString("cos://");
        cosSource2.setPrimaryKeyField("id");
        cosSource2.setProperty("category", "OtherCategory");
        config.addSource(cosSource2);

        ETConfiguration.SourceConfig found = config.findSourceConfigByCategory("cos", "AllPriceDepth");
        assertNotNull(found);
        assertEquals("cos1", found.getName());
        assertEquals("AllPriceDepth", found.getProperty("category"));
    }

    @Test
    public void testFindSourceConfigByCategory_ReturnsNullWhenNoMatch() {
        ETConfiguration config = new ETConfiguration();
        ETConfiguration.SourceConfig cosSource = new ETConfiguration.SourceConfig();
        cosSource.setName("cos1");
        cosSource.setType("cos");
        cosSource.setConnectionString("cos://");
        cosSource.setPrimaryKeyField("id");
        cosSource.setProperty("category", "AllPriceDepth");
        config.addSource(cosSource);

        ETConfiguration.SourceConfig found = config.findSourceConfigByCategory("cos", "UnknownCategory");
        assertNull(found);
    }

    @Test
    public void testFindSourceConfigByCategory_ReturnsNullWhenCategoryNull() {
        ETConfiguration config = new ETConfiguration();
        ETConfiguration.SourceConfig cosSource = new ETConfiguration.SourceConfig();
        cosSource.setName("cos1");
        cosSource.setType("cos");
        cosSource.setConnectionString("cos://");
        cosSource.setPrimaryKeyField("id");
        cosSource.setProperty("category", "AllPriceDepth");
        config.addSource(cosSource);

        ETConfiguration.SourceConfig found = config.findSourceConfigByCategory("cos", null);
        assertNull(found);
    }
}