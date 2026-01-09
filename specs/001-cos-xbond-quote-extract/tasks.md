---
description: "Task list for COS Xbond Quote Extraction feature"
---

# Tasks: COS Xbond Quote Extraction

**Input**: Design documents from `/specs/001-cos-xbond-quote-extract/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/, quickstart.md

**Tests**: Tests are MANDATORY per Constitution (TDD with >60% coverage) and spec.md ("User Scenarios & Testing *(mandatory)*").

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: `src/`, `tests/` at repository root
- Existing packages follow: `src/main/java/com/sdd/etl/[package]/`
- New components for this feature: `src/main/java/com/sdd/etl/source/extract/[component]/`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and dependency updates

**⚠️ CRITICAL**: These tasks must complete before any implementation can begin

- [ ] T001 Update pom.xml with COS SDK and OpenCSV dependencies in `D:\idea-workspace2\sdd-etl-tool\pom.xml`
- [ ] T002 [P] Create base package structure for extractor components in `src/main/java/com/sdd/etl/source/extract/`
- [ ] T003 [P] Create test package structure for extractor components in `src/test/java/com/sdd/etl/source/extract/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core abstractions and configuration extensions that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T004 Extend ETConfiguration to support COS source type in `src/main/java/com/sdd/etl/config/ETConfiguration.java`
- [ ] T005 [P] Extend ConfigurationLoader to parse COS parameters in `src/main/java/com/sdd/etl/config/ConfigurationLoader.java`
- [ ] T006 [P] Create CosSourceConfig model in `src/main/java/com/sdd/etl/source/extract/cos/config/CosSourceConfig.java`
- [ ] T007 [P] Create CosFileMetadata model in `src/main/java/com/sdd/etl/source/extract/cos/model/CosFileMetadata.java`
- [ ] T008 [P] Create RawQuoteRecord model in `src/main/java/com/sdd/etl/source/extract/cos/model/RawQuoteRecord.java`
- [ ] T009 Create CosClient interface and Tencent SDK implementation in `src/main/java/com/sdd/etl/source/extract/cos/client/CosClientImpl.java`
- [ ] T042 [P] Implement file size validation with configurable threshold (default 100MB) in `src/main/java/com/sdd/etl/source/extract/cos/CosExtractor.java`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Extract Xbond Quote data from COS (Priority: P1) 🎯 MVP

**Goal**: Implement concrete extractor that retrieves Xbond Quote data from COS by selecting files matching configured rules, downloading them, parsing CSV content, and converting to standardized `SourceDataModel` records.

**Independent Test**: Can be fully tested by running an ETL job for a single day where COS contains known matching and non-matching files, and verifying that only matching files contribute records to the extracted output.

### Tests for User Story 1 (MANDATORY - TDD required)

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T010 [P] [US1] Unit test for XbondQuoteDataModel validation rules in `src/test/java/com/sdd/etl/model/XbondQuoteDataModelTest.java`
- [ ] T011 [P] [US1] Unit test for RawQuoteRecord data mapping in `src/test/java/com/sdd/etl/source/extract/cos/model/RawQuoteRecordTest.java`
- [ ] T012 [P] [US1] Unit test for CosSourceConfig validation in `src/test/java/com/sdd/etl/source/extract/cos/config/CosSourceConfigTest.java`
- [ ] T013 [P] [US1] Contract test for Extractor interface in `src/test/java/com/sdd/etl/source/extract/ExtractorContractTest.java`
- [ ] T014 [P] [US1] Integration test for XbondQuoteExtractor with mocked COS client in `src/test/java/com/sdd/etl/source/extract/cos/XbondQuoteExtractorIntegrationTest.java`

### Implementation for User Story 1

