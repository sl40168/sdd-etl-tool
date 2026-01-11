package com.sdd.etl.loader.dolphin.sort;

import com.sdd.etl.loader.api.exceptions.LoaderException;
import com.sdd.etl.model.TargetDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * External (disk-based) sorter for large datasets that exceed memory limits.
 * Uses temporary files to sort data without loading everything into memory.
 */
public class ExternalSorter {

    private static final Logger logger = LoggerFactory.getLogger(ExternalSorter.class);

    private final long memoryLimitBytes;
    private final String tempDir;

    public ExternalSorter(long memoryLimitBytes, String tempDir) {
        this.memoryLimitBytes = memoryLimitBytes;
        this.tempDir = tempDir != null ? tempDir : System.getProperty("java.io.tmpdir");
    }

    public long getMemoryLimit() {
        return memoryLimitBytes;
    }

    /**
     * Sorts data using external sort algorithm.
     *
     * @param data the data to sort
     * @param sortFieldName the field to sort by (expected to be Instant or timestamp)
     * @return sorted list
     * @throws LoaderException if sorting fails
     */
    @SuppressWarnings("unchecked")
    public <T extends TargetDataModel> List<T> sort(List<T> data, String sortFieldName) throws LoaderException {
        if (data == null || data.isEmpty()) {
            return data;
        }

        logger.info("Starting external sort of {} records by field '{}'", data.size(), sortFieldName);

        try {
            // Phase 1: Split into sorted chunks
            List<File> sortedChunks = createSortedChunks(data, sortFieldName);

            // Phase 2: Merge chunks
            List<T> result = mergeSortedChunks(sortedChunks, sortFieldName);

            // Cleanup
            for (File chunk : sortedChunks) {
                if (!chunk.delete()) {
                    logger.warn("Failed to delete temporary file: {}", chunk.getAbsolutePath());
                }
            }

            logger.info("External sort completed for {} records", result.size());
            return result;
        } catch (Exception e) {
            throw new LoaderException("External sort failed", e);
        }
    }

    /**
     * Splits data into sorted chunks that fit in memory.
     */
    private <T extends TargetDataModel> List<File> createSortedChunks(List<T> data, String sortFieldName)
            throws IOException, ClassNotFoundException {
        List<File> chunks = new ArrayList<>();
        int chunkIndex = 0;

        List<T> chunk = new ArrayList<>();
        long estimatedSize = 0;

        for (T item : data) {
            // Estimate size
            estimatedSize += estimateItemSize(item);

            if (estimatedSize > memoryLimitBytes && !chunk.isEmpty()) {
                // Sort and write this chunk
                sortAndWriteChunk(chunk, sortFieldName, chunkIndex++);
                chunks.add(getChunkFile(chunkIndex));
                chunk = new ArrayList<>();
                estimatedSize = 0;
            }

            chunk.add(item);
        }

        // Write last chunk
        if (!chunk.isEmpty()) {
            sortAndWriteChunk(chunk, sortFieldName, chunkIndex++);
            chunks.add(getChunkFile(chunkIndex));
        }

        return chunks;
    }

    /**
     * Sorts a chunk and writes it to disk.
     */
    private <T extends TargetDataModel> void sortAndWriteChunk(List<T> chunk, String sortFieldName, int index)
            throws IOException {
        // Sort by sortField
        Collections.sort(chunk, (a, b) -> {
            try {
                Field field = findField(a.getClass(), sortFieldName);
                if (field != null) {
                    field.setAccessible(true);
                    Object valueA = field.get(a);
                    Object valueB = field.get(b);

                    if (valueA instanceof Comparable && valueB instanceof Comparable) {
                        return ((Comparable<Object>) valueA).compareTo(valueB);
                    }
                }
            } catch (Exception e) {
                logger.debug("Error comparing items: {}", e.getMessage());
            }
            return 0;
        });

        // Write to file (simplified - in production, use proper serialization)
        File chunkFile = getChunkFile(index);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(chunkFile))) {
            oos.writeObject(chunk);
        }

        logger.debug("Wrote chunk {} with {} records to {}", index, chunk.size(), chunkFile.getAbsolutePath());
    }

    /**
     * Merges sorted chunks into a single sorted list.
     */
    @SuppressWarnings("unchecked")
    private <T extends TargetDataModel> List<T> mergeSortedChunks(List<File> chunks, String sortFieldName)
            throws IOException, ClassNotFoundException {
        if (chunks.isEmpty()) {
            return new ArrayList<>();
        }

        if (chunks.size() == 1) {
            return readChunk(chunks.get(0));
        }

        // Simple 2-way merge (for production, use k-way merge for efficiency)
        List<T> result = readChunk(chunks.get(0));

        for (int i = 1; i < chunks.size(); i++) {
            List<T> nextChunk = readChunk(chunks.get(i));
            result = merge(result, nextChunk, sortFieldName);
        }

        return result;
    }

    /**
     * Merges two sorted lists.
     */
    private <T extends TargetDataModel> List<T> merge(List<T> list1, List<T> list2, String sortFieldName) {
        List<T> result = new ArrayList<>(list1.size() + list2.size());
        int i = 0, j = 0;

        while (i < list1.size() && j < list2.size()) {
            T item1 = list1.get(i);
            T item2 = list2.get(j);

            if (compareByField(item1, item2, sortFieldName) <= 0) {
                result.add(item1);
                i++;
            } else {
                result.add(item2);
                j++;
            }
        }

        while (i < list1.size()) {
            result.add(list1.get(i++));
        }

        while (j < list2.size()) {
            result.add(list2.get(j++));
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private <T extends TargetDataModel> int compareByField(T a, T b, String fieldName) {
        try {
            Field field = findField(a.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                Object valueA = field.get(a);
                Object valueB = field.get(b);

                if (valueA instanceof Comparable && valueB instanceof Comparable) {
                    return ((Comparable<Object>) valueA).compareTo(valueB);
                }
            }
        } catch (Exception e) {
            logger.debug("Error comparing items: {}", e.getMessage());
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    private <T extends TargetDataModel> List<T> readChunk(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<T>) ois.readObject();
        }
    }

    private File getChunkFile(int index) {
        return new File(tempDir, "sort_chunk_" + index + ".tmp");
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    private long estimateItemSize(TargetDataModel item) {
        // Rough estimate: 500 bytes per item
        return 500;
    }
}
