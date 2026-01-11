package com.sdd.etl.loader.dolphin;

import com.sdd.etl.loader.api.exceptions.ScriptExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Executes DolphinDB scripts for table management.
 * Note: DBConnection is from com.xxdb package provided by DolphinDB Java API.
 */
public class DolphinDBScriptExecutor {

    private static final Logger logger = LoggerFactory.getLogger(DolphinDBScriptExecutor.class);

    private final DolphinDBConnection connection;

    public DolphinDBScriptExecutor(DolphinDBConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }
        this.connection = connection;
    }

    /**
     * Executes a script from a file.
     *
     * @param scriptPath path to the script file
     * @throws ScriptExecutionException if script execution fails
     */
    public void executeScriptFromFile(String scriptPath) throws ScriptExecutionException {
        if (scriptPath == null || scriptPath.isEmpty()) {
            throw new IllegalArgumentException("Script path cannot be null or empty");
        }

        File scriptFile = new File(scriptPath);
        if (!scriptFile.exists()) {
            throw new ScriptExecutionException("Script file not found: " + scriptPath);
        }

        try {
            String scriptContent = readFile(scriptFile);
            executeScript(scriptContent);
            logger.info("Successfully executed script: {}", scriptPath);
        } catch (IOException e) {
            throw new ScriptExecutionException("Failed to read script file: " + scriptPath, e);
        }
    }

    /**
     * Executes a script string directly.
     *
     * @param script the DolphinDB script to execute
     * @throws ScriptExecutionException if script execution fails
     */
    public void executeScript(String script) throws ScriptExecutionException {
        if (script == null || script.trim().isEmpty()) {
            logger.warn("Empty script provided, skipping execution");
            return;
        }

        try {
            Object conn = connection.getConnection();
            logger.debug("Executing DolphinDB script: {}", script.substring(0, Math.min(100, script.length())));
            // Use reflection to call run() method on DBConnection
            java.lang.reflect.Method method = conn.getClass().getMethod("run", String.class);
            method.invoke(conn, script);
        } catch (Exception e) {
            throw new ScriptExecutionException("Failed to execute DolphinDB script: " + e.getMessage(), e);
        }
    }

    /**
     * Reads a file content into a string.
     */
    private String readFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}
