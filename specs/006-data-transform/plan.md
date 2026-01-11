# Implementation Plan: Data Transformation Pipeline

**Branch**: `006-data-transform` | **Date**: 2026-01-11 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/006-data-transform/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

This implementation plan defines the architecture and design for the data transformation pipeline component of the ETL tool. The feature enables transformation of extracted financial data from source models (Xbond quote, Xbond trade, Bond Future quote) to target models optimized for DolphinDB storage, with concurrent processing capabilities for improved throughput.

The implementation involves:
1. Creating a common Transformer API for many-to-many source-to-target mapping
2. Implementing three concrete transformers for the primary data types
3. Developing TransformSubprocess to orchestrate concurrent transformations
4. Integrating with existing ETLContext for data flow management

## Technical Context

**Language/Version**: Java 8
**Primary Dependencies**: SLF4J + Logback (logging), JUnit 4 (testing), Mockito (mocking), Apache Commons Configuration (already in project)
**Storage**: DolphinDB (via existing DolphinDBLoader integration)
**Testing**: JUnit 4, Mockito
**Target Platform**: Cross-platform CLI (Windows/Linux/macOS)
**Project Type**: Single project with modular package structure
**Performance Goals**:
  - 10,000 records transformed in under 30 seconds for any single data type
  - 40%+ speedup with concurrent processing across all three data types
  - 1M+ records per hour throughput capacity
**Constraints**:
  - Java 8 compatibility (non-negotiable per constitution)
  - CLI-only interface (no GUI/Web)
  - INI configuration format
  - >60% test coverage required (TDD)
  - Concurrent transformations must handle resource contention gracefully
**Scale/Scope**:
  - Three transformer implementations (83, 15, and 96 fields respectively)
  - One common Transformer API
  - One TransformSubprocess orchestrator
  - Support for concurrent processing of multiple data types

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Refer to `.specify/memory/constitution.md` for current project principles. Gates include:

- ✅ Java 8 platform compliance
- ✅ Maven build tool usage
- ✅ CLI interface only (no GUI/Web)
- ✅ INI configuration format
- ✅ Component boundary clarity (Transformer API, TransformSubprocess)
- ✅ TDD with >60% coverage
- ✅ Bug fix version recording (not applicable - new feature)
- ✅ Third-party library usage (will use existing Apache Commons, SLF4J, JUnit)
- ✅ Full build/test pass
- ✅ Protected files restriction (will not modify .specify/ or .codebuddy/)
- ✅ Primitive number field initialization (no default zero - will use wrapper types or explicit initialization)

**Status**: ✅ All gates passed. No violations documented.

**Post-Phase 1 Re-Check**: ✅ Confirmed - design decisions (reflection-based mapping, ExecutorService concurrency, sentinel value initialization) all align with constitution principles.

## Project Structure

### Documentation (this feature)

```text
specs/006-data-transform/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/
├── main/java/com/sdd/etl/
│   ├── loader/transformer/
│   │   ├── Transformer.java                 # Common API interface
│   │   ├── TransformerFactory.java          # Factory for selecting transformer by type
│   │   ├── exceptions/
│   │   │   └── TransformationException.java # Custom exception for transformation failures
│   │   ├── XbondQuoteTransformer.java      # Concrete implementation (83 fields)
│   │   ├── XbondTradeTransformer.java      # Concrete implementation (15 fields)
│   │   └── BondFutureQuoteTransformer.java # Concrete implementation (96 fields)
│   └── subprocess/
│       └── TransformSubprocess.java        # Concrete implementation (existing stub)
│
└── test/java/com/sdd/etl/
    ├── loader/transformer/
    │   ├── TransformerTest.java             # Abstract base test class
    │   ├── TransformerFactoryTest.java
    │   ├── XbondQuoteTransformerTest.java
    │   ├── XbondTradeTransformerTest.java
    │   └── BondFutureQuoteTransformerTest.java
    └── subprocess/
        └── TransformSubprocessTest.java
```

**Structure Decision**: Single project structure with clear package boundaries. Transformer implementations are placed under `com.sdd.etl.loader.transformer` to maintain logical grouping with loader components (as they prepare data for loading). The TransformSubprocess remains in its existing location under `com.sdd.etl.subprocess` to maintain subprocess consistency.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| N/A | N/A | N/A |

## Phase 0: Research

### Research Objectives

1. **Field Mapping Strategy**: Determine optimal approach for name-based field mapping between source and target models
2. **Concurrency Implementation**: Identify best practices for concurrent transformation execution in Java 8
3. **Error Handling Patterns**: Research exception handling and failure propagation strategies for concurrent operations
4. **Type Conversion**: Validate data type conversion requirements between source and target models

### Research Findings

**Decision 1: Field Mapping Using Java Reflection**

**Rationale**:
- Name-based field mapping is explicitly required by FR-006
- Java reflection provides a flexible, type-safe mechanism for dynamic field access
- Eliminates need for manual field-by-field mapping code
- Supports all three transformer implementations with minimal code duplication
- Performance is acceptable given record-by-record transformation requirement (FR-003)

