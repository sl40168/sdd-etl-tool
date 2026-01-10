package com.sdd.etl.source.extract.cos;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.model.SourceDataModel;
import com.sdd.etl.source.extract.cos.config.CosSourceConfig;
import com.sdd.etl.source.extract.cos.model.CosFileMetadata;
import com.sdd.etl.source.extract.cos.model.RawQuoteRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Integration test for XbondQuoteExtractor with mocked COS client.
 * 
 * <p>Verifies that the extractor correctly:
 * <ol>
 *   <li>Selects files based on category and business date</li>
 *   <li>Downloads files from COS</li>
 *   <li>Parses CSV files into raw records</li>
 *   <li>Converts raw records to SourceDataModel</li>
 * </ol>
 * 
 * <p>Uses Mockito to mock the CosClient and stub parsing/conversion methods
 * to isolate the extractor's flow logic.</p>
 */
public class XbondQuoteExtractorIntegrationTest {

    private XbondQuoteExtractor extractor;
    private CosClient mockCosClient;
    private CosSourceConfig mockSourceConfig;
    private ETLContext mockContext;
    
    @Before
    public void setUp() throws ETLException {
        // Create spy on the actual extractor
        extractor = spy(new XbondQuoteExtractor());
        
        // Mock dependencies
        mockCosClient = mock(CosClient.class);
        mockSourceConfig = mock(CosSourceConfig.class);
        mockContext = mock(ETLContext.class);
        
        // Set up context
        when(mockContext.getCurrentDate()).thenReturn("20250101");
        when(mockContext.getConfig()).thenReturn(null); // No config needed for basic tests
        
        // Inject mocked CosClient into extractor for testing
        extractor.setCosClientForTesting(mockCosClient);
        // Inject mocked source config (normally found via findCosSourceConfig)
        // We'll bypass setup by directly setting protected fields
        // This is a bit hacky but acceptable for integration testing
        extractor.sourceConfig = mockSourceConfig;
        when(mockSourceConfig.getMaxFileSizeOrDefault()).thenReturn(100L * 1024 * 1024); // 100MB default
        extractor.tempDirectory = new File(System.getProperty("java.io.tmpdir"));
    }
    
    @Test
    public void testExtract_NoFilesSelected_ReturnsEmptyList() throws ETLException {
        // Given: no files match the selection pattern
        when(mockCosClient.listObjects(any(CosSourceConfig.class), anyString()))
                .thenReturn(new ArrayList<>());
        
        // When
        List<SourceDataModel> result = extractor.extract(mockContext);
        
        // Then
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty when no files selected", result.isEmpty());
        verify(mockCosClient, times(1)).listObjects(any(CosSourceConfig.class), anyString());
        verify(mockCosClient, never()).downloadObject(any(CosSourceConfig.class), anyString());
    }
    
    @Test
    public void testExtract_SingleFileSelected_DownloadsAndParses() throws ETLException {
        // Given: one matching file
        CosFileMetadata fileMetadata = new CosFileMetadata(
                "AllPriceDepth/20250101/file1.csv", 1024L, Instant.ofEpochMilli(123456789L), "etag123", "STANDARD");
        List<CosFileMetadata> fileList = new ArrayList<>();
        fileList.add(fileMetadata);
        
        when(mockCosClient.listObjects(any(CosSourceConfig.class), anyString()))
                .thenReturn(fileList);
        when(mockCosClient.downloadObject(any(CosSourceConfig.class), anyString()))
                .thenReturn(new ByteArrayInputStream("csv,data".getBytes()));
        
        // Stub parsing method to return test records
        List<RawQuoteRecord> rawRecords = createTestRawRecords(5);
        doReturn(rawRecords).when(extractor).parseCsvFile(any(File.class));
        
        // Stub conversion method to return test SourceDataModel
        List<SourceDataModel> sourceModels = createTestSourceModels(5);
        doReturn(sourceModels).when(extractor).convertRawRecords(anyList());
        
        // When
        List<SourceDataModel> result = extractor.extract(mockContext);
        
        // Then
        assertNotNull("Result should not be null", result);
        assertEquals("Should return expected number of records", 5, result.size());
        verify(mockCosClient, times(1)).listObjects(any(CosSourceConfig.class), anyString());
        verify(mockCosClient, times(1)).downloadObject(any(CosSourceConfig.class), 
                eq("AllPriceDepth/20250101/file1.csv"));
        verify(extractor, times(1)).parseCsvFile(any(File.class));
        verify(extractor, times(1)).convertRawRecords(eq(rawRecords));
    }
    
    @Test
    public void testExtract_MultipleFilesSelected_DownloadsAll() throws ETLException {
        // Given: three matching files
        List<CosFileMetadata> fileList = new ArrayList<>();
        fileList.add(new CosFileMetadata("AllPriceDepth/20250101/file1.csv", 1024L, Instant.ofEpochMilli(123456789L), "etag1", "STANDARD"));
        fileList.add(new CosFileMetadata("AllPriceDepth/20250101/file2.csv", 2048L, Instant.ofEpochMilli(123456790L), "etag2", "STANDARD"));
        fileList.add(new CosFileMetadata("AllPriceDepth/20250101/file3.csv", 4096L, Instant.ofEpochMilli(123456791L), "etag3", "STANDARD"));
        
        when(mockCosClient.listObjects(any(CosSourceConfig.class), anyString()))
                .thenReturn(fileList);
        when(mockCosClient.downloadObject(any(CosSourceConfig.class), anyString()))
                .thenReturn(new ByteArrayInputStream("csv,data".getBytes()));
        
        // Stub parsing and conversion
        List<RawQuoteRecord> rawRecords = createTestRawRecords(10);
        doReturn(rawRecords).when(extractor).parseCsvFile(any(File.class));
        List<SourceDataModel> sourceModels = createTestSourceModels(10);
        doReturn(sourceModels).when(extractor).convertRawRecords(anyList());
        
        // When
        List<SourceDataModel> result = extractor.extract(mockContext);
        
        // Then
        assertEquals("Should return expected number of records", 10, result.size());
        verify(mockCosClient, times(1)).listObjects(any(CosSourceConfig.class), anyString());
        verify(mockCosClient, times(3)).downloadObject(any(CosSourceConfig.class), anyString());
        verify(extractor, times(3)).parseCsvFile(any(File.class));
        verify(extractor, times(1)).convertRawRecords(anyList());
    }
    
