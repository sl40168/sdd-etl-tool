package com.sdd.etl.loader.transformer;

import com.sdd.etl.loader.transformer.exceptions.TransformationException;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for selecting appropriate Transformer based on source data type.
 * <p>
 * This factory provides thread-safe access to transformer instances and
 * supports automatic registration of new transformers.
 * </p>
 * 
 * @since 1.0.0
 */
public class TransformerFactory {

    private static final Map<Class<?>, Transformer<?, ?>> TRANSFORMERS = new HashMap<>();

    static {
        // Register all available transformers
        register(new XbondQuoteTransformer());
        register(new XbondTradeTransformer());
        register(new BondFutureQuoteTransformer());
    }

    /**
     * Gets transformer for specified source data type.
     *
     * @param sourceType Source data model class (must be non-null)
     * @return Transformer instance (never null)
     * @throws TransformationException if no transformer is registered for sourceType
     * @throws IllegalArgumentException if sourceType is null
     */
    public static Transformer<?, ?> getTransformer(Class<?> sourceType)
            throws TransformationException {

        if (sourceType == null) {
            throw new IllegalArgumentException("Source type cannot be null");
        }

        Transformer<?, ?> transformer = TRANSFORMERS.get(sourceType);
        
        if (transformer == null) {
            throw new TransformationException(
                    sourceType.getSimpleName(),
                    0,
                    "No transformer found for source type: " + sourceType.getName()
            );
        }

        return transformer;
    }

    /**
     * Registers a transformer in the factory.
     * <p>
     * This method is typically called during static initialization,
     * but can also be called to add new transformers at runtime.
     * </p>
     *
     * @param transformer Transformer instance to register (must not be null)
     * @throws IllegalArgumentException if transformer is null
     * @throws IllegalArgumentException if transformer's source type is already registered
     */
    public static void register(Transformer<?, ?> transformer) {
        if (transformer == null) {
            throw new IllegalArgumentException("Transformer cannot be null");
        }

        Class<?> sourceType = transformer.getSourceType();
        
        if (TRANSFORMERS.containsKey(sourceType)) {
            throw new IllegalArgumentException(
                    "Transformer already registered for source type: " + sourceType.getName());
        }

        TRANSFORMERS.put(sourceType, transformer);
    }

    /**
     * Checks if a transformer is registered for the given source type.
     *
     * @param sourceType Source data model class to check
     * @return true if a transformer is registered, false otherwise
     */
    public static boolean hasTransformer(Class<?> sourceType) {
        return TRANSFORMERS.containsKey(sourceType);
    }

    /**
     * Gets the number of registered transformers.
     *
     * @return Number of registered transformers
     */
    public static int getRegisteredCount() {
        return TRANSFORMERS.size();
    }
}
