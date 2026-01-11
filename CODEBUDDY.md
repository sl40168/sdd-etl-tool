# sdd-etl-tool Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-01-11

## Active Technologies
- Java 8 (non-negotiable per Constitution) + Maven wrapper, Apache Commons CLI, Apache Commons Configuration (INI parsing), JUnit 4, Mockito, SLF4J + Logback (logging) (001-cos-xbond-quote-extract)
- Java 8 (non-negotiable per constitution) + Apache Commons Configuration (INI parsing), JUnit 4 (testing), Maven wrapper (002-date-api-refactor)
- N/A (config files only) (002-date-api-refactor)
- Java 8 (non-negotiable per constitution) + DolphinDB Java API, Apache Commons CLI, Apache Commons Configuration (INI parsing), JUnit 4, Mockito, SLF4J + Logback (logging) (005-dolphindb-loader)
- DolphinDB database (target system) (005-dolphindb-loader)
- Java 8 + SLF4J + Logback (logging), JUnit 4 (testing), Mockito (mocking), Apache Commons Configuration (already in project) (006-data-transform)
- DolphinDB (via existing DolphinDBLoader integration) (006-data-transform)

- Java 8 (non-negotiable per Constitution) + Maven wrapper, Apache Commons CLI, Apache Commons Configuration (INI parsing), JUnit 4, Mockito, SLF4J + Logback (logging) (001-etl-core-workflow)

## Project Structure

```text
src/
  main/
    java/
      com/sdd/etl/
        loader/          # DolphinDB data loader module
          api/            # Loader interface and exceptions
          config/         # Loader configuration parsing
          dolphin/         # DolphinDB-specific implementation
          annotation/      # @ColumnOrder annotation
          model/          # TargetDataModel concrete classes
          transformer/     # Data transformation pipeline (006-data-transform)
            Transformer.java              # Generic transformation API
            AbstractTransformer.java       # Base class with reflection-based mapping
            TransformerFactory.java        # Factory for selecting transformer by type
            exceptions/
              TransformationException.java # Custom exception for transformation failures
            XbondQuoteTransformer.java     # Xbond quote transformer (83 fields)
            XbondTradeTransformer.java     # Xbond trade transformer (15 fields)
            BondFutureQuoteTransformer.java # Bond future quote transformer (96 fields)
        subprocess/
          TransformSubprocess.java  # Orchestrates concurrent transformations (006-data-transform)
    resources/
      scripts/         # DolphinDB table scripts
  test/
    java/
      com/sdd/etl/
        loader/          # Loader tests
          transformer/
            TransformerTest.java            # Abstract test base class
            TransformerFactoryTest.java
            XbondQuoteTransformerTest.java
            XbondTradeTransformerTest.java
            BondFutureQuoteTransformerTest.java
        subprocess/
          TransformSubprocessTest.java
```

## Commands

# Add commands for Java 8 (non-negotiable per Constitution)

## Code Style

Java 8 (non-negotiable per Constitution): Follow standard conventions

## Architecture Patterns

### Transformer Pattern (006-data-transform)

The data transformation pipeline uses a reflection-based transformer pattern:

- **Generic API**: `Transformer<S extends SourceDataModel, T extends TargetDataModel>` defines the contract
- **Abstract Base**: `AbstractTransformer<S, T>` provides common implementation:
  - Field caching using `java.lang.reflect.Field` for performance
  - Name-based field mapping between source and target models
  - Automatic type conversions (String→LocalDate, LocalDateTime→Instant, Long→double, Integer→int)
  - Null-safe value conversion with sentinel values (-1 for int, Double.NaN for double)
  - Graceful handling of missing fields (returns null for absent fields)
- **Factory Pattern**: `TransformerFactory` selects concrete transformers based on `DataType`
- **Exception Handling**: `TransformationException` wraps failures with context (source type, target type, record count)

### Concurrent Processing Pattern

`TransformSubprocess` uses `ExecutorService` for concurrent transformation:

- Fixed thread pool sized to number of non-empty data types
- Each transformation submitted as `Callable<TransformResult>`
- `invokeAll()` for concurrent execution with blocking wait
- Immediate failure halt on any exception (cancels all pending tasks)
- Results stored back into `ETLContext`

### Sentinel Value Strategy

Per Constitution Principle 11 (no default zero initialization):

| Target Type | Sentinel Value | When Used |
|------------|--------------|-----------|
| int | -1 | Source Integer field is null |
| double | Double.NaN | Source Double/Long field is null |
| String | null | Source String field is null |
| LocalDate | null | Source String field cannot be parsed |
| Instant | null | Source LocalDateTime field is null |

## Recent Changes
- 006-data-transform: Added Java 8 + SLF4J + Logback (logging), JUnit 4 (testing), Mockito (mocking), Apache Commons Configuration (already in project)
- 005-dolphindb-loader: Completed DolphinDB loader implementation with @ColumnOrder annotations, shared connection management, and subprocess integration (2026-01-11)
- 002-date-api-refactor: Added Java 8 (non-negotiable per constitution) + Apache Commons Configuration (INI parsing), JUnit 4 (testing), Maven wrapper


<!-- MANUAL ADDITIONS START -->
- 006-data-transform: Completed data transformation pipeline with reflection-based field mapping, concurrent processing (ExecutorService), performance benchmarks (10K records < 30s), and full test coverage (>60%)
<!-- MANUAL ADDITIONS END -->
