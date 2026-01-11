---
description: "Task list for implementing DolphinDB Data Loader"
---

# Tasks: Load Data to DolphinDB

**Input**: Design documents from `/specs/005-dolphindb-loader/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: TDD with >60% coverage required per project constitution (PR-006). All tasks include test implementation.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: `src/`, `tests/` at repository root
- **Java 8 platform**: All code compatible with JDK 1.8
- **Maven build**: All dependencies managed via pom.xml
- **Loader module**: New components under `com.sdd.etl.loader` package

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, dependency management, and directory structure

- [ ] T001 Update Maven pom.xml with DolphinDB Java API dependency in pom.xml
- [ ] T002 [P] Create loader module directory structure: src/main/java/com/sdd/etl/loader/api/, dolphin/, config/
- [ ] T003 [P] Create resources directory for scripts: src/main/resources/scripts/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T004 [P] Create common Loader interface in src/main/java/com/sdd/etl/loader/api/Loader.java
- [ ] T005 [P] Create LoaderConfiguration class in src/main/java/com/sdd/etl/loader/config/LoaderConfiguration.java
- [ ] T006 [P] Create TargetTable descriptor in src/main/java/com/sdd/etl/loader/api/TargetTable.java
- [ ] T007 [P] Create exception hierarchy in src/main/java/com/sdd/etl/loader/api/exceptions/
- [ ] T008 [P] Create INI configuration parser in src/main/java/com/sdd/etl/loader/config/ConfigParser.java

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Load transformed data to DolphinDB via Java API (Priority: P1) 🎯 MVP

**Goal**: Load transformed data to DolphinDB using its Java API, enabling the final step of the ETL pipeline.

**Independent Test**: Run the loader with mock transformed data and verify that data appears in the correct DolphinDB tables.

### Tests for User Story 1 (TDD REQUIRED)

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T009 [P] [US1] Write unit tests for DolphinDBLoader in src/test/java/com/sdd/etl/loader/dolphin/DolphinDBLoaderTest.java
- [ ] T010 [P] [US1] Write integration tests with embedded DolphinDB instance in src/test/java/com/sdd/etl/loader/dolphin/integration/DolphinDBLoaderIntegrationTest.java

### Implementation for User Story 1

- [ ] T011 [P] [US1] Implement DolphinDBLoader class in src/main/java/com/sdd/etl/loader/dolphin/DolphinDBLoader.java
- [ ] T012 [P] [US1] Implement DolphinDBConnection wrapper in src/main/java/com/sdd/etl/loader/dolphin/DolphinDBConnection.java
- [ ] T013 [P] [US1] Implement DolphinDBScriptExecutor in src/main/java/com/sdd/etl/loader/dolphin/DolphinDBScriptExecutor.java
- [ ] T014 [P] [US1] Implement ExternalSorter for disk‑based sorting in src/main/java/com/sdd/etl/loader/dolphin/sort/ExternalSorter.java
- [ ] T015 [P] [US1] Implement data conversion utilities for column‑based insertion in src/main/java/com/sdd/etl/loader/dolphin/DataConverter.java
- [ ] T016 [US1] Integrate all components and ensure loader lifecycle works end‑to‑end

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently. Data can be loaded to DolphinDB via Java API with temporary tables and cleanup.

---

## Phase 4: User Story 2 - Integrate loader with existing ETL subprocesses (Priority: P2)

**Goal**: Integrate the loader with LoadSubprocess and CleanSubprocess, ensuring loading and cleanup are part of the automated ETL workflow.

**Independent Test**: Run the full ETL workflow and verify that tables are created during loading and removed after cleanup.

### Tests for User Story 2 (TDD REQUIRED)

- [ ] T017 [P] [US2] Write unit tests for LoadSubprocess integration in src/test/java/com/sdd/etl/subprocess/LoadSubprocessTest.java
- [ ] T018 [P] [US2] Write integration tests for subprocess orchestration in src/test/java/com/sdd/etl/subprocess/integration/SubprocessIntegrationTest.java

### Implementation for User Story 2

- [ ] T019 [US2] Update LoadSubprocess to instantiate and use DolphinDBLoader in src/main/java/com/sdd/etl/subprocess/LoadSubprocess.java
- [ ] T020 [US2] Update CleanSubprocess to call cleanupTemporaryTables() and shutdown() in src/main/java/com/sdd/etl/subprocess/CleanSubprocess.java
- [ ] T021 [US2] Integrate loader configuration parsing into ETLContext initialization

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently. The loader is fully integrated with existing ETL subprocesses.

---

## Phase 5: User Story 3 - Handle exceptions during data loading (Priority: P3)

**Goal**: Any loading exception must break the ETL process with clear error reporting, enabling manual investigation and ensuring data integrity.

**Independent Test**: Simulate various failure scenarios (network errors, invalid data) and verify the process stops with appropriate error messages.

### Tests for User Story 3 (TDD REQUIRED)

- [ ] T022 [P] [US3] Write error scenario tests in src/test/java/com/sdd/etl/loader/dolphin/ErrorHandlingTest.java
- [ ] T023 [P] [US3] Write validation failure tests for null sort field handling in src/test/java/com/sdd/etl/loader/dolphin/NullSortFieldTest.java

### Implementation for User Story 3

- [ ] T024 [US3] Implement exception hierarchy and error handling strategies in src/main/java/com/sdd/etl/loader/api/exceptions/
- [ ] T025 [US3] Add comprehensive logging for loader operations in src/main/java/com/sdd/etl/loader/dolphin/DolphinDBLogger.java
- [ ] T026 [US3] Ensure temporary tables remain intact on failure for forensic analysis

**Checkpoint**: All user stories should now be independently functional. The loader stops the ETL process on any exception, provides descriptive error messages, and leaves temporary tables for manual investigation.

---

## Phase 6: Polish & Cross‑Cutting Concerns

**Purpose**: Improvements that affect multiple user stories, final validation, and documentation

- [ ] T027 [P] Update project documentation and README.md with loader usage instructions
- [ ] T028 [P] Validate quickstart.md manual testing checklist with actual tests
- [ ] T029 Run full build and test suite to ensure >60% coverage and no regressions
- [ ] T030 [P] Code cleanup, apply consistent formatting, and final review
- [ ] T031 [P] Update agent context file CODEBUDDY.md with new technology entries
- [ ] T032 [P] Implement bug-fix version recording mechanism (FR‑010) with logging for future reference
- [ ] T033 [P] Ensure explicit initialization of primitive numeric fields in TargetDataModel classes (FR‑011)
- [ ] T034 [P] Run load-performance benchmark with 1 M synthetic records and verify 30‑minute threshold (SC‑001)
- [ ] T035 [P] Verify that adding a stub MySQL loader requires <200 lines of new code (SC‑004)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies – can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion – BLOCKS all user stories.
- **User Stories (Phase 3+)**: All depend on Foundational phase completion.
  - User stories can then proceed in parallel (if staffed).
  - Or sequentially in priority order (P1 → P2 → P3).
- **Polish (Phase 6)**: Depends on all desired user stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) – No dependencies on other stories.
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) – Integrates with US1 components but independently testable.
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) – Builds on US1/US2 error handling.

### Within Each User Story

- Tests MUST be written and FAIL before implementation (TDD).
- Models before services.
- Services before endpoints/integration.
- Core implementation before integration with existing components.
- Story complete before moving to next priority (for sequential execution).

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel (T002, T003).
- All Foundational tasks marked [P] can run in parallel (T004‑T008).
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows).
- Tests for a user story marked [P] can run in parallel.
- Models within a story marked [P] can run in parallel.
- Different user stories can be worked on in parallel by different team members.

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together (TDD):
Task: "Write unit tests for DolphinDBLoader in src/test/java/com/sdd/etl/loader/dolphin/DolphinDBLoaderTest.java"
Task: "Write integration tests with embedded DolphinDB instance in src/test/java/com/sdd/etl/loader/dolphin/integration/DolphinDBLoaderIntegrationTest.java"

# Launch all models for User Story 1 together:
Task: "Implement DolphinDBLoader class in src/main/java/com/sdd/etl/loader/dolphin/DolphinDBLoader.java"
Task: "Implement DolphinDBConnection wrapper in src/main/java/com/sdd/etl/loader/dolphin/DolphinDBConnection.java"
Task: "Implement DolphinDBScriptExecutor in src/main/java/com/sdd/etl/loader/dolphin/DphinDBScriptExecutor.java"
Task: "Implement ExternalSorter for disk‑based sorting in src/main/java/com/sdd/etl/loader/dolphin/sort/ExternalSorter.java"
Task: "Implement data conversion utilities for column‑based insertion in src/main/java/com/sdd/etl/loader/dolphin/DataConverter.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. **Complete Phase 1: Setup** – Update pom.xml, create directories.
2. **Complete Phase 2: Foundational** – Core interfaces and configuration (CRITICAL – blocks all stories).
3. **Complete Phase 3: User Story 1** – DolphinDB loader with end‑to‑end functionality.
4. **STOP and VALIDATE** – Test User Story 1 independently.
5. **Deploy/demo** – Ready for initial ETL runs with DolphinDB loading.

### Incremental Delivery

1. **Complete Setup + Foundational** → Foundation ready.
2. **Add User Story 1** → Test independently → Deploy/Demo (MVP!).
3. **Add User Story 2** → Test independently → Deploy/Demo (full integration).
4. **Add User Story 3** → Test independently → Deploy/Demo (robust error handling).
5. Each story adds value without breaking previous stories.

### Parallel Team Strategy

With multiple developers:

1. **Team completes Setup + Foundational together**.
2. **Once Foundational is done**:
   - Developer A: User Story 1 (DolphinDB loader implementation).
   - Developer B: User Story 2 (subprocess integration).
   - Developer C: User Story 3 (exception handling).
3. **Stories complete and integrate independently**.

---

## Notes

- **[P] tasks** = different files, no dependencies.
- **[Story] label** maps task to specific user story for traceability.
- **Each user story** should be independently completable and testable.
- **TDD protocol**: Verify tests fail before implementing.
- **Commit strategy**: Commit after each task or logical group.
- **Checkpoint validation**: Stop at any checkpoint to validate story independently.
- **Avoid**: vague tasks, same‑file conflicts, cross‑story dependencies that break independence.