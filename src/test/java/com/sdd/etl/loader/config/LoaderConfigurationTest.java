package com.sdd.etl.loader.config;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for LoaderConfiguration.
 */
public class LoaderConfigurationTest {

    @Test
    public void testDefaultValues() {
        LoaderConfiguration config = new LoaderConfiguration();

        assertEquals("Default host should be localhost", "localhost", config.getHost());
        assertEquals("Default port should be 8848", 8848, config.getPort());
        assertEquals("Default username should be empty", "", config.getUsername());
        assertEquals("Default password should be empty", "", config.getPassword());
        assertEquals("Default batchSize should be 1000", 1000, config.getBatchSize());
        assertEquals("Default database should be empty", "", config.getDatabase());
    }

    @Test
    public void testSetHost() {
        LoaderConfiguration config = new LoaderConfiguration();
        config.setHost("192.168.1.100");

        assertEquals("Host should be set correctly", "192.168.1.100", config.getHost());
    }

    @Test
    public void testSetPort() {
        LoaderConfiguration config = new LoaderConfiguration();
        config.setPort(12345);

        assertEquals("Port should be set correctly", 12345, config.getPort());
    }

    @Test
    public void testSetUsername() {
        LoaderConfiguration config = new LoaderConfiguration();
        config.setUsername("admin");

        assertEquals("Username should be set correctly", "admin", config.getUsername());
    }

    @Test
    public void testSetPassword() {
        LoaderConfiguration config = new LoaderConfiguration();
        config.setPassword("secret");

        assertEquals("Password should be set correctly", "secret", config.getPassword());
    }

    @Test
    public void testSetBatchSize() {
        LoaderConfiguration config = new LoaderConfiguration();
        config.setBatchSize(5000);

        assertEquals("BatchSize should be set correctly", 5000, config.getBatchSize());
    }

    @Test
    public void testSetDatabase() {
        LoaderConfiguration config = new LoaderConfiguration();
        config.setDatabase("mydatabase");

        assertEquals("Database should be set correctly", "mydatabase", config.getDatabase());
    }

    @Test
    public void testSetTableCreationScript() {
        LoaderConfiguration config = new LoaderConfiguration();
        config.setTableCreationScript("scripts/create.dos");

        assertEquals("TableCreationScript should be set correctly", "scripts/create.dos", config.getTableCreationScript());
    }

    @Test
    public void testSetTableDeletionScript() {
        LoaderConfiguration config = new LoaderConfiguration();
        config.setTableDeletionScript("scripts/delete.dos");

        assertEquals("TableDeletionScript should be set correctly", "scripts/delete.dos", config.getTableDeletionScript());
    }

    @Test
    public void testToStringContainsHost() {
        LoaderConfiguration config = new LoaderConfiguration();
        config.setHost("testhost");

        assertTrue("toString should contain host", config.toString().contains("testhost"));
    }

    @Test
    public void testToStringContainsPort() {
        LoaderConfiguration config = new LoaderConfiguration();
        config.setPort(9999);

        assertTrue("toString should contain port", config.toString().contains("9999"));
    }

    @Test
    public void testToStringContainsDatabase() {
        LoaderConfiguration config = new LoaderConfiguration();
        config.setDatabase("testdb");

        assertTrue("toString should contain database", config.toString().contains("testdb"));
    }
}
