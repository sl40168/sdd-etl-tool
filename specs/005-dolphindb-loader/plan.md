# Implementation Plan: Load Data to DolphinDB

**Branch**: `005-dolphindb-loader` | **Date**: 2026-01-11 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/005-dolphindb-loader/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Load transformed data to DolphinDB using its Java API with a daily ETL process. Create temporary tables before loading, sort TargetDataModel records by specified fields, load data sequentially into target tables based on data type, and clean up temporary tables after validation. Integrate with existing LoadSubprocess and CleanSubprocess for orchestration, and provide a common Loader API for future extension to other target systems.

## Technical Context

**Language/Version**: Java 8 (non-negotiable per constitution)  
**Primary Dependencies**: DolphinDB Java API, Apache Commons CLI, Apache Commons Configuration (INI parsing), JUnit 4, Mockito, SLF4J + Logback (logging)  
**Storage**: DolphinDB database (target system)  
**Testing**: JUnit 4, Mockito (unit testing), integration testing with embedded DolphinDB instance  
**Target Platform**: CLI tool running on Windows/Linux/macOS with Java 8 JRE  
**Project Type**: Single CLI tool (ETL pipeline component)  
**Performance Goals**: Load 1 million records within 30 minutes for daily ETL process  
**Constraints**: Must use DolphinDB Java API, temporary tables must be cleaned up, process must stop on any exception with descriptive error messages, must provide common Loader API for extensibility  
**Scale/Scope**: Daily ETL process for financial market data (xbond quote, xbond trade, bond future quote) with support for future target systems

## Design Approach

### Single Concrete Loader Architecture
The implementation uses **one concrete loader class** (`DolphinDBLoader`) that handles all three data types (Xbond Quote, Xbond Trade, Bond Future Quote). This design choice simplifies maintenance and aligns with the common Loader API pattern.

**Key Design Decisions**:
1. **Unified Loader**: A single `DolphinDBLoader` class implements the `Loader` interface and processes all `TargetDataModel` subtypes.
2. **Configuration‑Driven Table Mapping**: Target table names are configured via `LoaderConfiguration.targetTableMappings` (INI key `target.table.mappings`), mapping each data type to its destination table.
3. **Record Grouping**: The loader groups incoming records by their `dataType` field, using the configured mappings to determine the target table for each group.
4. **Sequential Loading by Data Type**: Within each group, records are sorted by configured fields (e.g., `receive_time`), then loaded into the corresponding target table via DolphinDB Java API.
5. **Column‑Based Array Conversion**: For efficient bulk insertion, record‑oriented data is transformed into column‑oriented arrays (one array per field) before being passed to DolphinDB's `tableInsert`.

**How It Works**:
1. The `LoadSubprocess` instantiates `DolphinDBLoader` and calls `init()` with configuration.
2. `createTemporaryTables()` creates temporary tables for each target table (immutable names).
3. `sortData()` groups records by data type and sorts each group by configured fields.
4. `loadData()` converts sorted records to column arrays and inserts them into temporary tables, then appends to target tables.
5. `validateLoad()` compares row counts and ensures data integrity.
6. `CleanSubprocess` calls `cleanupTemporaryTables()` and `shutdown()`.

**Advantages**:
- **Simplified Codebase**: One loader class instead of three separate implementations.
- **Easy Extensibility**: Adding a new data type requires only a new `TargetDataModel` subclass and an entry in the table mapping configuration.
- **Consistent Error Handling**: Centralized exception management and logging.
- **Performance**: Batch processing and column‑wise insertion optimize throughput.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

The design complies with all principles of the project constitution:

1. **Java 8 platform compliance**: Implementation targets Java 8 (non‑negotiable per constitution).
2. **Maven build tool usage**: Build uses Maven wrapper; dependencies are managed via `pom.xml`.
3. **CLI interface only**: The ETL tool is CLI‑based; no GUI or web interface.
4. **INI configuration format**: Loader configuration is read from INI file (`[loader]` section).
5. **Component boundary clarity**: Clear separation between common Loader API, DolphinDB‑specific implementation, configuration, and subprocess integration.
6. **TDD with >60% coverage**: Unit tests will be written before implementation; coverage target >60%.
7. **Bug fix version recording**: Bug fixes will be recorded with version numbers.
8. **Third‑party library usage**: Uses DolphinDB Java API, Apache Commons CLI/Configuration, JUnit 4, Mockito, SLF4J/Logback.
9. **Full build/test pass**: At the end of each implementation round, all tests must pass.
10. **Protected files restriction**: No protected files have been modified.
11. **Primitive number field initialization**: Numeric fields will be explicitly initialized; default zero values will be avoided.

No violations identified. (Note: FR‑010 and FR‑011 have been added to spec.md to explicitly address constitution principles PR‑7 and PR‑11.)

## Project Structure

### Documentation (this feature)

```text
specs/005-dolphindb-loader/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/main/java/com/sdd/etl/
├── context/                    # Existing ETL context classes (ETLContext, etc.)
├── subprocess/                 # Existing subprocess classes (LoadSubprocess, CleanSubprocess)
└── loader/                     # NEW: Data loader components
    ├── api/                    # Common Loader API interfaces
    ├── dolphin/                # DolphinDB-specific implementation
    └── config/                 # Loader configuration classes

src/main/resources/
├── scripts/                    # SQL/DOS scripts for temporary table operations
│   ├── temporary_table_creation.dos
│   └── temporary_table_deletion.dos
└── logback.xml                 # Existing logging configuration

src/test/java/com/sdd/etl/
├── loader/                     # Unit tests for loader components
│   ├── dolphin/
│   └── integration/
└── subprocess/                 # Existing subprocess tests
```

**Structure Decision**: The project follows the existing single-project Maven structure. New loader components will be added under `com.sdd.etl.loader` package, with API interfaces in `loader/api/`, DolphinDB implementation in `loader/dolphin/`, and configuration in `loader/config/`. Scripts for temporary table operations will be stored in `src/main/resources/scripts/`.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
