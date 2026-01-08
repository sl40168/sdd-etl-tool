package com.sdd.etl.util;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Detects concurrent ETL executions using file lock mechanism.
 * Ensures only one ETL process runs at a time.
 */
public class ConcurrentExecutionDetector {

    private static final String DEFAULT_LOCK_FILE_NAME = ".etl.lock";
    private String lockFileName;
    private FileChannel channel;
    private FileLock lock;

    /**
     * Default constructor using default lock file name.
     */
    public ConcurrentExecutionDetector() {
        this.lockFileName = DEFAULT_LOCK_FILE_NAME;
    }

    /**
     * Constructor with custom lock file name.
     *
     * @param lockFileName custom lock file name
     */
    public ConcurrentExecutionDetector(String lockFileName) {
        this.lockFileName = lockFileName;
    }

    /**
     * Acquires file lock to prevent concurrent execution.
     *
     * @return true if lock acquired successfully, false if another process is running
     */
    public boolean acquireLock() {
        // If lock already acquired by this detector, return true
        if (lock != null) {
            return true;
        }
        
        // If channel exists but no lock (previous acquisition failed), clean up
        if (channel != null) {
            try {
                channel.close();
            } catch (Exception e) {
                // Ignore
            }
            channel = null;
        }
        
        try {
            File lockFile = new File(lockFileName);

            // Create lock file if it doesn't exist
            if (!lockFile.exists()) {
                lockFile.createNewFile();
            }

            // Open file channel and try to acquire exclusive lock
            FileOutputStream fos = new FileOutputStream(lockFile);
            channel = fos.getChannel();

            try {
                lock = channel.tryLock();
            } catch (java.nio.channels.OverlappingFileLockException e) {
                // Already locked within this JVM (e.g., another detector instance)
                lock = null;
            }

            if (lock == null) {
                // Another process (or another detector in this JVM) holds the lock
                channel.close();
                channel = null;
                return false;
            }

            return true;
        } catch (Exception e) {
            com.sdd.etl.logging.ETLogger.error("Failed to acquire file lock: " + e.getMessage(), e);
            // Clean up if any resources were opened
            if (channel != null) {
                try { channel.close(); } catch (Exception ex) {}
                channel = null;
            }
            return false;
        }
    }

    /**
     * Releases file lock when process completes.
     */
    public void releaseLock() {
        try {
            if (lock != null) {
                lock.release();
            }

            if (channel != null) {
                channel.close();
            }

            // Delete lock file
            File lockFile = new File(lockFileName);
            if (lockFile.exists()) {
                lockFile.delete();
            }
        } catch (Exception e) {
            com.sdd.etl.logging.ETLogger.error("Failed to release file lock: " + e.getMessage(), e);
        } finally {
            lock = null;
            channel = null;
        }
    }

    /**
     * Gets the lock file path.
     *
     * @return absolute path to lock file
     */
    public String getLockFilePath() {
        return new File(lockFileName).getAbsolutePath();
    }
}
