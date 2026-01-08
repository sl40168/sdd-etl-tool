# Research Report: ETL Core Workflow

**Feature Branch**: `001-etl-core-workflow`
**Date**: 2026-01-08
**Status**: Complete

## Overview

This research report documents technology choices and architectural decisions for the ETL Core Workflow feature. All research items from the Technical Context have been resolved.

## Technology Decisions

### 1. CLI Library: Apache Commons CLI

**Decision**: Apache Commons CLI will be used for command-line argument parsing.

**Rationale**:
- Mature, stable library with long-standing presence in Java ecosystem
- Excellent documentation and community support
- Java 8 compatible (no conflicts with constitution requirement)
- Supports complex option types (required, optional, arguments)
- Built-in help text generation capability (FR-003)
- Lightweight with no unnecessary dependencies

**Alternatives Considered**:
- **Picocli**: More modern with annotation-based syntax, but may be overkill for simple 3-parameter use case
- **JCommander**: Good API but less mature than Commons CLI
- **Custom implementation**: Rejected per constitution principle #8 (use third-party libraries)

---

### 2. INI Configuration Parser: Apache Commons Configuration

**Decision**: Apache Commons Configuration will be used for parsing INI configuration files.

**Rationale**:
- Specifically designed for INI file format (matches constitution requirement)
- Provides type-safe access to configuration values
- Handles nested sections and key-value pairs elegantly
- Excellent error handling for missing or malformed configuration
- Java 8 compatible with stable API
- Well-documented with extensive examples

**Alternatives Considered**:
- **ini4j**: Popular but less actively maintained
- **Custom implementation**: Rejected per constitution principle #8; would require extensive parsing logic
- **Java Properties API**: Doesn't support INI format with sections

---

### 3. Logging Framework: SLF4J + Logback

**Decision**: SLF4J as facade with Logback as implementation for logging.

**Rationale**:
- SLF4J provides abstraction layer for future flexibility
- Logback is the native implementation of SLF4J with excellent performance
- Supports multiple appenders (console, file) required by FR-016 and FR-017
- Java 8 compatible with robust async logging capabilities
- Easy configuration via XML (logback.xml)
- Widely adopted in enterprise Java applications

**Alternatives Considered**:
- **java.util.logging**: Too basic, lacks flexibility
- **Log4j2**: Good alternative but Logback is more performant and simpler to configure
- **System.out.println**: Not maintainable; lacks features like log levels, formatting

---

### 4. Testing Framework: JUnit 4 + Mockito

**Decision**: JUnit 4 with Mockito for unit testing and mocking.

**Rationale**:
- JUnit 4 is the standard for Java 8 testing (JUnit 5 requires Java 8+ but JUnit 4 is more stable)
- Mockito is the de-facto standard for mocking in Java
- Both have excellent integration with Maven
- Simple, readable test syntax
- Strong community support and documentation
- Meets >60% coverage requirement through comprehensive test assertions

**Alternatives Considered**:
- **TestNG**: Powerful but JUnit 4 is more widely adopted
- **PowerMock**: Only needed for static/private mocking; not required for this architecture
- **Spock**: Groovy-based; would introduce another language to the project

---

### 5. Context Implementation Pattern: ThreadLocal + HashMap

**Decision**: Context will use ThreadLocal-based storage with HashMap for key-value pairs.

**Rationale**:
- ThreadLocal ensures thread safety for single-process execution
- HashMap provides O(1) access for context data retrieval
- Simple implementation meeting FR-014 through FR-031 requirements
- Type-safe access can be enforced via wrapper methods
- No additional dependencies required
- Meets context-based data transfer requirement (FR-027) without direct method calls

**Key Design Points**:
- Context is created per day's process (FR-014)
- Subprocesses write to and read from context (FR-028 through FR-031)
- Context manager handles lifecycle (creation, cleanup)
- Immutable context snapshots for debugging/troubleshooting

