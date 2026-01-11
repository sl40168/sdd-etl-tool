# Feature Specification: Bond Future Quote Database Extraction

**Feature Branch**: `004-bond-future-quote-extract`  
**Created**: 2026-01-11  
**Status**: Draft  
**Input**: User description: "Extract Bond Future Quote data from MySQL Database for ETL (Phase V)"

## Clarifications

### Session 2026-01-11

- Q: How should the system handle transient database connection failures? → A: Retry with exponential backoff (e.g., 3 attempts: 1s, 2s, 4s delays) then fail
- Q: How should the system handle duplicate quote records for the same security on the same business date? → A: Keep all duplicates in the output (no deduplication)
- Q: What should be the maximum query execution timeout for database queries? → A: 5 minutes (generous for ETL workloads)
- Q: How should database connections be managed across multiple extraction runs? → A: Use connection pooling with configurable pool size (e.g., min=1, max=5)
- Q: How should the system handle records with invalid or missing critical fields? → A: Skip invalid records, log warnings (MUST log), continue with valid records


## User Scenarios & Testing *(mandatory)*

### User Story 1 - Extract Bond Future Quote data from MySQL (Priority: P1)

As a data engineer running the ETL process for a given business date, I need the system to retrieve all Bond Future Quote records from the MySQL database that match the target date, so that downstream ETL steps can operate on a unified set of quote records.

**Why this priority**: Without reliable extraction of bond future quote data from the database, the ETL pipeline cannot produce any usable quote outputs for the day, making this the foundational capability.

**Independent Test**: Can be fully tested by running an ETL job for a single day where the MySQL database contains known records for that date and other dates, and verifying that only matching records are extracted.

**Acceptance Scenarios**:

1. **Given** a run context that includes a target business date, **When** an ETL job starts, **Then** the system queries the MySQL database using the configured SQL template with the business date filter and extracts all matching quote records.
2. **Given** no database records match the business date filter, **When** an ETL job starts, **Then** extraction completes successfully with an empty output set and logs that no records were found.
3. **Given** the database connection fails or times out, **When** extraction runs, **Then** the ETL job reports extraction failure for that day and does not mark extraction as complete.

---

### User Story 2 - Reuse existing Extractor API pattern (Priority: P1)

As a data engineer, I need the Bond Future Quote extractor to follow the same architectural pattern as existing extractors (COS-based Xbond Quote and Trade extractors), so that the system maintains consistency and leverages proven extraction patterns.

**Why this priority**: Consistency with existing extractors reduces implementation risk, simplifies maintenance, and ensures the multi-source extraction framework works seamlessly.

**Independent Test**: Can be tested by verifying that the Bond Future Quote extractor implements the same Extractor API contract and can be registered alongside other extractors in the ExtractSubprocess.

**Acceptance Scenarios**:

1. **Given** multiple extractors (Xbond Quote, Xbond Trade, Bond Future Quote) are configured for a run, **When** the ETL job executes extraction, **Then** each extractor receives the run context and returns records in their respective SourceDataModel extensions.
2. **Given** the Bond Future Quote extractor is the only configured extractor, **When** extraction runs, **Then** it operates independently and produces valid output without requiring other extractors.

---

### User Story 3 - Provide abstract Database Extractor base class (Priority: P1)

As a developer, I need an abstract Database Extractor base class that handles common database operations (SQL template loading, connection management, streaming data extraction), so that concrete database extractors can focus on their specific business logic.

**Why this priority**: A reusable database extractor foundation reduces code duplication, standardizes database interaction patterns, and makes it easier to add future database-based extractors.

**Independent Test**: Can be tested by implementing the Bond Future Quote extractor as a concrete extension of the abstract Database Extractor and verifying it correctly inherits template SQL loading, connection handling, and streaming capabilities.

**Acceptance Scenarios**:

