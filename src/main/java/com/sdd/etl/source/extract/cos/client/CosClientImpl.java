package com.sdd.etl.source.extract.cos.client;

import com.sdd.etl.ETLException;
import com.sdd.etl.source.extract.cos.CosClient;
import com.sdd.etl.source.extract.cos.config.CosSourceConfig;
import com.sdd.etl.source.extract.cos.model.CosFileMetadata;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.region.Region;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tencent COS SDK implementation of the CosClient interface.
 * 
 * <p>This class wraps the Tencent COS SDK to provide cloud storage operations
 * for the ETL extractor. It handles authentication, connection management,
 * and error translation to ETLException.</p>
 * 
 * <p><strong>Thread Safety</strong>: Instances of this class are NOT thread-safe.
 * Each extractor should create its own CosClient instance.</p>
 */
public class CosClientImpl implements CosClient {

    /** Logger for performance monitoring */
    private static final Logger logger = LoggerFactory.getLogger(CosClientImpl.class);
    
    /** Underlying Tencent COS client */
    private COSClient cosClient;
    
    /** Whether this client has been closed */
    private volatile boolean closed = false;
    
    /**
     * Creates a new CosClientImpl instance.
     * The client must be initialized with configuration before use.
     */
    public CosClientImpl() {
        // Client will be lazily initialized when needed
    }
    