**Implementation Approach**:
- Use `java.lang.reflect.Field` API to access fields by name
- Leverage source/target model getter/setter methods where available
- Handle type conversions automatically (e.g., Integer → int, String → String)
- Gracefully handle missing fields by leaving target fields unassigned (per FR-007)

**Alternatives Considered**:
- Manual field-by-field mapping: Rejected due to high code duplication (83+15+96 = 194 field mappings)
- Map-based intermediate representation: Rejected as it adds unnecessary indirection
- Annotation-based mapping (e.g., @Mapping): Rejected as it adds complexity without clear benefit for simple name-based mapping

**Decision 2: Concurrent Processing Using ExecutorService**

**Rationale**:
- Java 8 provides `ExecutorService` framework for concurrent task execution
- Meets FR-011 requirement for concurrent transformation execution
- Proven, well-documented pattern with built-in exception handling
- Thread pool can be sized based on available CPU cores
- Supports Future.get() for exception propagation (critical for FR-014)

**Implementation Approach**:
- Create `ExecutorService` with fixed thread pool (size = number of data types)
- Submit each transformer task as a `Callable<TransformResult>`
- Use `ExecutorService.invokeAll()` for concurrent execution
- Catch `ExecutionException` from any Future to halt entire process
- Properly shutdown executor after completion

**Alternatives Considered**:
- Java 8 parallel streams: Rejected as exception handling is less controllable for critical failure scenarios
- Fork/Join framework: Rejected as overkill for simple parallel task execution
- Manual thread creation: Rejected as it lacks built-in thread management and exception handling

**Decision 3: Immediate Failure Halt for Transform Exceptions**

**Rationale**:
- FR-014 explicitly requires immediate halt on any exception
- FR-015 requires user manual verification for transformation errors
- Prevents cascading errors and data corruption
- Aligns with ETL tool's data integrity requirements

**Implementation Approach**:
- Wrap each transformation in try-catch within callable
- On exception, cancel all pending tasks via `future.cancel(true)`
- Throw `TransformationException` with detailed context (source type, record count, error message)
- Propagate exception to caller via TransformSubprocess
- Log error details using SLF4J for debugging

**Alternatives Considered**:
- Continue processing other data types on failure: Rejected (violates FR-014)
- Retry mechanism: Rejected as not in requirements; manual verification preferred
- Partial success reporting: Rejected as it complicates downstream processing

**Decision 4: Type Conversion with Null Safety**

**Rationale**:
- Source models use wrapper types (Integer, Double, Long) to allow null
- Target models use primitive types with explicit initialization (-1 for int, Double.NaN for double)
- Constitution Principle 11 requires no default zero initialization
- Type conversion must handle null values gracefully

**Implementation Approach**:
- For null source values, set target field to sentinel value (-1 or Double.NaN)
- Preserve existing sentinel value initialization from target models
- Use wrapper type checks before conversion
- Log warnings for unexpected type mismatches during development/testing

**Alternatives Considered**:
- Throw exception on type mismatch: Rejected as too strict for optional fields
- Auto-convert with default values: Rejected (violates Constitution Principle 11)
- Skip field on mismatch: Accepted for optional fields, validate required fields separately

### Technology Stack Additions

No new dependencies required. Will use existing:
- Java 8 standard library (reflection, concurrency)
- SLF4J + Logback for logging
- JUnit 4 + Mockito for testing

## Phase 1: Design

### Data Model

See [data-model.md](./data-model.md) for detailed entity definitions, field mappings, and validation rules.

### API Contracts

See [contracts/](./contracts/) for interface definitions and transformation specifications.

### Quickstart Guide

See [quickstart.md](./quickstart.md) for developer onboarding and implementation examples.

### Agent Context Update

The agent context file has been updated to include:
- Transformer API pattern
- Field mapping strategy using reflection
- Concurrent processing with ExecutorService
- Exception handling requirements for transformation failures

Run: `powershell -ExecutionPolicy Bypass -File ".specify/scripts/powershell/update-agent-context.ps1" -AgentType codebuddy`

## Phase 2: Implementation Planning

*This section will be populated by `/speckit.tasks` command in the next phase.*

## Success Metrics

Track the following metrics during implementation:
1. Unit test coverage > 60% (Constitution requirement)
2. Integration test coverage for all three transformers
3. Performance benchmarks: 10,000 records < 30s (SC-001)
4. Concurrent processing speedup > 40% (SC-002)
5. Zero data loss in transformation tests (SC-006)

## Risks and Mitigations

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Reflection performance overhead | Medium | Medium | Benchmark during development; optimize field access caching if needed |
| Concurrency deadlocks | Low | High | Use fixed thread pool; avoid shared mutable state; use immutable data |
| Type conversion errors | Medium | Medium | Comprehensive unit tests; integration tests with real data samples |
| Memory pressure with large datasets | Medium | Medium | Monitor memory usage; consider batching for very large record sets (>100K) |
| Missing field handling ambiguity | Low | Low | Clear FR-007 requirement; document behavior in Transformer API javadoc |

## Open Questions

None at this time. All technical decisions have been resolved in Phase 0 research.
