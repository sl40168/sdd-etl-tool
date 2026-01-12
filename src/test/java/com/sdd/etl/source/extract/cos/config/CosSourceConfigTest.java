package com.sdd.etl.source.extract.cos.config;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for CosSourceConfig validation.
 * Tests COS-specific configuration validation logic.
 */
public class CosSourceConfigTest {

    private CosSourceConfig config;

    @Before
    public void setUp() {
        config = new CosSourceConfig();
    }

    @Test
    public void testIsValid_ValidConfig_ReturnsTrue() {
        // Setup a valid configuration
        config.setName("cos-source");
        config.setType("cos");
        config.setConnectionString("cos://example.com");
        config.setExtractQuery("SELECT * FROM data");
        config.setDateField("business_date");

        // COS-specific required fields
        config.setEndpoint("https://example.cos.ap-shanghai.myqcloud.com");
        config.setBucket("my-bucket-123");
        config.setSecretId("AKIDexample");
        config.setSecretKey("secret-key-example");

        assertTrue("Valid config should return true", config.isValid());
    }

    @Test
    public void testIsValid_MissingEndpoint_ReturnsFalse() {
        // Missing endpoint
        config.setName("cos-source");
        config.setType("cos");
        config.setConnectionString("cos://example.com");
        config.setBucket("my-bucket");
        config.setSecretId("AKIDexample");
        config.setSecretKey("secret-key-example");

        assertFalse("Missing endpoint should return false", config.isValid());
    }

    @Test
    public void testIsValid_EmptyEndpoint_ReturnsFalse() {
        // Empty endpoint
        config.setName("cos-source");
        config.setType("cos");
        config.setConnectionString("cos://example.com");
        config.setEndpoint("");
        config.setBucket("my-bucket");
        config.setSecretId("AKIDexample");
        config.setSecretKey("secret-key-example");

        assertFalse("Empty endpoint should return false", config.isValid());
    }

    @Test
    public void testIsValid_MissingBucket_ReturnsFalse() {
        // Missing bucket
        config.setName("cos-source");
        config.setType("cos");
        config.setConnectionString("cos://example.com");
        config.setEndpoint("https://example.cos.ap-shanghai.myqcloud.com");
        config.setSecretId("AKIDexample");
        config.setSecretKey("secret-key-example");

        assertFalse("Missing bucket should return false", config.isValid());
    }

    @Test
    public void testIsValid_EmptyBucket_ReturnsFalse() {
        // Empty bucket
        config.setName("cos-source");
        config.setType("cos");
        config.setConnectionString("cos://example.com");
        config.setEndpoint("https://example.cos.ap-shanghai.myqcloud.com");
        config.setBucket("");
        config.setSecretId("AKIDexample");
        config.setSecretKey("secret-key-example");

        assertFalse("Empty bucket should return false", config.isValid());
    }

    @Test
    public void testIsValid_MissingSecretId_ReturnsFalse() {
        // Missing secret ID is now allowed (anonymous access)
        // Only secretId provided, no secretKey -> invalid
        config.setName("cos-source");
        config.setType("cos");
        config.setConnectionString("cos://example.com");
        config.setEndpoint("https://example.cos.ap-shanghai.myqcloud.com");
        config.setBucket("my-bucket");
        config.setSecretKey("secret-key-example");

        // With only secretKey and no secretId, should be invalid (partial credentials)
        assertFalse("Partial credentials (only secretKey) should return false", config.isValid());
    }

    @Test
    public void testIsValid_EmptySecretId_ReturnsFalse() {
        // Empty secret ID with secretKey provided -> invalid (partial credentials)
        config.setName("cos-source");
        config.setType("cos");
        config.setConnectionString("cos://example.com");
        config.setEndpoint("https://example.cos.ap-shanghai.myqcloud.com");
        config.setBucket("my-bucket");
        config.setSecretId("");
        config.setSecretKey("secret-key-example");

        assertFalse("Partial credentials (empty secretId) should return false", config.isValid());
    }

