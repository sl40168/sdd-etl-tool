package com.sdd.etl.source.extract.cos;

import com.sdd.etl.ETLException;
import com.sdd.etl.source.extract.cos.config.CosSourceConfig;
import com.sdd.etl.source.extract.cos.model.CosFileMetadata;
import java.io.InputStream;
import java.util.List;

/**
 * Interface for COS (Cloud Object Storage) client operations.
 * Abstracts interactions with cloud storage services.
 */
public interface CosClient {

    /**
     * Lists objects in the specified bucket with given prefix.
     * 
     * @param config COS source configuration
     * @param prefix path prefix for filtering (may be null or empty for all objects)
     * @return list of metadata for matching objects
     * @throws ETLException if listing fails
     */
    List<CosFileMetadata> listObjects(CosSourceConfig config, String prefix) throws ETLException;

    /**
     * Downloads an object from COS as an input stream.
     * Caller is responsible for closing the stream.
     * 
     * @param config COS source configuration
     * @param key full object key/path
     * @return input stream for reading object data
     * @throws ETLException if download fails
     */
    InputStream downloadObject(CosSourceConfig config, String key) throws ETLException;

    /**
     * Closes the client and releases any resources.
     * 
     * @throws ETLException if closure fails
     */
    void close() throws ETLException;
}