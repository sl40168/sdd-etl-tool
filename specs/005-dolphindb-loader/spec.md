# Feature Specification: Load Data to DolphinDB

**Feature Branch**: `005-dolphindb-loader`  
**Created**: 2026-01-11  
**Status**: Draft  
**Input**: User description: "Load transformed data to DolphinDB via Java API with temporary tables and cleanup"

## Clarifications

### Session 2026-01-11

- Q: How should the loader authenticate with DolphinDB? → A: Use username/password credentials from configuration.
- Q: How should sorting handle null values in sort fields? → A: Skip records with null values in sort fields.
- Q: How should system handle data exceeding memory for sorting? → A: Configurable memory limit with fallback to disk.
- Q: How should system handle partial loading failures? → A: Keep successfully loaded data, leave temporary tables for manual cleanup.
- Q: How should temporary table names be generated? → A: Names generated once and remain immutable.

## User Scenarios & Testing *(mandatory)*

<!--
  IMPORTANT: User stories should be PRIORITIZED as user journeys ordered by importance.
  Each user story/journey must be INDEPENDENTLY TESTABLE - meaning if you implement just ONE of them,
  you should still have a viable MVP (Minimum Viable Product) that delivers value.
  
  Assign priorities (P1, P2, P3, etc.) to each story, where P1 is the most critical.
  Think of each story as a standalone slice of functionality that can be:
  - Developed independently
  - Tested independently
  - Deployed independently
  - Demonstrated to users independently
-->

### User Story 1 - Load transformed data to DolphinDB via Java API (Priority: P1)

As an ETL operator, I want to load transformed data to DolphinDB using its Java API, so that data is available for downstream analytics.

**Why this priority**: This is the core functionality of phase VI, enabling the final step of the ETL pipeline.

**Independent Test**: Can be tested by running the loader with mock transformed data and verifying that data appears in the correct DolphinDB tables.

**Acceptance Scenarios**:

1. **Given** a collection of transformed TargetDataModel instances, **When** the loader executes, **Then** temporary tables are created in DolphinDB.
2. **Given** sorted TargetDataModel instances by specified fields, **When** the loader executes, **Then** data is loaded into appropriate target tables in sequence.

---

### User Story 2 - Integrate loader with existing ETL subprocesses (Priority: P2)

As an ETL operator, I want the loader integrated with LoadSubprocess and CleanSubprocess, so that loading and cleanup are part of the automated ETL workflow.

**Why this priority**: Ensures proper orchestration and cleanup, maintaining a transparent environment for subsequent ETL runs.

**Independent Test**: Can be tested by running the full ETL workflow and verifying that tables are created during loading and removed after cleanup.

**Acceptance Scenarios**:

1. **Given** LoadSubprocess receives transformed data, **When** it executes, **Then** data is loaded to DolphinDB.
2. **Given** CleanSubprocess runs after data validation, **When** it executes, **Then** all temporary tables are removed from DolphinDB.

---

### User Story 3 - Handle exceptions during data loading (Priority: P3)

As an ETL operator, I want any loading exception to break the ETL process with clear error reporting, so that I can manually investigate.

**Why this priority**: Ensures data integrity and prevents partial loads that could corrupt downstream analytics.

**Independent Test**: Can be tested by simulating various failure scenarios (network errors, invalid data) and verifying the process stops with appropriate error messages.

**Acceptance Scenarios**:

1. **Given** a network error during temporary table creation, **When** the loader executes, **Then** the ETL process stops with a descriptive error.
2. **Given** invalid data format that violates DolphinDB schema constraints, **When** the loader executes, **Then** the process stops with a validation error.

---


### Edge Cases

- What happens when DolphinDB is unavailable during the loading process?
- How does the system handle duplicate data entries in the transformed dataset?
- What if temporary table creation fails due to naming conflicts with existing tables?
- Records with null values in sort fields are skipped during sorting (not loaded).
- Data exceeding memory uses disk-based sorting (external sort).
- Partial failures keep loaded data, temporary tables left for manual cleanup.

## Requirements *(mandatory)*

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right functional requirements.
-->

### Functional Requirements

- **FR-001**: System MUST load transformed data to DolphinDB using its Java API.
- **FR-002**: System MUST create temporary tables in DolphinDB before loading data.
- **FR-003**: System MUST sort TargetDataModel instances by specified fields before loading.
- **FR-004**: System MUST load sorted data into appropriate target tables sequentially based on data type.
- **FR-005**: System MUST integrate with LoadSubprocess to receive transformed data and execute loading.
- **FR-006**: System MUST integrate with CleanSubprocess to remove temporary tables after data validation.
- **FR-007**: System MUST stop the ETL process with descriptive error messages when any loading exception occurs.
- **FR-008**: System MUST provide a common Loader API that can be extended to support other target systems (e.g., MySQL).
- **FR-009**: Target table names MUST be configurable and remain unchanged during the entire ETL process.
- **FR-010**: System MUST record all business bug fixes with version numbers for future reference, per constitution PR‑7.
- **FR-011**: System MUST explicitly initialize all primitive numeric fields (int, double, long, etc.) in TargetDataModel classes, avoiding default zero values, per constitution PR‑11.

