# Feature Specification: COS Xbond Trade Extraction

**Feature Branch**: `003-xbond-trade-extract`  
**Created**: 2026-01-10  
**Status**: Draft  
**Input**: User description: "Extract Xbond Trade data from COS for ETL"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Extract Xbond Trade data from COS (Priority: P1)

As a data engineer running the ETL process for a given date range, I need the system to retrieve and parse all Xbond Trade files from COS that match the run context, so that downstream ETL steps can operate on a unified set of trade records.

**Why this priority**: Without reliable extraction of trade data, the ETL pipeline cannot produce any usable trade outputs for the day, making this the foundational capability.

**Independent Test**: Can be fully tested by running an ETL job for a single day where COS contains known matching and non-matching trade files, and verifying that only matching files contribute records to the extracted output.

**Acceptance Scenarios**:

1. **Given** a run context that includes a target date, **When** an ETL job starts, **Then** the system selects only COS files that match the context filter and extracts trade records from them.
2. **Given** no COS files match the context filter, **When** an ETL job starts, **Then** extraction completes successfully with an empty output set and logs that no files were selected.
3. **Given** at least one selected file is unreadable or cannot be downloaded, **When** extraction runs, **Then** the ETL job reports extraction failure for that day and does not mark extraction as complete.

---

### User Story 2 - Reuse existing Extractor API pattern (Priority: P1)

As a data engineer, I need the Xbond Trade extractor to follow the same architectural pattern as the Xbond Quote extractor (from Phase II), so that the system maintains consistency and leverages proven extraction patterns.

**Why this priority**: Consistency with existing extractors reduces implementation risk, simplifies maintenance, and ensures the multi-source extraction framework works seamlessly.

**Independent Test**: Can be tested by verifying that the Xbond Trade extractor implements the same Extractor API contract as the Xbond Quote extractor and can be registered alongside it in the ExtractSubprocess.

**Acceptance Scenarios**:

1. **Given** both Xbond Quote and Xbond Trade extractors are configured for a run, **When** the ETL job executes extraction, **Then** each extractor receives the run context and returns records in their respective SourceDataModel extensions.
2. **Given** the Xbond Trade extractor is the only configured extractor, **When** extraction runs, **Then** it operates independently and produces valid output without requiring other extractors.

---

### User Story 3 - Extend SourceDataModel for Trade data (Priority: P1)

As a data engineer, I need Xbond Trade data to be represented as its own extension of SourceDataModel, so that trade-specific attributes are captured while maintaining compatibility with the ETL framework.

**Why this priority**: Trade data has different attributes than quote data, requiring a distinct data model while preserving the common extraction interface.

**Independent Test**: Can be tested by extracting trade records and verifying that the output contains trade-specific fields (e.g., trade volume, trade price, counterparty) distinct from quote fields.

**Acceptance Scenarios**:

1. **Given** a COS file containing Xbond Trade data, **When** the extractor processes it, **Then** each trade record is converted into a SourceDataModel extension with trade-specific attributes.
2. **Given** extracted trade records, **When** downstream ETL steps receive them, **Then** they can access both common SourceDataModel fields and trade-specific extension fields.

---

### Edge Cases