    /**
     * Initializes the COS client with the given configuration.
     * Must be called before any other methods.
     * 
     * @param config COS source configuration
     * @throws ETLException if configuration is invalid or client initialization fails
     */
    private void initClient(CosSourceConfig config) throws ETLException {
        if (cosClient != null) {
            return; // Already initialized
        }
        
        if (config == null) {
            throw new ETLException("COS_CLIENT", null, "Configuration cannot be null");
        }
        
        if (!config.isValid()) {
            throw new ETLException("COS_CLIENT", null, 
                    "Invalid COS configuration: " + config);
        }
        
        try {
            // Create credentials
            COSCredentials cred = new BasicCOSCredentials(
                    config.getSecretId(), 
                    config.getSecretKey());
            
            // Configure region
            Region region = new Region(config.getRegion() != null ? 
                    config.getRegion() : "ap-shanghai");
            
            // Create client configuration
            ClientConfig clientConfig = new ClientConfig(region);
            // Set additional configuration if needed
            
            // Create COS client
            cosClient = new COSClient(cred, clientConfig);
            
        } catch (Exception e) {
            throw new ETLException("COS_CLIENT", null,
                    "Failed to initialize COS client: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<CosFileMetadata> listObjects(CosSourceConfig config, String prefix) 
            throws ETLException {
        
        checkClosed();
        initClient(config);
        
        long startTimeNanos = System.nanoTime();
        beforeListObjects(config, prefix);
        
        String bucketName = config.getBucket();
        String actualPrefix = prefix != null ? prefix : config.getPrefix();
        
        ListObjectsRequest request = new ListObjectsRequest();
        request.setBucketName(bucketName);
        if (actualPrefix != null && !actualPrefix.isEmpty()) {
            request.setPrefix(actualPrefix);
        }
        
        try {
            ObjectListing objectListing = cosClient.listObjects(request);
            List<CosFileMetadata> result = new ArrayList<>();
            
            for (COSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                CosFileMetadata metadata = new CosFileMetadata(
                        objectSummary.getKey(),
                        objectSummary.getSize(),
                        objectSummary.getLastModified() != null ? 
                                objectSummary.getLastModified().toInstant() : null,
                        objectSummary.getETag(),
                        objectSummary.getStorageClass()
                );
                result.add(metadata);
            }
            
            afterListObjects(config, prefix, result, startTimeNanos);
            return result;
            
        } catch (Exception e) {
            throw translateCosException(e, "while listing objects", null);
        }
    }
    
    @Override
    public InputStream downloadObject(CosSourceConfig config, String key) 
            throws ETLException {
        
        checkClosed();
        initClient(config);
        
        String bucketName = config.getBucket();
        
        if (key == null || key.trim().isEmpty()) {
            throw new ETLException("COS_CLIENT", null,
                    "Object key cannot be null or empty");
        }
        
        long startTimeNanos = System.nanoTime();
        beforeDownloadObject(config, key);
        
        try {
            GetObjectRequest request = new GetObjectRequest(bucketName, key);
            COSObject object = cosClient.getObject(request);
            InputStream result = object.getObjectContent();
            
            afterDownloadObject(config, key, result, startTimeNanos);
            return result;
            
        } catch (Exception e) {
            throw translateCosException(e, "while downloading object", key);
        }
    }
    
    @Override
    public void close() throws ETLException {
        if (closed) {
            return;
        }
        
        if (cosClient != null) {
            try {
                cosClient.shutdown();
            } catch (Exception e) {
                throw translateCosException(e, "while closing COS client", null);
            } finally {
                cosClient = null;
                closed = true;
            }
        }
    }
    
    /**
     * Checks if this client has been closed.
     * 
     * @throws ETLException if client is closed
     */
    private void checkClosed() throws ETLException {
        if (closed) {
            throw new ETLException("COS_CLIENT", null,
                    "COS client has been closed");
        }
    }

    /**
     * Translates Tencent COS SDK exceptions to ETLException.
     *
     * @param e the COS exception
     * @param context descriptive context for error message (e.g., "while listing objects")
     * @param key optional object key for context
     * @return ETLException with appropriate message
     */
    private ETLException translateCosException(Exception e, String context, String key) {
        String message;
        if (e instanceof CosServiceException) {
            CosServiceException serviceEx = (CosServiceException) e;
            message = String.format("COS service error %s: %s", context, serviceEx.getErrorMessage());
        } else if (e instanceof CosClientException) {
            CosClientException clientEx = (CosClientException) e;
            message = String.format("COS client error %s: %s", context, clientEx.getMessage());
        } else {
            message = String.format("Unexpected error %s: %s", context, e.getMessage());
        }
        
        if (key != null && !key.trim().isEmpty()) {
            message = message + " (key: " + key + ")";
        }
        
        return new ETLException("COS_CLIENT", null, message, e);
    }
    
    /**
     * Hook method called before listObjects operation starts.
     * Logs at DEBUG level for performance monitoring.
     * 
     * @param config COS source configuration
     * @param prefix file prefix to list
     */
    private void beforeListObjects(CosSourceConfig config, String prefix) {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting listObjects operation - bucket: {}, prefix: {}", 
                    config.getBucket(), prefix != null ? prefix : "default");
        }
    }
    
    /**
     * Hook method called after listObjects operation completes.
     * Logs performance metrics including duration and result count.
     * 
     * @param config COS source configuration
     * @param prefix file prefix that was listed
     * @param result list of file metadata returned
     * @param startTimeNanos start time in nanoseconds (from System.nanoTime())
     */
    private void afterListObjects(CosSourceConfig config, String prefix, 
            List<CosFileMetadata> result, long startTimeNanos) {
        long durationNanos = System.nanoTime() - startTimeNanos;
        double durationMillis = durationNanos / 1_000_000.0;
        
        if (logger.isDebugEnabled()) {
            logger.debug("Completed listObjects operation - bucket: {}, prefix: {}, " +
                    "files found: {}, duration: {:.2f} ms",
                    config.getBucket(), 
                    prefix != null ? prefix : "default",
                    result != null ? result.size() : 0,
                    durationMillis);
        }
    }
    
    /**
     * Hook method called before downloadObject operation starts.
     * Logs at DEBUG level for performance monitoring.
     * 
     * @param config COS source configuration
     * @param key object key to download
     */
    private void beforeDownloadObject(CosSourceConfig config, String key) {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting downloadObject operation - bucket: {}, key: {}", 
                    config.getBucket(), key);
        }
    }
    
    /**
     * Hook method called after downloadObject operation completes.
     * Logs performance metrics including duration.
     * 
     * @param config COS source configuration
     * @param key object key that was downloaded
     * @param result input stream for the downloaded object
     * @param startTimeNanos start time in nanoseconds (from System.nanoTime())
     */
    private void afterDownloadObject(CosSourceConfig config, String key,
            InputStream result, long startTimeNanos) {
        long durationNanos = System.nanoTime() - startTimeNanos;
        double durationMillis = durationNanos / 1_000_000.0;
        
        if (logger.isDebugEnabled()) {
            logger.debug("Completed downloadObject operation - bucket: {}, key: {}, " +
                    "duration: {:.2f} ms",
                    config.getBucket(), key, durationMillis);
        }
    }
    
    /**
     * Gets the underlying Tencent COS client (for testing purposes).
     * 
     * @return the COS client, or null if not initialized
     */
    COSClient getInternalClient() {
        return cosClient;
    }
}