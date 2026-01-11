# sdd-etl-tool Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-01-11

## Active Technologies
- Java 8 (non-negotiable per Constitution) + Maven wrapper, Apache Commons CLI, Apache Commons Configuration (INI parsing), JUnit 4, Mockito, SLF4J + Logback (logging) (001-cos-xbond-quote-extract)
- Java 8 (non-negotiable per constitution) + Apache Commons Configuration (INI parsing), JUnit 4 (testing), Maven wrapper (002-date-api-refactor)
- N/A (config files only) (002-date-api-refactor)
- Java 8 (non-negotiable per constitution) + DolphinDB Java API, Apache Commons CLI, Apache Commons Configuration (INI parsing), JUnit 4, Mockito, SLF4J + Logback (logging) (005-dolphindb-loader)
- DolphinDB database (target system) (005-dolphindb-loader)

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
    resources/
      scripts/         # DolphinDB table scripts
  test/
    java/
      com/sdd/etl/
        loader/          # Loader tests
```

## Commands

# Add commands for Java 8 (non-negotiable per Constitution)

## Code Style

Java 8 (non-negotiable per Constitution): Follow standard conventions

## Recent Changes
- 005-dolphindb-loader: Completed DolphinDB loader implementation with @ColumnOrder annotations, shared connection management, and subprocess integration (2026-01-11)
- 002-date-api-refactor: Added Java 8 (non-negotiable per constitution) + Apache Commons Configuration (INI parsing), JUnit 4 (testing), Maven wrapper
- 001-cos-xbond-quote-extract: Added Java 8 (non-negotiable per Constitution) + Maven wrapper, Apache Commons CLI, Apache Commons Configuration (INI parsing), JUnit 4, Mockito, SLF4J + Logback (logging)


<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