### Key Entities *(include if feature involves data)*

- **TargetDataModel**: Represents transformed data ready for loading, with fields mapped to DolphinDB schema.
- **Temporary Table**: Intermediate table in DolphinDB used during loading process, created with unique names that are generated once and remain immutable throughout loading.
- **Target Table**: Final destination table in DolphinDB where data is persisted for downstream analytics.
- **Loader Configuration**: Settings defining sorting fields, table mappings (configurable target table names that remain unchanged during the entire ETL process), DolphinDB connection parameters (including authentication credentials), and temporary table naming conventions.

### Concrete TargetDataModel Classes

Three concrete classes extend the abstract `TargetDataModel` base class, each corresponding to a specific data type and target table.

**1. XbondQuoteDataModel**
- **Target Table**: `xbond_quote_stream_temp` (configurable via `target.table.mappings`)
- **Field Order**: Exactly 83 fields as defined in Plan.md Section V
- **Key Fields**: `business_date` (DATE), `exch_product_id` (SYMBOL), `product_type` (SYMBOL), `exchange` (SYMBOL), `source` (SYMBOL), `settle_speed` (INT), `level` (SYMBOL), `status` (SYMBOL), `pre_close_price` (DOUBLE), `pre_settle_price` (DOUBLE), `pre_interest` (DOUBLE), `open_price` (DOUBLE), `high_price` (DOUBLE), `low_price` (DOUBLE), `close_price` (DOUBLE), `settle_price` (DOUBLE), `upper_limit` (DOUBLE), `lower_limit` (DOUBLE), `total_volume` (DOUBLE), `total_turnover` (DOUBLE), `open_interest` (DOUBLE), plus 60 additional fields for bid/offer levels 0‑5, ending with `event_time` (TIMESTAMP), `receive_time` (TIMESTAMP)

**2. XbondTradeDataModel**
- **Target Table**: `xbond_trade_stream_temp` (configurable via `target.table.mappings`)
- **Field Order**: Exactly 15 fields as defined in Plan.md Section VI
- **Key Fields**: `business_date` (DATE), `exch_product_id` (SYMBOL), `product_type` (SYMBOL), `exchange` (SYMBOL), `source` (SYMBOL), `settle_speed` (INT), `last_trade_price` (DOUBLE), `last_trade_yield` (DOUBLE), `last_trade_yield_type` (SYMBOL), `last_trade_volume` (DOUBLE), `last_trade_turnover` (DOUBLE), `last_trade_interest` (DOUBLE), `last_trade_side` (SYMBOL), `event_time` (TIMESTAMP), `receive_time` (TIMESTAMP)

**3. BondFutureQuoteDataModel**
- **Target Table**: `fut_market_price_stream_temp` (configurable via `target.table.mappings`)
- **Field Order**: Exactly 96 fields as defined in Plan.md Section VII
- **Key Fields**: `business_date` (DATE), `exch_product_id` (SYMBOL), `product_type` (SYMBOL), `exchange` (SYMBOL), `source` (SYMBOL), `settle_speed` (INT), plus 90 additional fields for last trade details, level/status, price/yield/volume fields, and six timestamp fields (`event_time_trade`, `receive_time_trade`, `create_time_trade`, `event_time_quote`, `receive_time_quote`, `create_time_quote`, `tick_type`, `receive_time`)

**Implementation Note**: The single concrete loader (`DolphinDBLoader`) will accept a list containing mixed instances of these three classes, group them by `dataType`, and load each group into the corresponding target table as specified in the configuration mapping.

## Assumptions

- Sorting fields are specified in configuration and are consistent across all TargetDataModel types.
- DolphinDB Java API is available and compatible with Java 8.
- Network connectivity between ETL tool and DolphinDB is reliable.
- Temporary table names are generated uniquely once and remain immutable throughout loading.
- Data validation occurs before cleanup, ensuring only valid data persists.

## Success Criteria *(mandatory)*

<!--
  ACTION REQUIRED: Define measurable success criteria.
  These must be technology-agnostic and measurable.
-->

### Measurable Outcomes

- **SC-001**: Data loading completes within 30 minutes for typical daily volume (e.g., 1 million records).
- **SC-002**: Temporary tables are successfully created before loading and removed after validation, with zero orphaned tables remaining.
- **SC-003**: Loading failures result in immediate process termination with error messages that enable operator diagnosis within 5 minutes.
- **SC-004**: Common Loader API allows adding support for a new target system (e.g., MySQL) with less than 200 lines of new code.