    @Test(expected = ETLException.class)
    public void testExtract_DownloadFails_ThrowsETLException() throws ETLException {
        // Given: one matching file but download fails
        CosFileMetadata fileMetadata = new CosFileMetadata(
                "AllPriceDepth/20250101/file1.csv", 1024L, Instant.ofEpochMilli(123456789L), "etag123", "STANDARD");
        List<CosFileMetadata> fileList = new ArrayList<>();
        fileList.add(fileMetadata);
        
        when(mockCosClient.listObjects(any(CosSourceConfig.class), anyString()))
                .thenReturn(fileList);
        when(mockCosClient.downloadObject(any(CosSourceConfig.class), anyString()))
                .thenThrow(new RuntimeException("Network error"));
        
        // When
        extractor.extract(mockContext);
        
        // Then: ETLException is expected
    }
    
    @Test
    public void testGetCategory_ReturnsAllPriceDepth() {
        assertEquals("Category should be AllPriceDepth", "AllPriceDepth", extractor.getCategory());
    }
    
    @Test
    public void testGetName_IncludesCategory() {
        String name = extractor.getName();
        assertNotNull("Name should not be null", name);
        assertTrue("Name should include category or extractor type", 
                name.contains("AllPriceDepth") || name.contains("XbondQuoteExtractor"));
    }
    
    // --- Helper methods for test data ---
    
    private List<RawQuoteRecord> createTestRawRecords(int count) {
        List<RawQuoteRecord> records = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            RawQuoteRecord record = new RawQuoteRecord();
            record.setId((long) i);
            record.setUnderlyingSecurityId("TEST" + i);
            record.setUnderlyingMdEntryType(i % 2); // 0 or 1
            record.setUnderlyingMdEntryPx(100.0 + i);
            record.setUnderlyingMdPriceLevel(i % 6); // 0-5
            record.setUnderlyingMdEntrySize(1000L + i);
            record.setUnderlyingYieldType("YIELD");
            record.setUnderlyingYield(2.5 + i * 0.1);
            record.setTransactTime(LocalDateTime.parse("2025-01-01T00:00:00"));
            record.setMqOffset(i * 100L);
            record.setRecvTime(LocalDateTime.parse("2025-01-01T00:00:00"));
            records.add(record);
        }
        return records;
    }
    
    private List<SourceDataModel> createTestSourceModels(int count) {
        List<SourceDataModel> models = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            // Create a mock SourceDataModel
            SourceDataModel mockModel = mock(SourceDataModel.class);
            when(mockModel.getPrimaryKey()).thenReturn("key_" + i);
            when(mockModel.getSourceType()).thenReturn("XbondQuote");
            models.add(mockModel);
        }
        return models;
    }
    
    @Test(expected = ETLException.class)
    public void testExtract_FileSizeExceedsThreshold_ThrowsETLException() throws ETLException {
        // Given: file size 1024 bytes, max threshold 500 bytes
        doReturn(500L).when(mockSourceConfig).getMaxFileSizeOrDefault();
        CosFileMetadata fileMetadata = new CosFileMetadata(
                "AllPriceDepth/20250101/oversized.csv", 1024L, Instant.ofEpochMilli(123456789L), "etag123", "STANDARD");
        List<CosFileMetadata> fileList = new ArrayList<>();
        fileList.add(fileMetadata);
        
        when(mockCosClient.listObjects(any(CosSourceConfig.class), anyString()))
                .thenReturn(fileList);
        
        // When
        extractor.extract(mockContext);
        
        // Then: ETLException expected due to file size validation
    }
    
    @Test
    public void testExtract_FileSizeWithinThreshold_Succeeds() throws ETLException {
        // Given: file size 1024 bytes, max threshold 2000 bytes
        doReturn(2000L).when(mockSourceConfig).getMaxFileSizeOrDefault();
        CosFileMetadata fileMetadata = new CosFileMetadata(
                "AllPriceDepth/20250101/acceptable.csv", 1024L, Instant.ofEpochMilli(123456789L), "etag123", "STANDARD");
        List<CosFileMetadata> fileList = new ArrayList<>();
        fileList.add(fileMetadata);
        
        when(mockCosClient.listObjects(any(CosSourceConfig.class), anyString()))
                .thenReturn(fileList);
        when(mockCosClient.downloadObject(any(CosSourceConfig.class), anyString()))
                .thenReturn(new ByteArrayInputStream("csv,data".getBytes()));
        
        // Stub parsing and conversion to return empty list (simplify test)
        doReturn(new ArrayList<RawQuoteRecord>()).when(extractor).parseCsvFile(any(File.class));
        doReturn(new ArrayList<SourceDataModel>()).when(extractor).convertRawRecords(anyList());
        
        // When
        List<SourceDataModel> result = extractor.extract(mockContext);
        
        // Then: extraction succeeds (no exception)
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mockCosClient, times(1)).downloadObject(any(CosSourceConfig.class), anyString());
    }
}