- [ ] T015 [US1] Implement Extractor interface in `src/main/java/com/sdd/etl/source/extract/Extractor.java`
- [ ] T016 [US1] Implement abstract CosExtractor base class in `src/main/java/com/sdd/etl/source/extract/cos/CosExtractor.java`
- [ ] T017 [US1] Implement XbondQuoteDataModel extending SourceDataModel in `src/main/java/com/sdd/etl/model/XbondQuoteDataModel.java`
- [ ] T018 [US1] Implement XbondQuoteExtractor extending CosExtractor in `src/main/java/com/sdd/etl/source/extract/cos/XbondQuoteExtractor.java`
- [ ] T019 [US1] Implement CsvParser utility for streaming CSV parsing in `src/main/java/com/sdd/etl/source/extract/cos/CsvParser.java`
- [ ] T020 [US1] Add validation and error handling for extraction failures in `src/main/java/com/sdd/etl/source/extract/cos/XbondQuoteExtractor.java`

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently. MVP achieved!

---

## Phase 4: User Story 2 - Support multiple extractors under a common API (Priority: P2)

**Goal**: Ensure data extraction is implemented through a common Extractor API that does not embed COS-specific assumptions, enabling additional source systems (e.g., databases) to be supported without changing the orchestration contract.

**Independent Test**: Can be tested by registering a second extractor (stubbed) that uses the same API and verifying it can filter based on context and produce output in the same record model.

### Tests for User Story 2 (MANDATORY - TDD required)

- [ ] T021 [P] [US2] Contract test for Extractor lifecycle (setup, extract, cleanup, validate) in `src/test/java/com/sdd/etl/source/extract/ExtractorLifecycleTest.java`
- [ ] T022 [P] [US2] Unit test for CosExtractor abstract methods in `src/test/java/com/sdd/etl/source/extract/cos/CosExtractorTest.java`
- [ ] T023 [P] [US2] Integration test for multiple extractor types using common API in `src/test/java/com/sdd/etl/source/extract/MultiExtractorIntegrationTest.java`

### Implementation for User Story 2

- [ ] T024 [P] [US2] Refactor Extractor interface to ensure clean separation of concerns in `src/main/java/com/sdd/etl/source/extract/Extractor.java`
- [ ] T025 [US2] Implement ExtractorFactory for creating extractors based on configuration type in `src/main/java/com/sdd/etl/source/extract/ExtractorFactory.java`
- [ ] T026 [US2] Update ETConfiguration to support multiple extractor configurations in `src/main/java/com/sdd/etl/config/ETConfiguration.java`
- [ ] T027 [US2] Create abstract SourceDataModel extensions for different source types in `src/main/java/com/sdd/etl/model/`

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently. The Extractor API is now truly source-agnostic.

---

## Phase 5: User Story 3 - Consolidate multi-source extraction results (Priority: P2)

**Goal**: Implement extraction step to execute multiple data extractions concurrently and consolidate all extracted records into a single output set, so that downstream steps receive one consistent input.

**Independent Test**: Can be tested by configuring two extractors that each return a known number of records and verifying the consolidated output contains the combined set and that extraction completes only after both finish.

### Tests for User Story 3 (MANDATORY - TDD required)

- [ ] T028 [P] [US3] Unit test for concurrent extractor execution in `src/test/java/com/sdd/etl/subprocess/ConcurrentExtractionTest.java`
- [ ] T029 [P] [US3] Integration test for extraction subprocess with multiple extractors in `src/test/java/com/sdd/etl/subprocess/ExtractSubprocessIntegrationTest.java`
- [ ] T030 [P] [US3] Unit test for record consolidation logic in `src/main/java/com/sdd/etl/subprocess/ExtractSubprocess.java`

### Implementation for User Story 3

- [ ] T031 [US3] Extend ExtractSubprocess to support multiple extractors in `src/main/java/com/sdd/etl/subprocess/ExtractSubprocess.java`
- [ ] T032 [US3] Implement ExecutorService-based concurrent extraction in `src/main/java/com/sdd/etl/subprocess/ExtractSubprocess.java`
- [ ] T033 [US3] Add result consolidation and error handling for multi-extractor scenarios in `src/main/java/com/sdd/etl/subprocess/ExtractSubprocess.java`
- [ ] T034 [US3] Update DailyETLWorkflow to use enhanced ExtractSubprocess in `src/main/java/com/sdd/etl/workflow/DailyETLWorkflow.java`

