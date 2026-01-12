package com.sdd.etl.source.extract.cos.config;

import com.sdd.etl.config.ETConfiguration;

/**
 * Configuration for Tencent COS data source.
 * Extends SourceConfig with COS-specific connection settings.
 * 
 * COS properties are stored in the properties map with keys:
 * - cos.endpoint
 * - cos.bucket
 * - cos.secretId
 * - cos.secretKey
 * - cos.region
 * - cos.prefix
 * 
 * SECURITY WARNING: SecretId and SecretKey contain sensitive credentials.
 * Never log or expose these values in toString(), logs, or error messages.
 * Consider using environment variables or secure configuration management
 * for production deployments.
 */
public class CosSourceConfig extends ETConfiguration.SourceConfig {

    /** Property key for COS endpoint */
    public static final String PROP_ENDPOINT = "cos.endpoint";
    /** Property key for COS bucket name */
    public static final String PROP_BUCKET = "cos.bucket";
    /** Property key for COS secret ID */
    public static final String PROP_SECRET_ID = "cos.secretId";
    /** Property key for COS secret key */
    public static final String PROP_SECRET_KEY = "cos.secretKey";
    /** Property key for COS region */
    public static final String PROP_REGION = "cos.region";
    /** Property key for COS prefix */
    public static final String PROP_PREFIX = "cos.prefix";
    /** Property key for maximum file size in bytes */
    public static final String PROP_MAX_FILE_SIZE = "cos.maxFileSize";
    /** Default maximum file size: 100 MB */
    public static final long DEFAULT_MAX_FILE_SIZE = 100L * 1024 * 1024;

    /**
     * Constructs a new CosSourceConfig with empty properties.
     */
    public CosSourceConfig() {
        super();
    }

    /**
     * Gets the COS endpoint URL.
     * @return endpoint URL, or null if not set
     */
    public String getEndpoint() {
        return getProperty(PROP_ENDPOINT);
    }

    /**
     * Sets the COS endpoint URL.
     * @param endpoint endpoint URL
     */
    public void setEndpoint(String endpoint) {
        setProperty(PROP_ENDPOINT, endpoint);
    }

    /**
     * Gets the COS bucket name.
     * @return bucket name, or null if not set
     */
    public String getBucket() {
        return getProperty(PROP_BUCKET);
    }

    /**
     * Sets the COS bucket name.
     * @param bucket bucket name
     */
    public void setBucket(String bucket) {
        setProperty(PROP_BUCKET, bucket);
    }

    /**
     * Gets the COS secret ID.
     * @return secret ID, or null if not set
     */
    public String getSecretId() {
        return getProperty(PROP_SECRET_ID);
    }

    /**
     * Sets the COS secret ID.
     * @param secretId secret ID
     */
    public void setSecretId(String secretId) {
        setProperty(PROP_SECRET_ID, secretId);
    }

    /**
     * Gets the COS secret key.
     * @return secret key, or null if not set
     */
    public String getSecretKey() {
        return getProperty(PROP_SECRET_KEY);
    }

    /**
     * Sets the COS secret key.
     * @param secretKey secret key
     */
    public void setSecretKey(String secretKey) {
        setProperty(PROP_SECRET_KEY, secretKey);
    }

    /**
     * Gets the COS region.
     * @return region, or null if not set
     */
    public String getRegion() {
        return getProperty(PROP_REGION);
    }

    /**
     * Sets the COS region.
     * @param region region
     */
    public void setRegion(String region) {
        setProperty(PROP_REGION, region);
    }

    /**
     * Gets the COS prefix (path prefix for filtering files).
     * @return prefix, or null if not set
     */
    public String getPrefix() {
        return getProperty(PROP_PREFIX);
    }

    /**
     * Sets the COS prefix.
     * @param prefix path prefix
     */
    public void setPrefix(String prefix) {
        setProperty(PROP_PREFIX, prefix);
    }

