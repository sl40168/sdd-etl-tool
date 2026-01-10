---
description: "Task list for implementing Date API Refactor and Default Config"
---

# Tasks: Refactor Date APIs and Add Default Config

**Input**: Design documents from `/specs/002-date-api-refactor/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are included as required by project constitution (TDD with >60% coverage). Each user story has independent test criteria.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single CLI application**: `src/main/java/`, `src/test/java/` per Maven standard structure

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Verify environment and branch readiness

- [ ] T001 Verify current branch is `002-date-api-refactor` using `git status`
- [ ] T002 Ensure Java 8 environment and Maven wrapper are available

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Create shared date utilities needed for refactoring

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T003 [P] Create DateUtils helper class in `src/main/java/com/sdd/etl/util/DateUtils.java` with `parseDate(String)` and `formatDate(LocalDate)` methods using "YYYYMMDD" format

---

## Phase 3: User Story 1 - Use LocalDate for current date in ETLContext (Priority: P1) 🎯 MVP

**Goal**: Change `ETLContext.getCurrentDate()` return type from `String` to `LocalDate` and update all dependent code

**Independent Test**: Call `ETLContext.getCurrentDate()` and verify it returns a `LocalDate` object representing the correct date

### Tests for User Story 1

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T004 [P] [US1] Update ETLContextTest to expect LocalDate return type in `src/test/java/com/sdd/etl/context/ETLContextTest.java`
- [ ] T005 [P] [US1] Update ContextManagerTest to handle LocalDate in `src/test/java/com/sdd/etl/context/ContextManagerTest.java`

### Implementation for User Story 1

- [ ] T006 [P] [US1] Change `getCurrentDate()` return type to `LocalDate` in `src/main/java/com/sdd/etl/context/ETLContext.java`
- [ ] T007 [P] [US1] Change `setCurrentDate(String)` parameter to `LocalDate` in same file
- [ ] T008 [US1] Update internal implementation to store and retrieve `LocalDate` objects (still using `ContextConstants.CURRENT_DATE` key)
- [ ] T009 [US1] Find and update all callers of `getCurrentDate()` and `setCurrentDate()` using IDE search; convert `String` usage to `LocalDate` with `DateUtils.formatDate()` where needed for external interfaces

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently. Run `mvn test -Dtest=ETLContextTest` to verify.

---

## Phase 4: User Story 2 - Use LocalDate for start and end dates in WorkflowResult (Priority: P2)

**Goal**: Change `WorkflowResult.startDate` and `endDate` fields from `String` to `LocalDate` types

**Independent Test**: Create a WorkflowResult instance and verify that `getStartDate()` and `getEndDate()` return `LocalDate` objects

### Tests for User Story 2

- [ ] T010 [P] [US2] Update WorkflowResultTest to expect LocalDate fields in `src/test/java/com/sdd/etl/model/WorkflowResultTest.java`
- [ ] T011 [P] [US2] Update any integration tests that use WorkflowResult date fields

### Implementation for User Story 2

- [ ] T012 [P] [US2] Change `startDate` and `endDate` field types to `LocalDate` in `src/main/java/com/sdd/etl/model/WorkflowResult.java`
- [ ] T013 [P] [US2] Update getter/setter signatures to use `LocalDate` instead of `String`
- [ ] T014 [US2] Find and update all callers of `getStartDate()`, `setStartDate()`, `getEndDate()`, `setEndDate()` to handle `LocalDate` types

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently. Run `mvn test -Dtest=WorkflowResultTest` to verify.

---

## Phase 5: User Story 3 - Generate date ranges as List<LocalDate> (Priority: P2)

**Goal**: Change `DateRangeGenerator.generate()` return type from `List<String>` to `List<LocalDate>`

**Independent Test**: Call `DateRangeGenerator.generate()` and verify the returned list contains `LocalDate` objects for the specified date range

### Tests for User Story 3

- [ ] T015 [P] [US3] Update DateRangeGeneratorTest to expect List<LocalDate> in `src/test/java/com/sdd/etl/util/DateRangeGeneratorTest.java`
- [ ] T016 [P] [US3] Update any tests that use DateRangeGenerator output

### Implementation for User Story 3

- [ ] T017 [P] [US3] Change `generate()` return type to `List<LocalDate>` in `src/main/java/com/sdd/etl/util/DateRangeGenerator.java`
- [ ] T018 [US3] Update internal implementation to add `LocalDate` objects to list instead of formatted strings (lines 48-56)
- [ ] T019 [US3] Find and update all callers of `DateRangeGenerator.generate()` to handle `List<LocalDate>`; use `DateUtils.formatDate()` where string representation needed

**Checkpoint**: All three date API user stories should now be independently functional. Run `mvn test -Dtest=DateRangeGeneratorTest` to verify.

---

## Phase 6: User Story 4 - Provide default INI configuration file (Priority: P3)

**Goal**: Create default INI configuration file covering all existing components as a demo

**Independent Test**: Load the default INI file with the configuration loader and verify all expected sections and properties are present and valid

### Tests for User Story 4

- [ ] T020 [P] [US4] Add test for loading default config in `src/test/java/com/sdd/etl/config/ConfigurationLoaderTest.java`
- [ ] T021 [P] [US4] Verify required sections and properties exist in integration test

### Implementation for User Story 4

- [ ] T022 [P] [US4] Create default INI configuration file at `src/main/resources/config.ini` with sections: `[database]`, `[logging]`, `[etl]`, `[subprocess]`
- [ ] T023 [US4] Update `ConfigurationLoader` to validate required sections/properties and provide clear error messages if missing

**Checkpoint**: Default config file should load successfully. Run `mvn test -Dtest=ConfigurationLoaderTest` to verify.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Final validation, test coverage, and cleanup

- [ ] T024 [P] Run full test suite with `mvn test` and fix any failures
- [ ] T025 [P] Verify test coverage exceeds 60% using `mvn jacoco:report`
- [ ] T026 [P] Update any remaining `String` date usages in logging and error messages to use `DateUtils.formatDate()` for consistency
- [ ] T027 Run quickstart.md validation steps to ensure migration examples work
- [ ] T028 Perform final code review and commit all changes with descriptive commit message

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-6)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (US1 → US2 → US3 → US4)
- **Polish (Phase 7)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - May integrate with US1 but should be independently testable
- **User Story 3 (P2)**: Can start after Foundational (Phase 2) - Uses DateUtils, may integrate with US1/US2 but independent
- **User Story 4 (P3)**: Can start after Foundational (Phase 2) - Independent of date API changes

### Within Each User Story

- Tests (T004-T005, T010-T011, T015-T016, T020-T021) MUST be written and FAIL before implementation
- Implementation follows test updates
- Story complete before moving to next priority (but can parallelize if team capacity allows)

### Parallel Opportunities

- All [P] marked tasks can run in parallel
- Setup tasks (T001-T002) can run in parallel
- Foundational task (T003) can run independently
- Test tasks within a story can run in parallel (e.g., T004 and T005)
- Model/field change tasks (T006-T007, T012-T013, T017) can run in parallel
- Different user stories can be worked on in parallel by different team members after Foundational phase

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together:
Task: "Update ETLContextTest to expect LocalDate return type" (T004)
Task: "Update ContextManagerTest to handle LocalDate" (T005)

# Launch implementation tasks after tests:
Task: "Change getCurrentDate() return type to LocalDate" (T006)
Task: "Change setCurrentDate() parameter to LocalDate" (T007)
Task: "Update internal implementation" (T008) - depends on T006/T007
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T002)
2. Complete Phase 2: Foundational (T003) - CRITICAL - blocks all stories
3. Complete Phase 3: User Story 1 (T004-T009)
4. **STOP and VALIDATE**: Run `mvn test -Dtest=ETLContextTest,ContextManagerTest` and verify all tests pass
5. Commit as "feat: refactor ETLContext.getCurrentDate() to return LocalDate"

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Commit (MVP!)
3. Add User Story 2 → Test independently → Commit
4. Add User Story 3 → Test independently → Commit  
5. Add User Story 4 → Test independently → Commit
6. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 (ETLContext)
   - Developer B: User Story 2 (WorkflowResult)  
   - Developer C: User Story 3 (DateRangeGenerator)
   - Developer D: User Story 4 (Default config)
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing (TDD approach)
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
- Use `DateUtils.formatDate()` for any `String` date output needed for external interfaces (CLI, logging)