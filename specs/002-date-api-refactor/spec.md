# Feature Specification: Refactor Date APIs and Add Default Config

**Feature Branch**: `002-date-api-refactor`  
**Created**: 2026-01-10  
**Status**: Draft  
**Input**: User description: "Phase III: Tech Requirement to Refactor - Make APIs more reasonable (use LocalDate instead of String for dates) and create default INI config file"

## Clarifications

### Session 2026-01-10

- Q: Should we refactor only the three specified APIs or all date fields in the codebase? → A: Refactor only the three specified APIs to minimize risk and scope.

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

### User Story 1 - Use LocalDate for current date in ETLContext (Priority: P1)

Developers using the ETLContext API want to retrieve the current date as a LocalDate object instead of a String representation to eliminate date-parsing errors for valid YYYYMMDD strings and enable compile-time-checked date operations.

**Why this priority**: This change improves API consistency and type safety, eliminating date-parsing errors and providing compile-time type checking for date operations.

**Independent Test**: Can be tested by calling `ETLContext.getCurrentDate()` and verifying the return type is `LocalDate` and contains the correct date.

**Acceptance Scenarios**:

1. **Given** an ETLContext instance, **When** `getCurrentDate()` is called, **Then** it returns a `LocalDate` object representing today's date.
2. **Given** an ETLContext instance with a specific date set, **When** `getCurrentDate()` is called, **Then** it returns the same date as a `LocalDate` object.

---

### User Story 2 - Use LocalDate for start and end dates in WorkflowResult (Priority: P2)

Developers using the WorkflowResult model want startDate and endDate fields to be LocalDate types instead of String for consistent date handling across the codebase.

**Why this priority**: Ensures date fields have proper type semantics, enabling date arithmetic and comparison operations to complete in <1 ms without string-parsing overhead.

**Independent Test**: Can be tested by creating a WorkflowResult instance and verifying that startDate and endDate fields are of type `LocalDate`.

**Acceptance Scenarios**:

1. **Given** a WorkflowResult instance, **When** startDate and endDate are accessed, **Then** they return `LocalDate` objects.
2. **Given** a WorkflowResult instance with specific date ranges, **When** dates are compared, **Then** comparison operations work correctly using LocalDate methods.

---

### User Story 3 - Generate date ranges as List<LocalDate> (Priority: P2)

Developers using the DateRangeGenerator utility want the `generate` method to return `List<LocalDate>` instead of `List<String>` for consistency with other date APIs and easier integration with date-based operations.

**Why this priority**: Aligns date generation with the rest of the system's date handling approach, eliminating String-to-LocalDate conversion code in client implementations.

**Independent Test**: Can be tested by calling `DateRangeGenerator.generate()` and verifying the returned list contains `LocalDate` objects for the specified date range.

**Acceptance Scenarios**:

1. **Given** a start date and end date, **When** `DateRangeGenerator.generate()` is called, **Then** it returns a `List<LocalDate>` containing all dates in the range inclusive.
2. **Given** invalid date inputs, **When** `DateRangeGenerator.generate()` is called, **Then** it throws appropriate exceptions (e.g., IllegalArgumentException).

---

### User Story 4 - Provide default INI configuration file (Priority: P3)

Developers and users need a default INI configuration file that demonstrates how to configure existing components, serving as a demo and reference for real usage.

**Why this priority**: Improves developer experience by providing a working example configuration, reducing setup time and clarifying configuration options.

**Independent Test**: Can be tested by loading the default INI file with the configuration loader and verifying that all expected sections and properties are present and valid.

**Acceptance Scenarios**:

1. **Given** the default INI configuration file, **When** it is parsed by the configuration loader, **Then** all sections and properties are successfully loaded without errors.
2. **Given** the default INI configuration file, **When** used to run the ETL workflow, **Then** the workflow executes with the default settings.

### Edge Cases

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right edge cases.
-->

- What happens when date parsing fails (e.g., malformed String input)? The system should throw a clear exception (e.g., DateTimeParseException) with descriptive message.
- How does system handle invalid date ranges (e.g., start date after end date)? DateRangeGenerator should throw IllegalArgumentException with appropriate message.
- What if the default INI configuration file is missing required sections? The configuration loader should either provide default values or throw a clear configuration error.

## Requirements *(mandatory)*

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right functional requirements.
-->

### Functional Requirements

- **FR-001**: ETLContext.getCurrentDate() MUST return LocalDate instead of String.
- **FR-002**: WorkflowResult.startDate and WorkflowResult.endDate MUST be LocalDate types instead of String.
- **FR-003**: DateRangeGenerator.generate() MUST return List<LocalDate> instead of List<String>.
- **FR-004**: All dependent code using the above APIs MUST be updated to handle LocalDate types correctly.
- **FR-005**: A default INI configuration file MUST be created covering all existing components as a demo.
- **FR-006**: The default INI configuration file MUST be placed in a standard location (e.g., src/main/resources/config.ini) and loadable by the configuration loader.

### Key Entities *(include if feature involves data)*

- **DateField**: Represents a date value in the system, with attributes like year, month, day. Used across APIs for consistent date handling.
- **ConfigurationFile**: Represents an INI configuration file containing sections and properties for system components. Provides runtime configuration.

## Assumptions

- The system uses Java 8 or later (non-negotiable per project constitution).
- The existing codebase has unit tests that will validate the refactoring.
- The configuration loader can parse INI files (Apache Commons Configuration).
- Only the three explicitly listed APIs (ETLContext.getCurrentDate, WorkflowResult.startDate/endDate, DateRangeGenerator.generate) will be refactored; other date fields remain as String.

## Success Criteria *(mandatory)*

<!--
  ACTION REQUIRED: Define measurable success criteria.
  These must be technology-agnostic and measurable.
-->

### Measurable Outcomes

- **SC-001**: All date-related API calls (ETLContext.getCurrentDate, WorkflowResult date fields, DateRangeGenerator.generate) return strongly-typed date objects instead of string representations, as verified by unit tests.
- **SC-002**: Default INI configuration file loads successfully with all expected sections and properties, verified by integration test.
- **SC-003**: Existing test suite passes with 100% success rate after refactoring, ensuring no regression.
- **SC-004**: Developers can use the new APIs without needing string-to-date conversions, reducing boilerplate code.