    /**
     * Gets the maximum file size allowed for download, in bytes.
     * Returns null if not configured.
     * @return maximum file size in bytes, or null
     */
    public Long getMaxFileSize() {
        String value = getProperty(PROP_MAX_FILE_SIZE);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Sets the maximum file size allowed for download, in bytes.
     * @param maxFileSize maximum file size in bytes
     */
    public void setMaxFileSize(Long maxFileSize) {
        if (maxFileSize == null) {
            setProperty(PROP_MAX_FILE_SIZE, null);
        } else {
            setProperty(PROP_MAX_FILE_SIZE, maxFileSize.toString());
        }
    }

    /**
     * Gets the maximum file size allowed for download, using the default
     * value (100 MB) if not explicitly configured.
     * @return maximum file size in bytes (never null)
     */
    public long getMaxFileSizeOrDefault() {
        Long configured = getMaxFileSize();
        if (configured != null) {
            return configured;
        }
        return DEFAULT_MAX_FILE_SIZE;
    }

    /**
     * Checks if a credential value appears to be a placeholder.
     * Common placeholder patterns indicate credentials haven't been properly set.
     * 
     * @param credential the credential string to check
     * @return true if credential matches placeholder patterns
     */
    private static boolean isPlaceholderCredential(String credential) {
        if (credential == null || credential.trim().isEmpty()) {
            return false; // Already caught by required field validation
        }
        
        String lowerCred = credential.toLowerCase().trim();
        // Common placeholder patterns
        // Exact matches for common placeholder words
        if (lowerCred.equals("changeme") ||
            lowerCred.equals("placeholder") ||
            lowerCred.equals("example") ||
            lowerCred.equals("fake") ||
            lowerCred.equals("dummy")) {
            return true;
        }
        // Starts with "your-" (commonly used in documentation)
        if (lowerCred.startsWith("your-")) {
            return true;
        }
        // All whitespace or digits only (invalid credentials)
        if (lowerCred.matches("^[\\s\\d]+$")) {
            return true;
        }
        return false;
    }

    /**
     * Checks if anonymous credentials are being used.
     * Anonymous mode is enabled when secretId and secretKey are both not provided.
     *
     * @return true if using anonymous credentials (no secretId/secretKey)
     */
    public boolean isAnonymous() {
        String secretId = getSecretId();
        String secretKey = getSecretKey();
        boolean secretIdProvided = (secretId != null && !secretId.trim().isEmpty());
        boolean secretKeyProvided = (secretKey != null && !secretKey.trim().isEmpty());
        return !secretIdProvided && !secretKeyProvided;
    }

    /**
     * Validates COS-specific configuration.
     * Required fields: endpoint, bucket.
     * Optional fields: region, prefix.
     * Credentials (secretId and secretKey) can be either:
     * - Both provided (authenticated access) - validated for placeholders
     * - Both omitted (anonymous access)
     * - Partially provided (invalid - must use either all or none)
     * Bucket name must follow COS naming conventions (lowercase letters, numbers, hyphens).
     *
     * @return true if configuration is valid
     */
    @Override
    public boolean isValid() {
        // First validate required fields from parent
        if (!super.isValid()) {
            return false;
        }

        // Check COS-specific required fields
        if (getEndpoint() == null || getEndpoint().trim().isEmpty()) {
            return false;
        }
        if (getBucket() == null || getBucket().trim().isEmpty()) {
            return false;
        }

        // Validate credentials: must be either both provided (authenticated) or both omitted (anonymous)
        String secretId = getSecretId();
        String secretKey = getSecretKey();
        boolean secretIdProvided = (secretId != null && !secretId.trim().isEmpty());
        boolean secretKeyProvided = (secretKey != null && !secretKey.trim().isEmpty());

        // Partial credentials are invalid
        if (secretIdProvided != secretKeyProvided) {
            return false;
        }

        // If both provided, validate they are not placeholders
        if (secretIdProvided && secretKeyProvided) {
            if (isPlaceholderCredential(secretId) || isPlaceholderCredential(secretKey)) {
                return false;
            }
        }

        // Validate bucket name: lowercase letters, numbers, hyphens
        String bucket = getBucket();
        if (!bucket.matches("^[a-z0-9-]+$")) {
            return false;
        }

        // Validate maxFileSize if configured: must be positive
        Long maxFileSize = getMaxFileSize();
        if (maxFileSize != null && maxFileSize <= 0) {
            return false;
        }

        return true;
    }

    /**
     * Returns a string representation of this configuration.
     * @return string representation
     */
    @Override
    public String toString() {
        return "CosSourceConfig{" +
                "name='" + getName() + '\'' +
                ", type='" + getType() + '\'' +
                ", endpoint='" + getEndpoint() + '\'' +
                ", bucket='" + getBucket() + '\'' +
                ", region='" + getRegion() + '\'' +
                ", prefix='" + getPrefix() + '\'' +
                ", maxFileSize=" + getMaxFileSizeOrDefault() +
                '}';
    }
}