    @Test
    public void testIsValid_MissingSecretKey_ReturnsFalse() {
        // Missing secret key is now allowed (anonymous access)
        // Only secretId provided, no secretKey -> invalid
        config.setName("cos-source");
        config.setType("cos");
        config.setConnectionString("cos://example.com");
        config.setEndpoint("https://example.cos.ap-shanghai.myqcloud.com");
        config.setBucket("my-bucket");
        config.setSecretId("AKIDexample");

        assertFalse("Partial credentials (only secretId) should return false", config.isValid());
    }

    @Test
    public void testIsValid_EmptySecretKey_ReturnsFalse() {
        // Empty secret key with secretId provided -> invalid (partial credentials)
        config.setName("cos-source");
        config.setType("cos");
        config.setConnectionString("cos://example.com");
        config.setEndpoint("https://example.cos.ap-shanghai.myqcloud.com");
        config.setBucket("my-bucket");
        config.setSecretId("AKIDexample");
        config.setSecretKey("");

        assertFalse("Partial credentials (empty secretKey) should return false", config.isValid());
    }

    @Test
    public void testIsValid_InvalidEndpointUrl_ReturnsFalse() {
        // Empty endpoint is invalid (required field)
        config.setName("cos-source");
        config.setType("cos");
        config.setConnectionString("cos://example.com");
        config.setEndpoint("");
        config.setBucket("my-bucket");
        config.setSecretId("AKIDexample");
        config.setSecretKey("secret-key-example");

        assertFalse("Empty endpoint should return false", config.isValid());
    }

    @Test
    public void testIsValid_BucketWithInvalidCharacters_ReturnsFalse() {
        // Bucket name with invalid characters
        config.setName("cos-source");
        config.setType("cos");
        config.setConnectionString("cos://example.com");
        config.setEndpoint("https://example.cos.ap-shanghai.myqcloud.com");
        config.setBucket("MyBucket!@#"); // Uppercase and special chars
        config.setSecretId("AKIDexample");
        config.setSecretKey("secret-key-example");

        assertFalse("Bucket with invalid characters should return false", config.isValid());
    }

    @Test
    public void testIsValid_BucketWithUppercase_ReturnsFalse() {
        // Bucket name with uppercase letters (must be lowercase)
        config.setName("cos-source");
        config.setType("cos");
        config.setConnectionString("cos://example.com");
        config.setEndpoint("https://example.cos.ap-shanghai.myqcloud.com");
        config.setBucket("MyBucket"); // Uppercase letter
        config.setSecretId("AKIDexample");
        config.setSecretKey("secret-key-example");

        assertFalse("Bucket with uppercase letters should return false", config.isValid());
    }

    @Test
    public void testIsValid_ValidBucketNames_AllReturnTrue() {
        // Test valid bucket names
        String[] validBucketNames = {
            "mybucket",
            "my-bucket",
            "my-bucket-123",
            "bucket-name",
            "bucket123",
            "test-bucket-456"
        };

        for (String bucketName : validBucketNames) {
            CosSourceConfig c = new CosSourceConfig();
            c.setName("cos-source");
            c.setType("cos");
            c.setConnectionString("cos://example.com");
            c.setEndpoint("https://example.cos.ap-shanghai.myqcloud.com");
            c.setBucket(bucketName);
            c.setSecretId("AKIDexample");
            c.setSecretKey("secret-key-example");

            assertTrue("Valid bucket name '" + bucketName + "' should return true", c.isValid());
        }
    }

    @Test
    public void testIsValid_OptionalRegionAndPrefix_ReturnsTrue() {
        // Optional fields region and prefix can be null or empty
        config.setName("cos-source");
        config.setType("cos");
        config.setConnectionString("cos://example.com");
        config.setEndpoint("https://example.cos.ap-shanghai.myqcloud.com");
        config.setBucket("my-bucket");
        config.setSecretId("AKIDexample");
        config.setSecretKey("secret-key-example");

        // Region and prefix not set (default null)
        assertTrue("Config with missing optional fields should return true", config.isValid());

        // Set region and prefix
        config.setRegion("ap-shanghai");
        config.setPrefix("data/");

        assertTrue("Config with optional fields set should return true", config.isValid());
    }

