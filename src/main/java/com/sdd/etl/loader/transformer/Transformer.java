package com.sdd.etl.loader.transformer;

import com.sdd.etl.loader.transformer.exceptions.TransformationException;
import com.sdd.etl.model.SourceDataModel;
import com.sdd.etl.model.TargetDataModel;
import java.util.List;

/**
 * Transformer API for converting source data models to target data models.
 * <p>
 * Implementations MUST follow these contracts:
 * <ul>
 *   <li>One-to-one transformation: Each source record produces exactly one target record</li>
 *   <li>Field mapping based on field names (case-sensitive)</li>
 *   <li>Missing source fields result in unassigned target fields</li>
 *   <li>Type conversion between compatible types</li>
 *   <li>Throw TransformationException on any failure</li>
 * </ul>
 * </p>
 * 
 * @param <S> Source data model type (must extend SourceDataModel)
 * @param <T> Target data model type (must extend TargetDataModel)
 * @since 1.0.0
 */
public interface Transformer<S extends SourceDataModel, T extends TargetDataModel> {

    /**
     * Transforms a list of source records to target records.
     * <p>
     * Records are transformed one by one (not batch transformation).
     * </p>
     *
     * @param sourceRecords List of source records (must be non-null, same type S)
     * @return List of transformed target records (same type T, same size as input)
     * @throws TransformationException if any record transformation fails
     * @throws IllegalArgumentException if sourceRecords is null
     */
    List<T> transform(List<S> sourceRecords) throws TransformationException;

    /**
     * Gets the source data model class this transformer supports.
     *
     * @return Source data model class (never null)
     */
    Class<S> getSourceType();

    /**
     * Gets the target data model class this transformer produces.
     *
     * @return Target data model class (never null)
     */
    Class<T> getTargetType();
}
