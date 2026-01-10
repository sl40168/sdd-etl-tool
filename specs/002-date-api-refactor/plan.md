# Implementation Plan: Refactor Date APIs and Add Default Config

**Branch**: `002-date-api-refactor` | **Date**: 2026-01-10 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `specs/002-date-api-refactor/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Refactor three date-related APIs to use strongly-typed `LocalDate` objects instead of `String` representations, improving type safety and reducing parsing errors. Additionally, create a default INI configuration file that demonstrates all existing components for user reference. The refactoring requires updating all dependent code while maintaining backward compatibility through existing tests.

## Technical Context

**Language/Version**: Java 8 (non-negotiable per constitution)  
**Primary Dependencies**: Apache Commons Configuration (INI parsing), JUnit 4 (testing), Maven wrapper  
**Storage**: N/A (config files only)  
**Testing**: JUnit 4 with >60% coverage requirement, TDD practice  
**Target Platform**: Cross-platform CLI (Java 8 JRE)  
**Project Type**: Single CLI application  
**Performance Goals**: Date API calls complete within 5 ms on standard hardware (within 10 % of equivalent String‑parsing performance)  
**Constraints**: Must maintain backward compatibility for existing tests; cannot break dependent code  
**Scale/Scope**: Internal refactoring affecting ~3 core classes and their dependencies; demo config for existing components

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

✅ **Java 8 Platform Compliance**: The refactoring targets Java 8 APIs (LocalDate from Java 8) and maintains Java 8 compatibility.

✅ **Maven Build Tool Usage**: Project uses Maven wrapper; dependencies managed via pom.xml.

✅ **CLI Interface Only**: The feature is an internal API refactoring; no changes to CLI interface.

✅ **INI Configuration Format**: FR-005/FR-006 require creating a default INI configuration file; no other formats used.

✅ **Component Boundary Clarity**: Refactoring maintains existing component boundaries (ETLContext, WorkflowResult, DateRangeGenerator).

✅ **TDD with >60% Coverage**: Existing tests will be updated; TDD approach required for new test cases.

✅ **Bug Fix Version Recording**: Not a bug fix, but changes will be recorded in commit history.

✅ **Third-Party Library Usage**: Apache Commons Configuration for INI parsing; JUnit for testing.

✅ **Full Build/Test Pass**: All existing tests must pass after refactoring; new tests added.

✅ **Protected Files Restriction**: No modifications to `./specify/scripts/*.*`, `./specify/templates/*.*`, or `./codebuddy` directories.

✅ **Primitive Number Field Initialization**: Not directly applicable, but code changes will follow this principle.

**CONCLUSION**: ✅ **PASS** - All constitution requirements are satisfied. No violations documented.

**Post-Design Review**: After Phase 1 design (data model, contracts, quickstart), all constitution gates remain satisfied. The design maintains Java 8 compatibility, uses existing dependencies, and adheres to component boundaries.

## Project Structure

### Documentation (this feature)

```text
specs/002-date-api-refactor/
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
│   ├── cli/
│   │   ├── ETLCommandLine.java          # CLI entry point and argument parsing
│   │   └── CommandLineValidator.java    # Input validation logic
│   ├── context/
│   │   ├── ETLContext.java              # Core context implementation
│   │   ├── ContextManager.java          # Context lifecycle management
│   │   └── ContextConstants.java        # Context key constants
│   ├── workflow/
│   │   ├── DailyETLWorkflow.java       # Day-by-day orchestration
│   │   ├── SubprocessExecutor.java      # Subprocess sequencing logic
│   │   └── WorkflowEngine.java          # Main workflow coordinator
│   ├── logging/
│   │   ├── ETLogger.java                # Logging facade
│   │   └── StatusLogger.java            # Status logging implementation
│   ├── subprocess/
│   │   ├── ExtractSubprocess.java       # API definition (no implementation)
│   │   ├── TransformSubprocess.java     # API definition (no implementation)
│   │   ├── LoadSubprocess.java          # API definition (no implementation)
│   │   ├── ValidateSubprocess.java      # API definition (no implementation)
│   │   └── CleanSubprocess.java         # API definition (no implementation)
│   ├── model/
│   │   ├── SourceDataModel.java         # API definition (no implementation)
│   │   └── TargetDataModel.java         # API definition (no implementation)
│   └── config/
│       ├── ConfigurationLoader.java     # INI file loader
│       └── ETConfiguration.java        # Configuration POJO
├── main/resources/
│   └── logback.xml                       # Logging configuration
└── test/java/com/sdd/etl/
    ├── cli/
    ├── context/
    ├── workflow/
    ├── subprocess/
    └── model/

pom.xml
README.md
.etlconfig.ini (example configuration)
```

**Structure Decision**: Single Maven project with standard src/main/java and src/test/java structure. The refactoring focuses on three core classes in context, model, and util packages, maintaining existing component boundaries.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