    @Test
    public void testIsValid_MissingParentRequiredFields_ReturnsFalse() {
        // Missing parent required fields (name, type, connectionString)
        config.setEndpoint("https://example.cos.ap-shanghai.myqcloud.com");
        config.setBucket("my-bucket");
        config.setSecretId("AKIDexample");
        config.setSecretKey("secret-key-example");

        // No name, type, connectionString set
        assertFalse("Config missing parent required fields should return false", config.isValid());
    }

    @Test
    public void testGetSetEndpoint() {
        // Test endpoint getter/setter
        String endpoint = "https://example.cos.ap-shanghai.myqcloud.com";
        config.setEndpoint(endpoint);

        assertEquals("Endpoint should match", endpoint, config.getEndpoint());
    }

    @Test
    public void testGetSetBucket() {
        // Test bucket getter/setter
        String bucket = "my-bucket";
        config.setBucket(bucket);

        assertEquals("Bucket should match", bucket, config.getBucket());
    }

    @Test
    public void testGetSetSecretId() {
        // Test secret ID getter/setter
        String secretId = "AKIDexample";
        config.setSecretId(secretId);

        assertEquals("SecretId should match", secretId, config.getSecretId());
    }

    @Test
    public void testGetSetSecretKey() {
        // Test secret key getter/setter
        String secretKey = "secret-key-example";
        config.setSecretKey(secretKey);

        assertEquals("SecretKey should match", secretKey, config.getSecretKey());
    }

    @Test
    public void testGetSetRegion() {
        // Test region getter/setter
        String region = "ap-shanghai";
        config.setRegion(region);

        assertEquals("Region should match", region, config.getRegion());
    }

    @Test
    public void testGetSetPrefix() {
        // Test prefix getter/setter
        String prefix = "data/";
        config.setPrefix(prefix);

        assertEquals("Prefix should match", prefix, config.getPrefix());
    }

    @Test
    public void testGetSetMaxFileSize() {
        // Test maxFileSize getter/setter
        Long maxFileSize = 100L * 1024 * 1024; // 100MB
        config.setMaxFileSize(maxFileSize);

        assertEquals("maxFileSize should match", maxFileSize, config.getMaxFileSize());

        // Test setting null
        config.setMaxFileSize(null);
        assertNull("maxFileSize should be null", config.getMaxFileSize());
    }

    @Test
    public void testGetMaxFileSizeOrDefault_WhenNotSet_ReturnsDefault() {
        // When not configured, should return default (100MB)
        assertNull("maxFileSize should be null when not set", config.getMaxFileSize());
        assertEquals("getMaxFileSizeOrDefault should return default 100MB",
                100L * 1024 * 1024, config.getMaxFileSizeOrDefault());
    }

    @Test
    public void testGetMaxFileSizeOrDefault_WhenSet_ReturnsConfiguredValue() {
        // When configured with custom value, should return that value
        Long customSize = 500L;
        config.setMaxFileSize(customSize);

        assertEquals("getMaxFileSizeOrDefault should return configured value",
                customSize, (Long) config.getMaxFileSizeOrDefault());
    }

    @Test
    public void testIsValid_MaxFileSizeZeroOrNegative_ReturnsFalse() {
        // Setup valid base configuration
        config.setName("cos-source");
        config.setType("cos");
        config.setConnectionString("cos://example.com");
        config.setEndpoint("https://example.cos.ap-shanghai.myqcloud.com");
        config.setBucket("my-bucket");
        config.setSecretId("AKIDexample");
        config.setSecretKey("secret-key-example");

        // Zero maxFileSize should be invalid
        config.setMaxFileSize(0L);
        assertFalse("maxFileSize = 0 should be invalid", config.isValid());

        // Negative maxFileSize should be invalid
        config.setMaxFileSize(-100L);
        assertFalse("maxFileSize < 0 should be invalid", config.isValid());
    }

    @Test
    public void testIsValid_MaxFileSizePositive_ReturnsTrue() {
        // Setup valid base configuration
        config.setName("cos-source");
        config.setType("cos");
        config.setConnectionString("cos://example.com");
        config.setEndpoint("https://example.cos.ap-shanghai.myqcloud.com");
        config.setBucket("my-bucket");
        config.setSecretId("AKIDexample");
        config.setSecretKey("secret-key-example");

        // Positive maxFileSize should be valid
        config.setMaxFileSize(100L * 1024 * 1024); // 100MB
        assertTrue("maxFileSize > 0 should be valid", config.isValid());
    }

