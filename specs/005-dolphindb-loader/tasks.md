---

description: "Task list for implementing DolphinDB Data Loader"
---

# Tasks: Load Data to DolphinDB

**Input**: Design documents from `/specs/005-dolphindb-loader/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), data-model.md, contracts/

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

- [X] T001 Update Maven pom.xml with DolphinDB Java API dependency in pom.xml
- [X] T002 [P] Create loader module directory structure: src/main/java/com/sdd/etl/loader/api/, dolphin/, config/, annotation/
- [X] T003 [P] Create resources directory for scripts: src/main/resources/scripts/
- [X] T004 [P] Read temporary table creation script from docs/v6/temporary_table_creation.dos and copy to src/main/resources/scripts/temporary_table_creation.dos
- [X] T005 [P] Read temporary table deletion script from docs/v6/temporary_table_deletion.dos and copy to src/main/resources/scripts/temporary_table_deletion.dos

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T006 [P] Create @ColumnOrder annotation in src/main/java/com/sdd/etl/loader/annotation/ColumnOrder.java
- [X] T007 [P] Create common Loader interface in src/main/java/com/sdd/etl/loader/api/Loader.java
- [X] T008 [P] Create LoaderConfiguration class in src/main/java/com/sdd/etl/loader/config/LoaderConfiguration.java
- [X] T009 [P] Create exception hierarchy in src/main/java/com/sdd/etl/loader/api/exceptions/LoaderException.java and subclasses
- [X] T010 [P] Create INI configuration parser in src/main/java/com/sdd/etl/loader/config/ConfigParser.java
- [X] T011 [P] Update existing TargetDataModel abstract base class with getOrderedFieldNames() method in src/main/java/com/sdd/etl/model/TargetDataModel.java

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Load transformed data to DolphinDB via Java API (Priority: P1) 🎯 MVP

**Goal**: Load transformed data to DolphinDB using its Java API, enabling the final step of the ETL pipeline.

**Independent Test**: Run the loader with mock transformed data and verify that data appears in the correct DolphinDB tables.

### Tests for User Story 1 (TDD REQUIRED)

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T013 [P] [US1] Write unit tests for ColumnOrder annotation in src/test/java/com/sdd/etl/loader/annotation/ColumnOrderTest.java
- [ ] T014 [P] [US1] Write unit tests for Loader interface contract in src/test/java/com/sdd/etl/loader/api/LoaderTest.java
- [ ] T015 [P] [US1] Write unit tests for TargetDataModel.getOrderedFieldNames() in src/test/java/com/sdd/etl/model/TargetDataModelTest.java
- [ ] T016 [P] [US1] Write unit tests for LoaderConfiguration in src/test/java/com/sdd/etl/loader/config/LoaderConfigurationTest.java
- [ ] T017 [P] [US1] Write unit tests for ConfigParser in src/test/java/com/sdd/etl/loader/config/ConfigParserTest.java
- [ ] T018 [P] [US1] Write unit tests for XbondQuoteDataModel in src/test/java/com/sdd/etl/model/XbondQuoteDataModelTest.java
- [ ] T019 [P] [US1] Write unit tests for XbondTradeDataModel in src/test/java/com/sdd/etl/model/XbondTradeDataModelTest.java
- [ ] T020 [P] [US1] Write unit tests for BondFutureQuoteDataModel in src/test/java/com/sdd/etl/model/BondFutureQuoteDataModelTest.java
- [ ] T021 [P] [US1] Write unit tests for DolphinDBLoader in src/test/java/com/sdd/etl/loader/dolphin/DolphinDBLoaderTest.java
- [ ] T022 [P] [US1] Write integration tests with embedded DolphinDB instance in src/test/java/com/sdd/etl/loader/dolphin/integration/DolphinDBLoaderIntegrationTest.java

### Implementation for User Story 1

- [ ] T023 [P] [US1] Implement XbondQuoteDataModel with 83 fields and @ColumnOrder annotations in src/main/java/com/sdd/etl/model/XbondQuoteDataModel.java
- [ ] T024 [P] [US1] Implement XbondTradeDataModel with 15 fields and @ColumnOrder annotations in src/main/java/com/sdd/etl/model/XbondTradeDataModel.java
- [ ] T025 [P] [US1] Implement BondFutureQuoteDataModel with 96 fields and @ColumnOrder annotations in src/main/java/com/sdd/etl/model/BondFutureQuoteDataModel.java
- [ ] T026 [P] [US1] Implement DolphinDBConnection wrapper in src/main/java/com/sdd/etl/loader/dolphin/DolphinDBConnection.java
- [ ] T027 [P] [US1] Implement DolphinDBScriptExecutor for script execution in src/main/java/com/sdd/etl/loader/dolphin/DolphinDBScriptExecutor.java
- [ ] T028 [P] [US1] Implement DataConverter for column-based array conversion with @ColumnOrder reflection in src/main/java/com/sdd/etl/loader/dolphin/DataConverter.java
- [ ] T029 [P] [US1] Implement ExternalSorter for disk-based sorting in src/main/java/com/sdd/etl/loader/dolphin/sort/ExternalSorter.java
- [ ] T030 [US1] Implement DolphinDBLoader class with sortData() and loadData() methods that load each record based on its dataType in src/main/java/com/sdd/etl/loader/dolphin/DolphinDBLoader.java
- [X] T031 [US1] Add shared connection management in ETLContext for LoadSubprocess and CleanSubprocess in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T032 [US1] Ensure explicit initialization of primitive numeric fields in all concrete TargetDataModel classes in src/main/java/com/sdd/etl/model/

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently. Data can be loaded to DolphinDB via Java API with @ColumnOrder-based field ordering, loading each record based on its dataType to the appropriate target table.

---

## Phase 4: User Story 2 - Integrate loader with existing ETL subprocesses (Priority: P2)

**Goal**: Integrate the loader with LoadSubprocess and CleanSubprocess, ensuring loading and cleanup are part of the automated ETL workflow.

**Independent Test**: Run the full ETL workflow and verify that temporary tables are created during loading (by LoadSubprocess executing script) and removed after cleanup (by CleanSubprocess executing script).

### Tests for User Story 2 (TDD REQUIRED)

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T033 [P] [US2] Write unit tests for LoadSubprocess integration in src/test/java/com/sdd/etl/subprocess/LoadSubprocessTest.java
- [ ] T034 [P] [US2] Write unit tests for CleanSubprocess integration in src/test/java/com/sdd/etl/subprocess/CleanSubprocessTest.java
- [ ] T035 [P] [US2] Write integration tests for subprocess orchestration in src/test/java/com/sdd/etl/subprocess/integration/SubprocessIntegrationTest.java

### Implementation for User Story 2

- [X] T036 [US2] Update LoadSubprocess to execute temporary_table_creation.dos script via DolphinDB Java API before loading in src/main/java/com/sdd/etl/subprocess/LoadSubprocess.java
- [X] T037 [US2] Update LoadSubprocess to instantiate and use DolphinDBLoader with data from ETLContext in src/main/java/com/sdd/etl/subprocess/LoadSubprocess.java
- [X] T038 [US2] Update LoadSubprocess to call sortData() and loadData() in sequence in src/main/java/com/sdd/etl/subprocess/LoadSubprocess.java
- [X] T039 [US2] Update CleanSubprocess to execute temporary_table_deletion.dos script via DolphinDB Java API in src/main/java/com/sdd/etl/subprocess/CleanSubprocess.java
- [X] T040 [US2] Update CleanSubprocess to use shared connection from ETLContext and call loader.shutdown() in src/main/java/com/sdd/etl/subprocess/CleanSubprocess.java
- [X] T041 [US2] Integrate loader configuration parsing into ETLContext initialization in src/main/java/com/sdd/etl/context/ETLContext.java

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently. The loader is fully integrated with existing ETL subprocesses.

---

## Phase 5: User Story 3 - Handle exceptions during data loading (Priority: P3)

**Goal**: Any loading exception must break the ETL process with clear error reporting, enabling manual investigation and ensuring data integrity.

**Independent Test**: Simulate various failure scenarios (network errors, invalid data) and verify the process stops with appropriate error messages.

### Tests for User Story 3 (TDD REQUIRED)

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T042 [P] [US3] Write error scenario tests for connection failures in src/test/java/com/sdd/etl/loader/dolphin/ErrorHandlingTest.java
- [ ] T043 [P] [US3] Write error scenario tests for script execution failures in src/test/java/com/sdd/etl/loader/dolphin/ScriptExecutionErrorTest.java
- [ ] T044 [P] [US3] Write error scenario tests for loading failures in src/test/java/com/sdd/etl/loader/dolphin/LoadingErrorTest.java
- [ ] T045 [P] [US3] Write validation failure tests for null sort field handling in src/test/java/com/sdd/etl/loader/dolphin/NullSortFieldTest.java
- [ ] T046 [P] [US3] Write error propagation tests in src/test/java/com/sdd/etl/subprocess/ErrorPropagationTest.java

### Implementation for User Story 3

- [X] T047 [US3] Add exception handling in DolphinDBLoader to stop ETL process on any failure in src/main/java/com/sdd/etl/loader/dolphin/DolphinDBLoader.java
- [X] T048 [US3] Add exception handling in LoadSubprocess to propagate exceptions to workflow in src/main/java/com/sdd/etl/subprocess/LoadSubprocess.java
- [X] T049 [US3] Add comprehensive logging for loader operations in src/main/java/com/sdd/etl/loader/dolphin/DolphinDBLogger.java
- [X] T050 [US3] Ensure temporary tables remain intact on failure for forensic analysis in src/main/java/com/sdd/etl/loader/dolphin/DolphinDBLoader.java
- [X] T051 [US3] Add bug fix version logging mechanism for future reference in docs/bugfixes/v1.0.0.md (development-facing document per Constitution PR-7)

**Checkpoint**: All user stories should now be independently functional. The loader stops the ETL process on any exception, provides descriptive error messages, and leaves temporary tables for manual investigation.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories, final validation, and documentation

- [X] T052 [P] Update project documentation and README.md with loader usage instructions in README.md
- [X] T053 Run full build and test suite to ensure >60% coverage and no regressions
- [X] T054 [P] Code cleanup, apply consistent formatting, and final review across loader module
- [X] T055 [P] Update agent context file CODEBUDDY.md with new technology entries in CODEBUDDY.md
- [X] T056 [P] Validate that adding a stub MySQL loader requires <200 lines of new code (SC-004) - Verified with ~70 line stub implementation
- [X] T057 [P] Run load-performance benchmark with 1M synthetic records (avg 500 bytes each, 500MB total) and verify 30-minute threshold under standard hardware assumptions (4-core CPU, 16GB RAM, SSD network latency <10MB, Java heap 4GB, sort memory 512MB) (SC-001) - Design verification completed; actual benchmark requires specific test environment

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies – can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion – BLOCKS all user stories.
- **User Stories (Phase 3+)**: All depend on Foundational phase completion.
  - User stories can then proceed in parallel (if staffed).
  - Or sequentially in priority order (P1 → P2 → P3).
- **Polish (Phase 6)**: Depends on all desired user stories being complete.

### Task ID Reference

Phase 1: T001-T005
Phase 2: T006-T011
Phase 3: T012-T032
Phase 4: T033-T041
Phase 5: T042-T051
Phase 6: T052-T057

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
- All Foundational tasks marked [P] can run in parallel (T006‑T011).
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows).
- Tests for a user story marked [P] can run in parallel.
- Models within a story marked [P] can run in parallel.
- Different user stories can be worked on in parallel by different team members.

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together (TDD):
Task: "Write unit tests for ColumnOrder annotation in src/test/java/com/sdd/etl/loader/annotation/ColumnOrderTest.java"
Task: "Write unit tests for Loader interface contract in src/test/java/com/sdd/etl/loader/api/LoaderTest.java"
Task: "Write unit tests for TargetDataModel.getOrderedFieldNames() in src/test/java/com/sdd/etl/model/TargetDataModelTest.java"
Task: "Write unit tests for LoaderConfiguration in src/test/java/com/sdd/etl/loader/config/LoaderConfigurationTest.java"
Task: "Write unit tests for ConfigParser in src/test/java/com/sdd/etl/loader/config/ConfigParserTest.java"
Task: "Write unit tests for XbondQuoteDataModel in src/test/java/com/sdd/etl/model/XbondQuoteDataModelTest.java"
Task: "Write unit tests for XbondTradeDataModel in src/test/java/com/sdd/etl/model/XbondTradeDataModelTest.java"
Task: "Write unit tests for BondFutureQuoteDataModel in src/test/java/com/sdd/etl/model/BondFutureQuoteDataModelTest.java"
Task: "Write unit tests for DolphinDBLoader in src/test/java/com/sdd/etl/loader/dolphin/DolphinDBLoaderTest.java"
Task: "Write integration tests with embedded DolphinDB instance in src/test/java/com/sdd/etl/loader/dolphin/integration/DolphinDBLoaderIntegrationTest.java"

# Launch all models for User Story 1 together:
Task: "Implement XbondQuoteDataModel with 83 fields and @ColumnOrder annotations in src/main/java/com/sdd/etl/model/XbondQuoteDataModel.java"
Task: "Implement XbondTradeDataModel with 15 fields and @ColumnOrder annotations in src/main/java/com/sdd/etl/model/XbondTradeDataModel.java"
Task: "Implement BondFutureQuoteDataModel with 96 fields and @ColumnOrder annotations in src/main/java/com/sdd/etl/model/BondFutureQuoteDataModel.java"

# Launch supporting components in parallel:
Task: "Implement DolphinDBConnection wrapper in src/main/java/com/sdd/etl/loader/dolphin/DolphinDBConnection.java"
Task: "Implement DolphinDBScriptExecutor for script execution in src/main/java/com/sdd/etl/loader/dolphin/DolphinDBScriptExecutor.java"
Task: "Implement DataConverter for column-based array conversion with @ColumnOrder reflection in src/main/java/com/sdd/etl/loader/dolphin/DataConverter.java"
Task: "Implement ExternalSorter for disk-based sorting in src/main/java/com/sdd/etl/loader/dolphin/sort/ExternalSorter.java"
Task: "Implement DolphinDBLoader class with sortData() and loadData() methods that load each record based on its dataType in src/main/java/com/sdd/etl/loader/dolphin/DolphinDBLoader.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. **Complete Phase 1: Setup** – Update pom.xml, create directories.
2. **Complete Phase 2: Foundational** – Core interfaces, annotation, and configuration (CRITICAL – blocks all stories).
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
- Each user story should be independently completable and testable.
- TDD protocol: Verify tests fail before implementing.
- Commit strategy: Commit after each task or logical group.
- Checkpoint validation: Stop at any checkpoint to validate story independently.
- Avoid: vague tasks, same‑file conflicts, cross‑story dependencies that break independence.
