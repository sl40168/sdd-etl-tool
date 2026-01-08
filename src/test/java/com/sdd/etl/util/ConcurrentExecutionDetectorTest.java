package com.sdd.etl.util;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Unit tests for ConcurrentExecutionDetector.
 */
public class ConcurrentExecutionDetectorTest {

    private static final String LOCK_FILE = ".etl.test.lock";
    private ConcurrentExecutionDetector detector;

    @Before
    public void setUp() {
        // Clean up any existing lock file
        deleteLockFile();
        detector = new ConcurrentExecutionDetector(LOCK_FILE);
    }

    @After
    public void tearDown() {
        // Clean up lock file after tests
        if (detector != null) {
            detector.releaseLock();
        }
        deleteLockFile();
    }

    private void deleteLockFile() {
        try {
            File lockFile = new File(LOCK_FILE);
            if (lockFile.exists()) {
                lockFile.delete();
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    @Test
    public void testAcquireLock_AcquiresSuccessfully() throws Exception {
        boolean result = detector.acquireLock();

        assertTrue("Should acquire lock successfully", result);
        assertTrue("Lock file should exist", new File(LOCK_FILE).exists());
    }

    @Test
    public void testAcquireLock_FailsWhenLocked() throws Exception {
        // First acquisition should succeed
        detector.acquireLock();

        // Create a second detector to simulate concurrent execution
        ConcurrentExecutionDetector secondDetector = new ConcurrentExecutionDetector(LOCK_FILE);
        boolean result = secondDetector.acquireLock();

        assertFalse("Should fail to acquire lock when already held", result);
    }

    @Test
    public void testReleaseLock_ReleasesSuccessfully() throws Exception {
        // Acquire lock
        detector.acquireLock();
        assertTrue("Lock file should exist after acquisition", new File(LOCK_FILE).exists());

        // Release lock
        detector.releaseLock();

        assertFalse("Lock file should not exist after release", new File(LOCK_FILE).exists());
    }

    @Test
    public void testReleaseLock_CanReacquireAfterRelease() throws Exception {
        // Acquire and release
        detector.acquireLock();
        detector.releaseLock();

        // Should be able to acquire again
        boolean result = detector.acquireLock();

        assertTrue("Should acquire lock after previous release", result);
        assertTrue("Lock file should exist after re-acquisition", new File(LOCK_FILE).exists());
    }

    @Test
    public void testAcquireLock_MultipleTimesSameDetector() throws Exception {
        // First acquisition
        boolean result1 = detector.acquireLock();
        assertTrue("First acquisition should succeed", result1);

        // Second acquisition on same detector should also succeed
        boolean result2 = detector.acquireLock();
        assertTrue("Second acquisition on same detector should succeed", result2);
    }

    @Test
    public void testReleaseLock_CalledWithoutAcquire() throws Exception {
        // Release without acquiring should not throw exception
        try {
            detector.releaseLock();
            // Success if no exception thrown
        } catch (Exception e) {
            fail("Release without acquire should not throw exception");
        }
    }

    @Test
    public void testAcquireLock_CreatesLockFileInWorkingDirectory() throws Exception {
        String testLockFile = ".etl.custom.test.lock";
        ConcurrentExecutionDetector customDetector = new ConcurrentExecutionDetector(testLockFile);

        customDetector.acquireLock();

        assertTrue("Custom lock file should be created", new File(testLockFile).exists());

        // Cleanup
        customDetector.releaseLock();
        new File(testLockFile).delete();
    }

    @Test
    public void testConcurrentScenario_TwoDetectorsCompete() throws Exception {
        ConcurrentExecutionDetector detector1 = new ConcurrentExecutionDetector(LOCK_FILE);
        ConcurrentExecutionDetector detector2 = new ConcurrentExecutionDetector(LOCK_FILE);

        // Detector 1 acquires
        boolean acquired1 = detector1.acquireLock();
        assertTrue("First detector should acquire lock", acquired1);

        // Detector 2 tries to acquire
        boolean acquired2 = detector2.acquireLock();
        assertFalse("Second detector should not acquire lock", acquired2);

        // Detector 1 releases
        detector1.releaseLock();

        // Detector 2 should now be able to acquire
        boolean acquired2AfterRelease = detector2.acquireLock();
        assertTrue("Second detector should acquire after first releases", acquired2AfterRelease);

        // Cleanup
        detector2.releaseLock();
    }

    @Test
    public void testLockPersistence_SurvivesCrash() throws Exception {
        detector.acquireLock();

        // Simulate detector being garbage collected
        detector = null;

        // Try to acquire with new detector
        ConcurrentExecutionDetector newDetector = new ConcurrentExecutionDetector(LOCK_FILE);
        boolean result = newDetector.acquireLock();

        assertFalse("Lock should persist across detector instances", result);

        // Cleanup
        newDetector.releaseLock();
    }
}