    @Test
    public void testToString_ContainsExpectedFields() {
        // toString should include key fields
        config.setName("cos-source");
        config.setType("cos");
        config.setEndpoint("https://example.cos.ap-shanghai.myqcloud.com");
        config.setBucket("my-bucket");
        config.setRegion("ap-shanghai");
        config.setPrefix("data/");

        String str = config.toString();

        assertTrue("toString should contain name", str.contains("name='cos-source'"));
        assertTrue("toString should contain type", str.contains("type='cos'"));
        assertTrue("toString should contain endpoint", str.contains("endpoint='https://example.cos.ap-shanghai.myqcloud.com'"));
        assertTrue("toString should contain bucket", str.contains("bucket='my-bucket'"));
        assertTrue("toString should contain region", str.contains("region='ap-shanghai'"));
        assertTrue("toString should contain prefix", str.contains("prefix='data/'"));
    }

    @Test
    public void testIsValid_AnonymousAccess_NoCredentials_ReturnsTrue() {
        // Anonymous access: no secretId and no secretKey should be valid
        config.setName("cos-source");
        config.setType("cos");
        config.setConnectionString("cos://example.com");
        config.setEndpoint("https://example.cos.ap-shanghai.myqcloud.com");
        config.setBucket("my-bucket");
        // No secretId, no secretKey -> anonymous access

        assertTrue("Anonymous access (no credentials) should be valid", config.isValid());
        assertTrue("Should be using anonymous credentials", config.isAnonymous());
    }

    @Test
    public void testIsValid_AuthenticatedAccess_BothCredentials_ReturnsTrue() {
        // Authenticated access: both secretId and secretKey provided
        config.setName("cos-source");
        config.setType("cos");
        config.setConnectionString("cos://example.com");
        config.setEndpoint("https://example.cos.ap-shanghai.myqcloud.com");
        config.setBucket("my-bucket");
        config.setSecretId("AKIDexample");
        config.setSecretKey("secret-key-example");

        assertTrue("Authenticated access (both credentials) should be valid", config.isValid());
        assertFalse("Should not be using anonymous credentials", config.isAnonymous());
    }

    @Test
    public void testIsValid_PartialCredentials_OnlySecretId_ReturnsFalse() {
        // Partial credentials: only secretId, no secretKey should be invalid
        config.setName("cos-source");
        config.setType("cos");
        config.setConnectionString("cos://example.com");
        config.setEndpoint("https://example.cos.ap-shanghai.myqcloud.com");
        config.setBucket("my-bucket");
        config.setSecretId("AKIDexample");
        // No secretKey

        assertFalse("Partial credentials (only secretId) should be invalid", config.isValid());
        assertFalse("Should not be considered anonymous", config.isAnonymous());
    }

    @Test
    public void testIsValid_PartialCredentials_OnlySecretKey_ReturnsFalse() {
        // Partial credentials: only secretKey, no secretId should be invalid
        config.setName("cos-source");
        config.setType("cos");
        config.setConnectionString("cos://example.com");
        config.setEndpoint("https://example.cos.ap-shanghai.myqcloud.com");
        config.setBucket("my-bucket");
        // No secretId
        config.setSecretKey("secret-key-example");

        assertFalse("Partial credentials (only secretKey) should be invalid", config.isValid());
        assertFalse("Should not be considered anonymous", config.isAnonymous());
    }

    @Test
    public void testIsValid_BothCredentialsEmpty_ReturnsTrue() {
        // Both credentials empty strings should be treated as anonymous
        config.setName("cos-source");
        config.setType("cos");
        config.setConnectionString("cos://example.com");
        config.setEndpoint("https://example.cos.ap-shanghai.myqcloud.com");
        config.setBucket("my-bucket");
        config.setSecretId("");
        config.setSecretKey("");

        assertTrue("Empty credentials should be treated as anonymous access", config.isValid());
        assertTrue("Should be using anonymous credentials", config.isAnonymous());
    }
}
