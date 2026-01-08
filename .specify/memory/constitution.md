<!--
Sync Impact Report:
- Version change: N/A → 1.0.0
- Added sections: All sections (initial creation)
- Removed sections: N/A
- Templates requiring updates:
  ✅ .specify/templates/plan-template.md (Constitution Check section aligns)
  ✅ .specify/templates/spec-template.md (requirements alignment)
  ✅ .specify/templates/tasks-template.md (task categorization aligns)
- Follow-up TODOs: None
-->

# Project Constitution

**Constitution Version**: 1.0.0
**Ratification Date**: 2026-01-08
**Last Amended Date**: 2026-01-08

## Project Overview

**Project Name**: sdd-etl-tool

**Project Description**: This is an ETL CLI Tool which can help you to extract data from different data sources, transform and load data to different data targets crossing days.

**Primary Language**: Java 8

**Build Tool**: Maven (using Maven wrapper)

**Architecture**: CLI-based ETL tool with clear component boundaries

## Core Principles

### 1. Java 8 Platform Requirement

The project MUST be built on Java 8 platform. All code, dependencies, and build configurations MUST target Java 8 compatibility.

**Rationale**: Ensures compatibility with legacy systems and standardizes the runtime environment across deployment targets.

---

### 2. Maven Build Tool

The project MUST be built using Maven build tool wrapper. All build processes, dependency management, and packaging MUST be handled through Maven.

**Rationale**: Provides standardized build process, dependency resolution, and ensures reproducible builds across different environments.

---

### 3. CLI Interface Exclusivity

All functionalities MUST be exposed on CLI interface. No GUI, web UI, or other interface types are permitted.

**Rationale**: Simplifies deployment, enables automation and scripting, and aligns with ETL tool use cases where batch processing and automation are primary concerns.

---

### 4. INI Configuration

All configurations MUST be configured and loaded from INI configuration file format. No other configuration formats (JSON, YAML, XML, properties files) are permitted for main configuration.

**Rationale**: Provides simple, human-readable configuration format that is easy to edit and maintain, suitable for ETL workflow parameters.

---

### 5. Component Boundary Clarity

The project MUST keep the boundaries of components clearly. Each component MUST have well-defined responsibilities and interfaces.

**Rationale**: Facilitates maintainability, testing, and future enhancements by ensuring loose coupling and high cohesion.

---

### 6. Test Driven Development with Coverage Requirement

Test Driven Development MUST be practiced. The unit test coverage MUST be greater than 60%. At any time, a new unit test MUST be added before bug fix.

**Rationale**: Ensures code quality, facilitates refactoring, and provides regression protection. The 60% threshold balances quality with development velocity.

---

### 7. Bug Fix Version Recording

All business bug fixes MUST be recorded by version and referred in future. Each bug fix MUST include version number and be tracked for future reference.

**Rationale**: Provides audit trail, facilitates debugging of similar issues, and enables impact analysis for future changes.

---

### 8. Use Third-Party Open Source Libraries

The project MUST encourage using well-known third-party open source libraries, instead of building your own implementations.

**Rationale**: Leverages community-tested solutions, reduces development time, and improves reliability by avoiding reinventing the wheel.

---

### 9. Build and Test Validation

At the end of each implementation round, full build and test MUST pass. No incomplete or failing builds are permitted.

**Rationale**: Ensures code quality and prevents technical debt accumulation. Maintains project health and deployability.

---

### 10. Protected Files

The following directories and files MUST NOT be modified:
- `./specify/scripts/*.*`
- `./specify/templates/*.*`
- `./codebuddy`

**Rationale**: These contain tooling infrastructure and automation that should remain stable across development cycles.

---

## Governance

### Amendment Procedure

1. **Proposal**: Any principle amendment MUST be proposed with clear rationale and impact analysis.
2. **Review**: Amendments MUST be reviewed against existing principles and project goals.
3. **Versioning**: All amendments MUST follow semantic versioning rules:
   - MAJOR: Backward incompatible governance/principle removals or redefinitions
   - MINOR: New principle/section added or materially expanded guidance
   - PATCH: Clarifications, wording, typo fixes, non-semantic refinements
4. **Propagation**: Amendments MUST propagate to dependent templates and documentation.

### Versioning Policy

Constitution versions follow semantic versioning (MAJOR.MINOR.PATCH):
- Version MUST increment with any amendment
- Current version: 1.0.0
- Historical versions MUST be preserved in commit history

### Compliance Review

1. **Pre-Implementation**: Constitution check MUST be performed before any major feature implementation (Phase 0 research gate).
2. **Periodic Review**: Constitution compliance MUST be reviewed quarterly or after significant changes.
3. **Violation Documentation**: Any violations MUST be documented with justification in implementation plan complexity tracking.
4. **Template Sync**: All dependent templates MUST remain synchronized with constitution principles.

## Template Dependencies

The following templates depend on this constitution and MUST remain synchronized:
- `.specify/templates/plan-template.md` - Constitution Check section
- `.specify/templates/spec-template.md` - Requirements and success criteria
- `.specify/templates/tasks-template.md` - Task categorization and structure
- `.specify/templates/agent-file-template.md` - Development guidelines

## Quick Reference

| Category | Standard | Notes |
|----------|----------|-------|
| Language | Java 8 | Non-negotiable |
| Build Tool | Maven | With wrapper |
| Interface | CLI only | No GUI/Web |
| Config Format | INI | Only INI files |
| Test Coverage | >60% | TDD required |
| Build Status | All tests pass | No exceptions |