**Alternatives Considered**:
- **DI Framework (Spring/Guice)**: Overkill for single-process CLI tool; violates simplicity principle
- **Singleton pattern**: Not suitable for multi-day concurrent execution (even though this phase doesn't support it)
- **Database-backed context**: Unnecessary complexity; file-based logging is sufficient

---

### 6. Concurrent Execution Detection: File Lock Mechanism

**Decision**: File lock mechanism using `java.nio.channels.FileLock`.

**Rationale**:
- Simple, OS-level locking mechanism
- Works across different platforms (Windows, Linux, macOS)
- Automatically releases lock when process terminates
- No external dependencies
- Meets FR-024 requirement for detecting concurrent executions
- Lightweight with minimal performance impact

**Implementation Details**:
- Lock file created in application directory on startup
- Attempt to acquire exclusive lock on startup; fail if already held
- Lock released automatically on JVM exit (finally block)
- Lock file remains as indicator if process crashes (can be manually removed)

**Alternatives Considered**:
- **Database lock**: Would require database setup; overkill for CLI tool
- **Network socket bind**: Complex to implement and port-dependent
- **PID file checking**: Not cross-platform reliable

---

### 7. Error Handling Strategy: Early Termination with Context Preservation

**Decision**: Fail-fast approach with detailed error messages and context preservation.

**Rationale**:
- Matches user requirement: no automatic retry (clarification 2026-01-08)
- Users are Production Support personnel; can investigate and restart manually
- Context preservation enables post-mortem analysis
- Clear error messages support troubleshooting (assumption in spec)
- Prevents data corruption from partial completion

**Implementation Details**:
- Validation failures stop before ETL execution starts (FR-002)
- Subprocess failures stop the day's process immediately (FR-006)
- Day failures stop entire multi-day process (FR-004 scenario 3)
- Error messages include: subprocess name, date, error description, suggested action
- Context state logged before termination for debugging

**Alternatives Considered**:
- **Retry mechanism**: Explicitly rejected by user clarification
- **Continue on error**: Risks data corruption and inconsistent state
- **Checkpoint/resume**: Out of scope per clarification; manual restart required

---

### 8. Date Processing: Java 8 java.time API

**Decision**: Use `java.time.LocalDate` for date representation and validation.

**Rationale**:
- Built into Java 8 (no additional dependencies)
- Immutable, thread-safe date objects
- Built-in validation for valid dates (leap years, month boundaries)
- Easy format parsing with `DateTimeFormatter` (YYYYMMDD format)
- Supports date arithmetic for iterating through date ranges

**Implementation Details**:
- Parse dates using `DateTimeFormatter.ofPattern("yyyyMMdd")`
- Validate format and logical validity (from ≤ to)
- Iterate using `date.plusDays(1)`
- Format back to YYYYMMDD for logging and context storage

**Alternatives Considered**:
- **java.util.Date/java.util.Calendar**: Deprecated, mutable, error-prone
- **Joda-Time**: Was standard before Java 8; now obsolete with java.time
- **String manipulation**: Error-prone, doesn't validate date validity

---

## Architectural Patterns

### 1. Orchestrator Pattern for Daily ETL Workflow

**Pattern**: Orchestrator Pattern with sequential subprocess execution.

**Rationale**:
- Clearly separates workflow logic from subprocess implementation
- Enforces strict sequence requirement (FR-005, FR-006)
- Easy to add monitoring and logging at orchestration level
- Testable in isolation (can mock subprocesses)
- Supports future extensibility (adding more subprocesses)

**Components**:
- `DailyETLWorkflow`: Orchestrates a single day's execution
- `SubprocessExecutor`: Executes subprocesses in sequence with dependency checks
- `WorkflowEngine`: Manages multi-day date range iteration

---

### 2. Context Pattern for Data Transfer

**Pattern**: Context Object Pattern with thread-safe storage.

**Rationale**:
- Meets FR-027 requirement for context-based data transfer
- Decouples subprocesses (no direct method calls)
- Centralized state management for monitoring and debugging
- Type-safe access through wrapper methods
- Supports both read and write operations per subprocess requirements

**Data Flow**:
1. Extract writes extracted data count → Context
2. Transform reads extracted data, writes transformed data count → Context
3. Load reads transformed data, writes loaded data count → Context
4. Validate reads loaded data, writes validation results → Context
5. Clean reads validation results, performs cleanup → Context

---

## Dependencies Summary

**Runtime Dependencies**:
- `org.apache.commons:commons-cli:1.4` - CLI argument parsing
- `org.apache.commons:commons-configuration2:2.8.0` - INI configuration parsing
- `commons-beanutils:commons-beanutils:1.9.4` - Required by Commons Configuration
- `org.slf4j:slf4j-api:1.7.36` - Logging facade
- `ch.qos.logback:logback-classic:1.2.11` - Logging implementation
- `ch.qos.logback:logback-core:1.2.11` - Logback core

**Test Dependencies**:
- `junit:junit:4.13.2` - Unit testing framework
- `org.mockito:mockito-core:4.5.1` - Mocking framework

**All dependencies are Java 8 compatible.**

## Conclusion

All technology choices align with:
- Project Constitution requirements (Java 8, Maven, CLI-only, INI config)
- Feature specification requirements (FR-001 through FR-031)
- User clarifications (session 2026-01-08)
- Best practices for Java ETL CLI tools

No further research required. Proceeding to Phase 1: Design & Contracts.
