package com.sdd.etl.loader.config;

import com.sdd.etl.loader.api.exceptions.LoaderException;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Unit tests for ConfigParser.
 */
public class ConfigParserTest {

    private File testConfigFile;

    @Before
    public void setUp() throws IOException {
        testConfigFile = File.createTempFile("test-config", ".ini");
        testConfigFile.deleteOnExit();
    }

    @After
    public void tearDown() {
        if (testConfigFile != null && testConfigFile.exists()) {
            testConfigFile.delete();
        }
    }

    private void writeConfigFile(String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testConfigFile))) {
            writer.write(content);
        }
    }

    @Test
    public void testParseBasicConfiguration() throws Exception {
        String configContent =
                "[dolphindb]\n" +
                "host=192.168.1.100\n" +
                "port=8848\n" +
                "username=admin\n" +
                "password=secret\n";

        writeConfigFile(configContent);

        LoaderConfiguration config = ConfigParser.parse(testConfigFile.getAbsolutePath());

        assertEquals("Host should be parsed", "192.168.1.100", config.getHost());
        assertEquals("Port should be parsed", 8848, config.getPort());
        assertEquals("Username should be parsed", "admin", config.getUsername());
        assertEquals("Password should be parsed", "secret", config.getPassword());
    }

    @Test
    public void testParseWithBatchSizeAndDatabase() throws Exception {
        String configContent =
                "[dolphindb]\n" +
                "host=localhost\n" +
                "port=8848\n" +
                "batch_size=5000\n" +
                "database=mydb\n";

        writeConfigFile(configContent);

        LoaderConfiguration config = ConfigParser.parse(testConfigFile.getAbsolutePath());

        assertEquals("BatchSize should be parsed", 5000, config.getBatchSize());
        assertEquals("Database should be parsed", "mydb", config.getDatabase());
    }

    @Test
    public void testParseWithScripts() throws Exception {
        String configContent =
                "[dolphindb]\n" +
                "table_creation_script=scripts/create.dos\n" +
                "table_deletion_script=scripts/delete.dos\n";

        writeConfigFile(configContent);

        LoaderConfiguration config = ConfigParser.parse(testConfigFile.getAbsolutePath());

        assertEquals("TableCreationScript should be parsed", "scripts/create.dos", config.getTableCreationScript());
        assertEquals("TableDeletionScript should be parsed", "scripts/delete.dos", config.getTableDeletionScript());
    }

    @Test
    public void testParseFullConfiguration() throws Exception {
        String configContent =
                "[dolphindb]\n" +
                "host=dolphindb.example.com\n" +
                "port=8942\n" +
                "username=dbuser\n" +
                "password=mypassword\n" +
                "batch_size=2000\n" +
                "database=financialdb\n" +
                "table_creation_script=scripts/temporary_table_creation.dos\n" +
                "table_deletion_script=scripts/temporary_table_deletion.dos\n";

        writeConfigFile(configContent);

        LoaderConfiguration config = ConfigParser.parse(testConfigFile.getAbsolutePath());

        assertEquals("Host", "dolphindb.example.com", config.getHost());
        assertEquals("Port", 8942, config.getPort());
        assertEquals("Username", "dbuser", config.getUsername());
        assertEquals("Password", "mypassword", config.getPassword());
        assertEquals("BatchSize", 2000, config.getBatchSize());
        assertEquals("Database", "financialdb", config.getDatabase());
        assertEquals("TableCreationScript", "scripts/temporary_table_creation.dos", config.getTableCreationScript());
        assertEquals("TableDeletionScript", "scripts/temporary_table_deletion.dos", config.getTableDeletionScript());
    }

    @Test(expected = LoaderException.class)
    public void testParseWithNullPath() throws LoaderException {
        ConfigParser.parse((String) null);
    }

    @Test(expected = LoaderException.class)
    public void testParseWithEmptyPath() throws LoaderException {
        ConfigParser.parse("");
    }

    @Test(expected = LoaderException.class)
    public void testParseWithNonExistentFile() throws LoaderException {
        ConfigParser.parse("/non/existent/path/config.ini");
    }

    @Test
    public void testParsePartialConfigurationUsesDefaults() throws Exception {
        String configContent =
                "[dolphindb]\n" +
                "host=customhost\n";

        writeConfigFile(configContent);

        LoaderConfiguration config = ConfigParser.parse(testConfigFile.getAbsolutePath());

        assertEquals("Custom host should be used", "customhost", config.getHost());
        assertEquals("Default port should be used", 8848, config.getPort());
        assertEquals("Default batchSize should be used", 1000, config.getBatchSize());
    }

    @Test
    public void testParseWithEmptyValues() throws Exception {
        String configContent =
                "[dolphindb]\n" +
                "host=\n" +
                "port=\n" +
                "username=\n" +
                "password=\n";

        writeConfigFile(configContent);

        LoaderConfiguration config = ConfigParser.parse(testConfigFile.getAbsolutePath());

        assertEquals("Empty host should be returned", "", config.getHost());
        assertEquals("Empty username should be returned", "", config.getUsername());
        assertEquals("Empty password should be returned", "", config.getPassword());
    }
}
