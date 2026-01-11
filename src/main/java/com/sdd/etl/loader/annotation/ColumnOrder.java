package com.sdd.etl.loader.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify the order of fields in DolphinDB column-based storage.
 * This is critical for DolphinDB's columnar data structure where field order
 * must match the database schema definition.
 *
 * @see com.sdd.etl.loader.dolphin.DataConverter
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ColumnOrder {

    /**
     * The zero-based position of this field in the DolphinDB table schema.
     * Must be unique within a model class.
     *
     * @return the field position (0, 1, 2, ...)
     */
    int value();
}