1. **Given** a concrete database extractor extends the abstract Database Extractor, **When** it runs, **Then** it automatically reads the SQL template from the INI configuration without custom parsing logic.
2. **Given** a database query returns a large result set, **When** the abstract Database Extractor processes it, **Then** it builds SourceDataModel records in a streaming fashion to minimize memory usage.
3. **Given** a database operation fails, **When** the abstract Database Extractor encounters the error, **Then** it breaks the extraction process and logs a structured error message.

---

### User Story 4 - Execute Bond Future Quote extractor concurrently with other extractors (Priority: P1)

As a data engineer, I need the Bond Future Quote extractor to run concurrently with existing COS extractors (Xbond Quote and Xbond Trade), so that the overall ETL extraction phase completes faster.

**Why this priority**: Concurrent execution reduces total extraction time, improving ETL job performance and allowing faster data delivery.

**Independent Test**: Can be tested by configuring all three extractors for a single run and verifying that they execute in parallel and all complete successfully with their respective outputs.

**Acceptance Scenarios**:

1. **Given** all extractors (Xbond Quote, Xbond Trade, Bond Future Quote) are configured, **When** extraction runs, **Then** they execute concurrently and each produces its output independently.
2. **Given** one extractor fails during concurrent execution, **When** the failure occurs, **Then** the overall extraction is marked as failed and all extractors are stopped or allowed to complete before reporting failure.

---

### Edge Cases

- No matching quote records in MySQL for the given business date.
- Database connection timeout or network interruption during query execution.
- Extremely large result sets that could cause memory issues (must stream data).
- Database schema changes that make the SQL template invalid.
- Malformed or unexpected data types in database columns (invalid records are skipped with mandatory warning logging).
- Records with missing critical fields such as null security ID or invalid trading date (skipped with warnings).
- Duplicate quote records for the same security and business date (all duplicates are retained in extraction output).
- Database returns partial results due to query timeout.
- Multiple concurrent ETL jobs attempting to query the same database.
- Database maintenance or downtime during extraction window.
- SQL template contains syntax errors or invalid placeholders.
- Business date format mismatch between context and database storage format.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST provide an abstract Database Extractor base class that extends the existing Extractor API contract.
- **FR-002**: The abstract Database Extractor MUST read SQL template strings from INI configuration files.
- **FR-003**: The abstract Database Extractor MUST provide a mechanism for concrete implementations to supply filter condition values to fill SQL template placeholders.
- **FR-004**: The abstract Database Extractor MUST build SourceDataModel records in a streaming fashion to minimize memory usage, processing database rows incrementally.
- **FR-005**: The abstract Database Extractor MUST return the complete set of SourceDataModel records only after all matching data has been extracted from the database.
- **FR-006**: The abstract Database Extractor MUST log a warning when no records match the filter criteria, but still complete successfully with an empty result set.
- **FR-007**: The abstract Database Extractor MUST break the extraction process and report a structured error when any database operation fails (connection, query execution, data parsing). For connection failures, the system MUST retry up to 3 times with exponential backoff delays (1s, 2s, 4s) before reporting final failure.
- **FR-008**: The system MUST provide a concrete Bond Future Quote Extractor that extends the abstract Database Extractor.
- **FR-009**: The Bond Future Quote Extractor MUST use the SQL template: `select * from bond.fut_tick where trading_date = {BUSINESS_DATE}`.
- **FR-010**: The Bond Future Quote Extractor MUST derive the `BUSINESS_DATE` filter value from the ETL run context, formatted as an integer in `YYYYMMDD` format.
- **FR-011**: The Bond Future Quote Extractor MUST convert each database row into a SourceDataModel extension specific to Bond Future Quote data.
- **FR-012**: The Bond Future Quote SourceDataModel extension MUST capture all relevant quote attributes from the `bond.fut_tick` table schema.
- **FR-013**: The Bond Future Quote Extractor MUST be executable within the existing ETL ExtractSubprocess alongside other extractors (Xbond Quote, Xbond Trade).
- **FR-014**: The ExtractSubprocess MUST support concurrent execution of all configured extractors (COS-based and database-based).
- **FR-015**: The system MUST provide structured JSON operational logs that include: timestamp, log level, category, database query details, extracted record count, error details (if any), and processing duration.
- **FR-016**: The abstract Database Extractor MUST enforce a configurable query execution timeout with a default of 5 minutes, failing the extraction if the timeout is exceeded.
- **FR-017**: The abstract Database Extractor MUST use connection pooling with configurable pool size (default: min=1, max=5 connections) to manage database connections efficiently across extraction runs.
- **FR-018**: The abstract Database Extractor MUST skip records with invalid or missing critical fields (e.g., null security ID, invalid date format), MUST log a warning for each skipped record including the reason and row identifier, and MUST continue processing valid records.