**Checkpoint**: All user stories should now be independently functional. Multi-source concurrent extraction is operational.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T035 [P] Update quickstart.md with implemented examples in `specs/001-cos-xbond-quote-extract/quickstart.md`
- [ ] T036 [P] Implement structured JSON logging with timestamp, level, category, file count, record count, and error details in `src/main/java/com/sdd/etl/source/extract/cos/CosExtractor.java`
- [ ] T037 Code cleanup and refactoring across all new extractor components
- [ ] T038 [P] Add performance metrics and monitoring hooks in `src/main/java/com/sdd/etl/source/extract/cos/CosClientImpl.java`
- [ ] T039 Security hardening for COS credential handling in `src/main/java/com/sdd/etl/source/extract/cos/config/CosSourceConfig.java`
- [ ] T040 Run full build and test suite to verify >60% coverage requirement
- [ ] T041 [P] Update project documentation with COS extraction feature in `docs/`
- [ ] T043 [P] Implement performance monitoring with 30-minute extraction time target validation, metrics recording (extraction time, file count, record count, success/failure), and structured reporting in `src/main/java/com/sdd/etl/subprocess/ExtractSubprocess.java`

---

## Edge Case Coverage

| Edge Case (spec.md:L66-74) | Task Coverage | Implementation Notes |
|----------------------------|---------------|----------------------|
| No matching files in COS | T020 | Error handling and logging for empty result sets |
| Large number of matching files causing long runtimes | T043 | Performance monitoring with time targets |
| Duplicate files/records across sources | T033 | Consolidation logic retains duplicates |
| Partial download failures or transient connectivity | T020 | Structured error messages with retry context |
| Corrupted file content or unexpected encoding | T019, T020 | CSV parsing validation and error handling |
| Single file with records for multiple dates | T019, T018 | Date-based filtering per context |
| Extremely large individual files exceeding memory limits | T042 | File size validation with configurable threshold |

**Coverage Status**: All edge cases have task mappings. Implementation should include explicit handling for each scenario.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Phase 6)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Depends on US1 implementation for API validation
- **User Story 3 (P2)**: Can start after Foundational (Phase 2) - Depends on US1 and US2 for multi-extractor integration

### Within Each User Story

- Tests (MANDATORY) MUST be written and FAIL before implementation
- Models before services
- Services before endpoints
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, user stories US1, US2, US3 can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Models within a story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together (mandatory TDD):
Task: "Unit test for XbondQuoteDataModel validation rules in src/test/java/com/sdd/etl/model/XbondQuoteDataModelTest.java"
Task: "Unit test for RawQuoteRecord data mapping in src/test/java/com/sdd/etl/source/extract/cos/model/RawQuoteRecordTest.java"
Task: "Unit test for CosSourceConfig validation in src/test/java/com/sdd/etl/source/extract/cos/config/CosSourceConfigTest.java"
Task: "Contract test for Extractor interface in src/test/java/com/sdd/etl/source/extract/ExtractorContractTest.java"

# Launch core implementation tasks in parallel (after tests):
Task: "Implement Extractor interface in src/main/java/com/sdd/etl/source/extract/Extractor.java"
Task: "Implement abstract CosExtractor base class in src/main/java/com/sdd/etl/source/extract/cos/CosExtractor.java"
Task: "Implement XbondQuoteDataModel extending SourceDataModel in src/main/java/com/sdd/etl/model/XbondQuoteDataModel.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently (full extraction from COS works)
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo (API now source-agnostic)
4. Add User Story 3 → Test independently → Deploy/Demo (multi-source concurrency)
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 (COS extractor implementation)
   - Developer B: User Story 2 (API abstraction and factory)
   - Developer C: User Story 3 (concurrent execution and consolidation)
3. Stories complete and integrate independently

---

## Notes

- **[P] tasks** = different files, no dependencies
- **[Story] label** maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing (TDD mandatory)
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- **Double with Double.NaN defaults** for all price/yield fields (per user requirement)
- **Java 8 compatibility** is non-negotiable (per Constitution)
- **INI configuration format** required (per Constitution)
- **>60% test coverage** required (per Constitution)