- No matching trade files in COS for the given context filter.
- Large number of matching trade files causing long runtimes.
- Duplicate files and/or duplicate trade records across multiple sources (duplicates are retained in extraction output).
- Partial download failures or transient connectivity issues.
- Corrupted file content or unexpected file encoding.
- A single file contains trade records for multiple dates (records should be filtered to the target date from context).
- Extremely large individual files that exceed memory limits (fail the day extraction gracefully; must not crash the process).
- Mixed quote and trade data in the same COS bucket (extractor must select only trade files).
- Trade files with incomplete or malformed records (extractor should log warnings but continue processing valid records).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The Xbond Trade extractor MUST implement the same Extractor API contract as the Xbond Quote extractor from Phase II.
- **FR-002**: The Xbond Trade extractor MUST apply filtering based on information provided by the ETL run context, including evaluating configured selection criteria.
- **FR-003**: The system MUST support extracting Xbond Trade data from COS by selecting files using a configured match rule/template evaluated with context values (e.g., target date).
- **FR-004**: The system MUST retrieve the selected COS trade files into a local working area for processing during the run.
- **FR-004a**: If any selected trade file cannot be downloaded, the system MUST fail the day's extraction with a structured error message including file name, download failure reason, and timestamp.
- **FR-005**: The system MUST parse each selected trade file into in-memory raw records suitable for conversion and filter records to the target date based on context when files contain mixed dates.
- **FR-005a**: If a selected trade file exceeds 100MB (configurable threshold), the system MUST fail the day's extraction with a structured error message including file name, size, and safe processing limit (and must not crash).
- **FR-006**: The system MUST define a SourceDataModel extension specific to Xbond Trade data that captures trade-specific attributes.
- **FR-007**: The system MUST convert the combined raw trade records into a set of trade-specific SourceDataModel records as the extraction output.
- **FR-008**: The system MUST return the converted SourceDataModel set as the extraction output for downstream ETL steps.
- **FR-009**: The Xbond Trade extractor MUST be executable within the existing ETL ExtractSubprocess alongside other extractors.
- **FR-010**: The ExtractSubprocess MUST support concurrent execution of Xbond Trade and Xbond Quote extractors when both are configured.
- **FR-011**: The ExtractSubprocess MUST consolidate trade records with records from other extractors into a single logical output without de-duplicating records.
- **FR-012**: The system MUST provide structured JSON operational logs that include: timestamp, log level, category, selected file count, extracted trade record count, error details (if any), and processing duration.

### Key Entities *(include if feature involves data)*

- **Xbond Trade Extractor**: A component responsible for retrieving and converting Xbond Trade source data from COS into trade-specific SourceDataModel records based on run context.
- **COS Trade File**: A file stored in COS that contains Xbond Trade data.
- **Trade File Selection Rule**: A configurable rule/template that determines whether a COS file contains trade data and is in-scope for the run based on context values.
- **Context Filter Criteria**: The set of filter inputs derived from the ETL run context (e.g., target date) used to select relevant trade files/records.
- **Raw Trade Record**: A parsed representation of a single trade file's contents prior to conversion into the standard output model.
- **Trade SourceDataModel Record**: The standardized trade record format (extending SourceDataModel) produced by the Xbond Trade extractor and consumed by downstream ETL steps.
- **Trade-Specific Attributes**: Data fields unique to trade records such as trade volume, trade price, trade timestamp, counterparty information, and trade type.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: For a given run date, the system consistently extracts all available matching COS trade files and produces a consolidated output set without manual intervention.
- **SC-002**: When no matching trade files exist for a run date, the ETL job completes extraction successfully and produces an empty trade output set.
- **SC-003**: When the trade extractor encounters an unrecoverable error (e.g., file cannot be retrieved or parsed), the day's extraction is marked failed and downstream steps are not executed for that day.
- **SC-004**: For a typical daily workload, trade extraction completes within 30 minutes (configurable target). Performance metrics including extraction duration, file count, record count, and success/failure status MUST be recorded in structured operational logs.
- **SC-005**: The Xbond Trade extractor can run concurrently with the Xbond Quote extractor without conflicts, and both produce their respective outputs within the same ETL run.

## Assumptions *(optional)*

- The ETL run context provides at least a target date for filtering.
- The COS bucket/prefix (or equivalent location identifiers) and credentials are provided via existing configuration mechanisms.
- The business definition of "Xbond Trade data" and the mapping into the trade-specific SourceDataModel extension are available to implementers; this specification focuses on extraction behavior and orchestration outcomes.
- The Xbond Quote extractor from Phase II is already implemented and serves as the reference pattern.
- Trade files in COS follow a consistent naming convention or metadata pattern that allows them to be distinguished from quote files.
- The existing ExtractSubprocess framework supports multiple extractors and can accommodate the new trade extractor without modification to the orchestration logic.
