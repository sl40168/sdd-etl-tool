package com.sdd.etl.model;

import java.util.Map;

/**
 * Abstract base class for data models sourced from Tencent COS (Cloud Object Storage).
 * 
 * <p>Provides COS-specific metadata fields that are common to all COS data sources,
 * such as bucket name, region, prefix, and endpoint configuration.</p>
 * 
 * <p>Concrete implementations for specific COS data types (e.g., Xbond Quote data)
 * should extend this class and add their domain-specific fields and validation logic.</p>
 */
public abstract class CosSourceDataModel extends SourceDataModel {
    
    /** COS bucket name */
    protected String cosBucket;
    
    /** COS region */
    protected String cosRegion;
    
    /** COS endpoint URL */
    protected String cosEndpoint;
    
    /** COS object prefix (path prefix for filtering files) */
    protected String cosPrefix;
    
    /** Maximum allowed file size in bytes (default 100MB) */
    protected Long cosMaxFileSize;
    
    /** COS secret ID (for authentication) */
    protected String cosSecretId;
    
    /** COS secret key (for authentication) */
    protected String cosSecretKey;
    
    /**
     * Constructs an empty CosSourceDataModel with default values.
     */
    public CosSourceDataModel() {
        super();
        this.cosMaxFileSize = 100L * 1024 * 1024; // 100MB default
    }
    
    /**
     * Gets the COS bucket name.
     * 
     * @return bucket name, or null if not set
     */
    public String getCosBucket() {
        return cosBucket;
    }
    
    /**
     * Sets the COS bucket name.
     * 
     * @param cosBucket bucket name
     */
    public void setCosBucket(String cosBucket) {
        this.cosBucket = cosBucket;
    }
    
    /**
     * Gets the COS region.
     * 
     * @return region, or null if not set
     */
    public String getCosRegion() {
        return cosRegion;
    }
    
    /**
     * Sets the COS region.
     * 
     * @param cosRegion region
     */
    public void setCosRegion(String cosRegion) {
        this.cosRegion = cosRegion;
    }
    
    /**
     * Gets the COS endpoint URL.
     * 
     * @return endpoint URL, or null if not set
     */
    public String getCosEndpoint() {
        return cosEndpoint;
    }
    
    /**
     * Sets the COS endpoint URL.
     * 
     * @param cosEndpoint endpoint URL
     */
    public void setCosEndpoint(String cosEndpoint) {
        this.cosEndpoint = cosEndpoint;
    }
    
    /**
     * Gets the COS object prefix.
     * 
     * @return prefix, or null if not set
     */
    public String getCosPrefix() {
        return cosPrefix;
    }
    
    /**
     * Sets the COS object prefix.
     * 
     * @param cosPrefix prefix
     */
    public void setCosPrefix(String cosPrefix) {
        this.cosPrefix = cosPrefix;
    }
    
    /**
     * Gets the maximum allowed file size in bytes.
     * 
     * @return maximum file size in bytes, or null if not set
     */
    public Long getCosMaxFileSize() {
        return cosMaxFileSize;
    }
    
    /**
     * Sets the maximum allowed file size in bytes.
     * 
     * @param cosMaxFileSize maximum file size in bytes
     */
    public void setCosMaxFileSize(Long cosMaxFileSize) {
        this.cosMaxFileSize = cosMaxFileSize;
    }
    
    /**
     * Gets the COS secret ID.
     * 
     * @return secret ID, or null if not set
     */
    public String getCosSecretId() {
        return cosSecretId;
    }
    
    /**
     * Sets the COS secret ID.
     * 
     * @param cosSecretId secret ID
     */
    public void setCosSecretId(String cosSecretId) {
        this.cosSecretId = cosSecretId;
    }
    
    /**
     * Gets the COS secret key.
     * 
     * @return secret key, or null if not set
     */
    public String getCosSecretKey() {
        return cosSecretKey;
    }
    
    /**
     * Sets the COS secret key.
     * 
     * @param cosSecretKey secret key
     */
    public void setCosSecretKey(String cosSecretKey) {
        this.cosSecretKey = cosSecretKey;
    }
    
    /**
     * Validates COS-specific configuration.
     * 
     * <p>This method should be called by concrete implementations in their
     * {@link #validate()} method to ensure COS configuration is valid.
     * 
     * @return true if COS configuration is valid
     */
    protected boolean validateCosConfiguration() {
        // Validate required COS fields
        if (cosBucket == null || cosBucket.trim().isEmpty()) {
            return false;
        }
        if (cosEndpoint == null || cosEndpoint.trim().isEmpty()) {
            return false;
        }
        // Secret ID and key are required for authentication
        if (cosSecretId == null || cosSecretId.trim().isEmpty()) {
            return false;
        }
        if (cosSecretKey == null || cosSecretKey.trim().isEmpty()) {
            return false;
        }
        // Validate region (optional but recommended)
        if (cosRegion != null && !cosRegion.trim().isEmpty()) {
            // Basic region format validation (letters, numbers, hyphens)
            if (!cosRegion.matches("^[a-z0-9-]+$")) {
                return false;
            }
        }
        // Validate max file size: must be positive if set
        if (cosMaxFileSize != null && cosMaxFileSize <= 0) {
            return false;
        }
        return true;
    }
    
    /**
     * Returns a map containing all COS configuration fields.
     * Useful for debugging and logging.
     * 
     * @return map of COS configuration fields
     */
    public Map<String, Object> getCosConfigurationSummary() {
        Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("cosBucket", cosBucket);
        summary.put("cosRegion", cosRegion);
        summary.put("cosEndpoint", cosEndpoint);
        summary.put("cosPrefix", cosPrefix);
        summary.put("cosMaxFileSize", cosMaxFileSize);
        // Do not include secret ID/key for security
        return summary;
    }
}