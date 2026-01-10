package com.sdd.etl.source.extract.cos.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Metadata about a COS file to be processed.
 * Contains information about object key, size, last modified timestamp, etc.
 */
public class CosFileMetadata {

    private String key;
    private Long size;
    private Instant lastModified;
    private String etag;
    private String storageClass;

    /**
     * Constructs an empty CosFileMetadata.
     */
    public CosFileMetadata() {
    }

    /**
     * Constructs a CosFileMetadata with all fields.
     * @param key full object key/path in COS
     * @param size file size in bytes
     * @param lastModified last modification timestamp
     * @param etag object ETag for integrity (optional)
     * @param storageClass COS storage class (optional)
     */
    public CosFileMetadata(String key, Long size, Instant lastModified, String etag, String storageClass) {
        this.key = key;
        this.size = size;
        this.lastModified = lastModified;
        this.etag = etag;
        this.storageClass = storageClass;
    }

    /**
     * Gets the full object key/path in COS.
     * @return object key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the full object key/path in COS.
     * @param key object key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Gets the file size in bytes.
     * @return file size, or null if unknown
     */
    public Long getSize() {
        return size;
    }

    /**
     * Sets the file size in bytes.
     * @param size file size
     */
    public void setSize(Long size) {
        this.size = size;
    }

    /**
     * Gets the last modification timestamp.
     * @return last modified timestamp, or null if unknown
     */
    public Instant getLastModified() {
        return lastModified;
    }

    /**
     * Sets the last modification timestamp.
     * @param lastModified last modified timestamp
     */
    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Gets the object ETag for integrity.
     * @return ETag, or null if not available
     */
    public String getEtag() {
        return etag;
    }

    /**
     * Sets the object ETag for integrity.
     * @param etag ETag
     */
    public void setEtag(String etag) {
        this.etag = etag;
    }

    /**
     * Gets the COS storage class.
     * @return storage class, or null if not specified
     */
    public String getStorageClass() {
        return storageClass;
    }

    /**
     * Sets the COS storage class.
     * @param storageClass storage class
     */
    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    /**
     * Checks if this metadata is valid (key is required).
     * @return true if key is non-empty
     */
    public boolean isValid() {
        return key != null && !key.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CosFileMetadata that = (CosFileMetadata) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(size, that.size) &&
                Objects.equals(lastModified, that.lastModified) &&
                Objects.equals(etag, that.etag) &&
                Objects.equals(storageClass, that.storageClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, size, lastModified, etag, storageClass);
    }

    @Override
    public String toString() {
        return "CosFileMetadata{" +
                "key='" + key + '\'' +
                ", size=" + size +
                ", lastModified=" + lastModified +
                ", etag='" + etag + '\'' +
                ", storageClass='" + storageClass + '\'' +
                '}';
    }
}