### Key Entities *(include if feature involves data)*

- **Abstract Database Extractor**: A reusable base class that handles common database extraction operations including SQL template loading, connection management, query execution, and streaming data conversion.
- **Bond Future Quote Extractor**: A concrete implementation of the Database Extractor that retrieves bond future quote data from the MySQL `bond.fut_tick` table.
- **SQL Template**: A parameterized SQL query string stored in INI configuration with placeholders (e.g., `{BUSINESS_DATE}`) that are filled at runtime.
- **Business Date Filter**: The target date from the ETL run context, formatted as `YYYYMMDD` integer, used to filter database records.
- **Bond Future Quote SourceDataModel**: The standardized quote record format (extending SourceDataModel) produced by the Bond Future Quote extractor, containing fields such as exchange, security ID, price, volume, bid/ask prices, timestamps, etc.
- **Database Connection**: The MySQL database connection used to execute queries, managed by the abstract Database Extractor.
- **fut_tick Table**: The MySQL table (`bond.fut_tick`) containing bond future quote data with columns including: id, exchg, code, price, open, high, low, settle_price, upper_limit, lower_limit, total_volume, total_turnover, open_interest, trading_date, action_date, action_time, pre_close, pre_settle, pre_interest, bid_prices, ask_prices, bid_qty, ask_qty, receive_time.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: For a given business date, the system consistently extracts all available matching bond future quote records from MySQL and produces a consolidated output set without manual intervention.
- **SC-002**: When no matching quote records exist for a business date, the ETL job completes extraction successfully and produces an empty quote output set.
- **SC-003**: When the database extractor encounters an unrecoverable error (e.g., connection failure, query timeout, data parsing error), the day's extraction is marked failed and downstream steps are not executed for that day.
- **SC-004**: For a typical daily workload, bond future quote extraction completes within 10 minutes. Performance metrics including extraction duration, record count, and success/failure status MUST be recorded in structured operational logs.
- **SC-005**: The Bond Future Quote extractor runs concurrently with COS-based extractors (Xbond Quote, Xbond Trade) without conflicts, and all produce their respective outputs within the same ETL run.
- **SC-006**: The abstract Database Extractor processes large result sets (10,000+ records) without exceeding reasonable memory limits through streaming data conversion.

## Assumptions *(optional)*

- The ETL run context provides a target business date for filtering.
- MySQL database connection details (host, port, database name, credentials) are provided via existing configuration mechanisms.
- The `bond.fut_tick` table schema is stable and matches the documented structure.
- The business date is stored in the `trading_date` column as an integer in `YYYYMMDD` format.
- The mapping from `bond.fut_tick` columns to Bond Future Quote SourceDataModel fields is documented in the technical plan.
- The existing ExtractSubprocess framework supports multiple extractors and can accommodate the new database extractor without modification to the orchestration logic.
- Database query performance is acceptable for typical daily record volumes (estimated at thousands of records per day).
- The MySQL database is accessible from the ETL execution environment with appropriate network and firewall configurations.
