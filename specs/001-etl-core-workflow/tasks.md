# Tasks: ETL Core Workflow

**Input**: Design documents from `/specs/001-etl-core-workflow/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: This project uses TDD approach with >60% coverage requirement (per constitution). Test tasks are included for each component.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Java project**: `src/main/java/`, `src/test/java/` at repository root
- **Resources**: `src/main/resources/` for configuration files
- Paths shown below follow plan.md structure

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Create Maven project structure per implementation plan in pom.xml with Java 8 target
- [X] T002 [P] Create directory structure for packages: cli, context, workflow, subprocess, model, config, logging in src/main/java/com/sdd/etl/
- [X] T003 [P] Create directory structure for test packages in src/test/java/com/sdd/etl/
- [X] T004 [P] Create resources directory in src/main/resources/ for logback.xml configuration
- [X] T005 Add Apache Commons CLI dependency (version 1.4) to pom.xml
- [X] T006 [P] Add Apache Commons Configuration dependency (version 2.8.0) to pom.xml
- [X] T007 [P] Add commons-beanutils dependency (version 1.9.4) to pom.xml (required by Commons Configuration)
- [X] T008 [P] Add SLF4J API dependency (version 1.7.36) to pom.xml
- [X] T009 [P] Add Logback dependencies (logback-classic 1.2.11, logback-core 1.2.11) to pom.xml
- [X] T010 [P] Add JUnit 4 dependency (version 4.13.2) to pom.xml
- [X] T011 [P] Add Mockito dependency (version 4.5.1) to pom.xml
- [X] T012 Create example INI configuration file .etlconfig.ini.example in project root

**Checkpoint**: Project structure and dependencies ready

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T013 Create custom ETLException class in src/main/java/com/sdd/etl/ETLException.java with fields for subprocessType, date, and rootCause
- [X] T014 [P] Create SubprocessType enum in src/main/java/com/sdd/etl/context/SubprocessType.java with values: EXTRACT, TRANSFORM, LOAD, VALIDATE, CLEAN
- [X] T015 [P] Create ContextConstants class in src/main/java/com/sdd/etl/context/ContextConstants.java with all context key constants
- [X] T016 Create SubprocessResult POJO in src/main/java/com/sdd/etl/model/SubprocessResult.java with fields: success, dataCount, errorMessage, timestamp
- [X] T017 [P] Create DailyProcessResult POJO in src/main/java/com/sdd/etl/model/DailyProcessResult.java with fields: date, success, subprocess results, context
- [X] T018 [P] Create WorkflowResult POJO in src/main/java/com/sdd/etl/model/WorkflowResult.java with fields: success, processedDays, successfulDays, failedDays, dailyResults
- [X] T019 Create CommandLineArguments POJO in src/main/java/com/sdd/etl/model/CommandLineArguments.java with fields: fromDate, toDate, configPath, helpRequested
- [X] T020 Create ETConfiguration POJO in src/main/java/com/sdd/etl/config/ETConfiguration.java with nested config classes (SourceConfig, TargetConfig, TransformationConfig, ValidationConfig, LoggingConfig)
- [X] T021 [P] Unit test for ETLogger facade in src/test/java/com/sdd/etl/logging/ETLoggerTest.java
- [X] T022 [P] Unit test for StatusLogger POJO in src/test/java/com/sdd/etl/logging/StatusLoggerTest.java
- [X] T023 [P] Unit test for CommandLineValidator POJO in src/test/java/com/sdd/etl/cli/CommandLineValidatorTest.java
- [X] T024 [P] Unit test for ConfigurationLoader POJO in src/test/java/com/sdd/etl/config/ConfigurationLoaderTest.java
- [X] T025 [P] Unit test for SourceDataModel abstract class in src/test/java/com/sdd/etl/model/SourceDataModelTest.java
- [X] T026 [P] Unit test for TargetDataModel abstract class in src/test/java/com/sdd/etl/model/TargetDataModelTest.java
- [X] T027 [P] Unit test for SubprocessResult POJO in src/test/java/com/sdd/etl/model/SubprocessResultTest.java
- [X] T028 [P] Unit test for ETLContext POJO in src/test/java/com/sdd/etl/context/ETLContextTest.java
- [X] T029 [P] Unit test for ContextManager POJO in src/test/java/com/sdd/etl/context/ContextManagerTest.java
- [X] T030 Create ETLogger facade in src/main/java/com/sdd/etl/logging/ETLogger.java with methods for info, warn, error
- [X] T031 [P] Create StatusLogger in src/main/java/com/sdd/etl/logging/StatusLogger.java with methods for logging subprocess and day completion
- [X] T032 Configure Logback in src/main/resources/logback.xml with console and file appenders
- [X] T033 [P] Create CommandLineValidator in src/main/java/com/sdd/etl/cli/CommandLineValidator.java with validation methods for date format, date range, and file existence
- [X] T034 Create ConfigurationLoader in src/main/java/com/sdd/etl/config/ConfigurationLoader.java with INI file loading logic using Apache Commons Configuration
- [X] T035 Create SourceDataModel abstract class in src/main/java/com/sdd/etl/model/SourceDataModel.java with abstract methods: validate(), getPrimaryKey(), getSourceType()
- [X] T036 [P] Create TargetDataModel abstract class in src/main/java/com/sdd/etl/model/TargetDataModel.java with abstract methods: validate(), toTargetFormat(), getTargetType()
- [X] T037 Create SourceConfig nested class in ETConfiguration.java with fields: name, type, connectionString, primaryKeyField, extractQuery, dateField
- [X] T038 [P] Create TargetConfig nested class in ETConfiguration.java with fields: name, type, connectionString, batchSize
- [X] T039 Create TransformationConfig nested class in ETConfiguration.java with fields: name, sourceField, targetField, transformType
- [X] T040 [P] Create ValidationConfig nested class in ETConfiguration.java with fields: name, field, ruleType, ruleValue
- [X] T041 Create LoggingConfig nested class in ETConfiguration.java with fields: logFilePath, logLevel
- [X] T042 [P] Create SubprocessInterface interface in src/main/java/com/sdd/etl/subprocess/SubprocessInterface.java with methods: execute(), validateContext(), getType()
- [X] T043 Create ExtractSubprocess abstract class in src/main/java/com/sdd/etl/subprocess/ExtractSubprocess.java implementing SubprocessInterface (API only - no concrete implementation)
- [X] T044 [P] Create TransformSubprocess abstract class in src/main/java/com/sdd/etl/subprocess/TransformSubprocess.java implementing SubprocessInterface (API only - no concrete implementation)
- [X] T045 [P] Create LoadSubprocess abstract class in src/main/java/com/sdd/etl/subprocess/LoadSubprocess.java implementing SubprocessInterface (API only - no concrete implementation)
- [X] T046 [P] Create ValidateSubprocess abstract class in src/main/java/com/sdd/etl/subprocess/ValidateSubprocess.java implementing SubprocessInterface (API only - no concrete implementation)
- [X] T047 [P] Create CleanSubprocess abstract class in src/main/java/com/sdd/etl/subprocess/CleanSubprocess.java implementing SubprocessInterface (API only - no concrete implementation)
- [X] T048 Create ETLContext class in src/main/java/com/sdd/etl/context/ETLContext.java with HashMap-based storage, getters, setters, and generic get/set methods
- [X] T049 [P] Create ContextManager in src/main/java/com/sdd/etl/context/ContextManager.java with static methods: createContext(), validateContext(), snapshot(), logContextState()
- [X] T050 Create SubprocessExecutor in src/main/java/com/sdd/etl/workflow/SubprocessExecutor.java with executeAll() method for strict subprocess sequencing
- [X] T051 [P] Create DailyETLWorkflow in src/main/java/com/sdd/etl/workflow/DailyETLWorkflow.java with execute() method orchestrating single day's subprocesses
- [X] T052 Create WorkflowEngine in src/main/java/com/sdd/etl/workflow/WorkflowEngine.java with execute() method for multi-day iteration and day orchestration
- [X] T053 [P] Create ConcurrentExecutionDetector in src/main/java/com/sdd/etl/util/ConcurrentExecutionDetector.java with file lock mechanism using java.nio.channels.FileLock
- [X] T054 [P] Create DateRangeGenerator in src/main/java/com/sdd/etl/util/DateRangeGenerator.java with method to generate list of dates between from and to dates using java.time.LocalDate
- [X] T055 Create ETLCommandLine main class in src/main/java/com/sdd/etl/cli/ETLCommandLine.java as CLI entry point with Apache Commons CLI integration

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Command Line Execution (Priority: P1) 🎯 MVP

**Goal**: Enable data engineers to start the ETL process from command line by providing date range and configuration file, so that they can extract, transform, and load data across multiple days without manual intervention.

**Independent Test**: Run the tool with valid parameters (--from, --to, --config) and verify the process starts correctly. Run with invalid parameters and verify clear error messages are displayed.

### Tests for User Story 1

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T056 [P] [US1] Unit test for CommandLineArguments POJO in src/test/java/com/sdd/etl/model/CommandLineArgumentsTest.java
- [X] T057 [P] [US1] Unit test for date format validation in CommandLineValidator in src/test/java/com/sdd/etl/cli/CommandLineValidatorTest.java
- [X] T058 [P] [US1] Unit test for date range validation in CommandLineValidator in src/test/java/com/sdd/etl/cli/CommandLineValidatorTest.java
- [X] T059 [P] [US1] Unit test for configuration file existence validation in CommandLineValidator in src/test/java/com/sdd/etl/cli/CommandLineValidatorTest.java
- [X] T060 [P] [US1] Unit test for help command argument parsing in ETLCommandLine in src/test/java/com/sdd/etl/cli/ETLCommandLineTest.java
- [X] T061 [P] [US1] Unit test for required parameter parsing (--from, --to, --config) in ETLCommandLine in src/test/java/com/sdd/etl/cli/ETLCommandLineTest.java
- [X] T062 [P] [US1] Unit test for invalid parameter error handling in ETLCommandLine in src/test/java/com/sdd/etl/cli/ETLCommandLineTest.java
- [X] T063 [P] [US1] Unit test for concurrent execution detection in ConcurrentExecutionDetector in src/test/java/com/sdd/etl/util/ConcurrentExecutionDetectorTest.java

### Implementation for User Story 1

- [X] T064 [US1] Implement CommandLineValidator.validateDateFormat() method in src/main/java/com/sdd/etl/cli/CommandLineValidator.java with YYYYMMDD format validation using java.time.LocalDate
- [X] T065 [US1] Implement CommandLineValidator.validateDateRange() method in src/main/java/com/sdd/etl/cli/CommandLineValidator.java with from ≤ to check
- [X] T066 [US1] Implement CommandLineValidator.validateConfigFileExists() method in src/main/java/com/sdd/etl/cli/CommandLineValidator.java with file existence and readability check
- [X] T067 [US1] Implement CommandLineValidator.validateAll() method in src/main/java/com/sdd/etl/cli/CommandLineValidator.java that calls all validation methods and aggregates errors
- [X] T068 [US1] Implement ConcurrentExecutionDetector.acquireLock() method in src/main/java/com/sdd/etl/util/ConcurrentExecutionDetector.java using FileLock with .etl.lock file
- [X] T069 [US1] Implement ConcurrentExecutionDetector.releaseLock() method in src/main/java/com/sdd/etl/util/ConcurrentExecutionDetector.java with proper cleanup in finally block
- [X] T070 [US1] Implement DateRangeGenerator.generate() method in src/main/java/com/sdd/etl/util/DateRangeGenerator.java using java.time.LocalDate to iterate from from date to to date inclusive
- [X] T071 [US1] Implement ConfigurationLoader.load() method in src/main/java/com/sdd/etl/config/ConfigurationLoader.java using Apache Commons Configuration to parse INI file
- [X] T072 [US1] Implement ETLCommandLine.main() method in src/main/java/com/sdd/etl/cli/ETLCommandLine.java with Apache Commons CLI setup for --from, --to, --config, --help options
- [X] T073 [US1] Implement ETLCommandLine.parseArguments() method in src/main/java/com/sdd/etl/cli/ETLCommandLine.java to parse command-line arguments and create CommandLineArguments object
- [X] T074 [US1] Implement ETLCommandLine.displayHelp() method in src/main/java/com/sdd/etl/cli/ETLCommandLine.java to display usage information with examples and exit codes
- [X] T075 [US1] Implement ETLCommandLine.validateAndExecute() method in src/main/java/com/sdd/etl/cli/ETLCommandLine.java with input validation, config loading, and concurrent execution detection
- [X] T076 [US1] Add error handling for invalid date format with clear error message in ETLCommandLine in src/main/java/com/sdd/etl/cli/ETLCommandLine.java
- [X] T077 [US1] Add error handling for invalid date range (from > to) with clear error message in ETLCommandLine in src/main/java/com/sdd/etl/cli/ETLCommandLine.java
- [X] T078 [US1] Add error handling for missing configuration file with clear error message in ETLCommandLine in src/main/java/com/sdd/etl/cli/ETLCommandLine.java
- [X] T079 [US1] Add error handling for concurrent execution detection with clear error message and exit code 2 in ETLCommandLine in src/main/java/com/sdd/etl/cli/ETLCommandLine.java
- [X] T080 [US1] Add startup banner logging with tool version, from date, to date, and config path in ETLCommandLine in src/main/java/com/sdd/etl/cli/ETLCommandLine.java

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Daily Process Orchestration (Priority: P1)

**Goal**: Enable the ETL process to execute day by day in sequence, so that each day's data is processed independently and completely before moving to the next day.

**Independent Test**: Run a multi-day ETL job (e.g., 3 days) and verify that each day's process completes before the next begins, and that a failure on one day stops subsequent days.

### Tests for User Story 2

- [X] T081 [P] [US2] Unit test for DailyProcessResult POJO in src/test/java/com/sdd/etl/model/DailyProcessResultTest.java
- [X] T082 [P] [US2] Unit test for WorkflowResult POJO in src/test/java/com/sdd/etl/model/WorkflowResultTest.java
- [X] T083 [P] [US2] Unit test for DateRangeGenerator.generate() method in src/test/java/com/sdd/etl/util/DateRangeGeneratorTest.java
- [X] T084 [P] [US2] Unit test for WorkflowEngine.generateDateRange() method in src/test/java/com/sdd/etl/workflow/WorkflowEngineTest.java
- [X] T085 [P] [US2] Unit test for WorkflowEngine.executeDay() method in src/test/java/com/sdd/etl/workflow/WorkflowEngineTest.java
- [X] T086 [P] [US2] Unit test for WorkflowEngine.execute() method with multi-day range in src/test/java/com/sdd/etl/workflow/WorkflowEngineTest.java
- [X] T087 [P] [US2] Unit test for day failure stopping subsequent days in WorkflowEngine in src/test/java/com/sdd/etl/workflow/WorkflowEngineTest.java
- [X] T088 [P] [US2] Integration test for single-day execution in src/test/java/com/sdd/etl/workflow/WorkflowEngineIntegrationTest.java
- [X] T089 [P] [US2] Integration test for multi-day sequential execution in src/test/java/com/sdd/etl/workflow/WorkflowEngineIntegrationTest.java

### Implementation for User Story 2

- [X] T090 [P] [US2] Implement WorkflowEngine.generateDateRange() method in src/main/java/com/sdd/etl/workflow/WorkflowEngine.java to generate list of dates from from to to dates
- [X] T091 [US2] Implement WorkflowEngine.executeDay() method in src/main/java/com/sdd/etl/workflow/WorkflowEngine.java to execute daily workflow for a single date
- [X] T092 [US2] Implement WorkflowEngine.aggregateResults() method in src/main/java/com/sdd/etl/workflow/WorkflowEngine.java to aggregate DailyProcessResult into WorkflowResult with summary statistics
- [X] T093 [US2] Implement WorkflowEngine.logSummary() method in src/main/java/com/sdd/etl/workflow/WorkflowEngine.java to log final workflow summary with total days, successful days, failed days, and duration
- [X] T094 [US2] Implement WorkflowEngine.execute() method in src/main/java/com/sdd/etl/workflow/WorkflowEngine.java to iterate through date range and execute daily workflows for each day
- [X] T095 [US2] Add day failure stop logic in WorkflowEngine.execute() method in src/main/java/com/sdd/etl/workflow/WorkflowEngine.java to break out of loop if a day fails
- [X] T096 [US2] Add logging for each day start in WorkflowEngine.executeDay() method in src/main/java/com/sdd/etl/workflow/WorkflowEngine.java
- [X] T097 [US2] Add logging for day completion in WorkflowEngine.executeDay() method in src/main/java/com/sdd/etl/workflow/WorkflowEngine.java
- [X] T098 [US2] Add logging for day failure in WorkflowEngine.execute() method in src/main/java/com/sdd/etl/workflow/WorkflowEngine.java with error details

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - Subprocess Sequential Execution (Priority: P1)

**Goal**: Enable each day's process to execute extract, transform, load, validate, and clean subprocesses in strict sequence, so that data flows through the pipeline correctly and each step can depend on the previous step's completion.

**Independent Test**: Implement all 5 subprocess APIs and verify they execute in order for a single day. Verify that a subprocess failure prevents subsequent subprocesses from executing.

### Tests for User Story 3

- [X] T099 [P] [US3] Unit test for SubprocessResult POJO in src/test/java/com/sdd/etl/model/SubprocessResultTest.java
- [X] T100 [P] [US3] Unit test for SubprocessExecutor.execute() method with mock subprocess in src/test/java/com/sdd/etl/workflow/SubprocessExecutorTest.java
- [X] T101 [P] [US3] Unit test for SubprocessExecutor.executeAll() method in src/test/java/com/sdd/etl/workflow/SubprocessExecutorTest.java
- [X] T102 [P] [US3] Unit test for subprocess sequence order in SubprocessExecutor in src/test/java/com/sdd/etl/workflow/SubprocessExecutorTest.java
- [X] T103 [P] [US3] Unit test for extract failure stopping transform in SubprocessExecutor in src/test/java/com/sdd/etl/workflow/SubprocessExecutorTest.java
- [X] T104 [P] [US3] Unit test for validate failure stopping clean in SubprocessExecutor in src/test/java/com/sdd/etl/workflow/SubprocessExecutorTest.java
- [X] T105 [P] [US3] Unit test for DailyETLWorkflow.execute() method in src/test/java/com/sdd/etl/workflow/DailyETLWorkflowTest.java
- [X] T106 [P] [US3] Integration test for complete subprocess sequence in src/test/java/com/sdd/etl/workflow/DailyETLWorkflowIntegrationTest.java

### Implementation for User Story 3

- [X] T107 [US3] Implement SubprocessExecutor.execute() method in src/main/java/com/sdd/etl/workflow/SubprocessExecutor.java to execute a single subprocess with context validation
- [X] T108 [US3] Implement SubprocessExecutor.validateContextBeforeExecution() method in src/main/java/com/sdd/etl/workflow/SubprocessExecutor.java to validate context state before subprocess execution
- [X] T109 [US3] Implement SubprocessExecutor.validatePreviousSubprocessCompletion() method in src/main/java/com/sdd/etl/workflow/SubprocessExecutor.java to verify previous subprocess completed successfully
- [X] T110 [US3] Implement SubprocessExecutor.executeAll() method in src/main/java/com/sdd/etl/workflow/SubprocessExecutor.java to execute all subprocesses in strict sequence: EXTRACT → TRANSFORM → LOAD → VALIDATE → CLEAN
- [X] T111 [US3] Add subprocess dependency checks in SubprocessExecutor.executeAll() method in src/main/java/com/sdd/etl/workflow/SubprocessExecutor.java to only trigger next subprocess if current succeeds
- [X] T112 [US3] Add exception throwing for subprocess failures in SubprocessExecutor.executeAll() method in src/main/java/com/sdd/etl/workflow/SubprocessExecutor.java with ETLException containing subprocess details
- [X] T113 [US3] Implement DailyETLWorkflow.validateInitialState() method in src/main/java/com/sdd/etl/workflow/DailyETLWorkflow.java to validate context before executing day's workflow
- [X] T114 [US3] Implement DailyETLWorkflow.execute() method in src/main/java/com/sdd/etl/workflow/DailyETLWorkflow.java to orchestrate all subprocesses for a single day using SubprocessExecutor
- [X] T115 [US3] Implement DailyETLWorkflow.logCompletion() method in src/main/java/com/sdd/etl/workflow/DailyETLWorkflow.java to log subprocess results and day completion status
- [X] T116 [US3] Add error handling in DailyETLWorkflow.execute() method in src/main/java/com/sdd/etl/workflow/DailyETLWorkflow.java to catch ETLException and propagate day failure
- [X] T117 [US3] Add subprocess result aggregation in DailyETLWorkflow.execute() method in src/main/java/com/sdd/etl/workflow/DailyETLWorkflow.java to create DailyProcessResult with all subprocess results

**Checkpoint**: All user stories should now be independently functional

---

## Phase 6: User Story 4 - Multi-Source Extraction (Priority: P2)

**Goal**: Enable extracting data from multiple sources in a single ETL process, so that data from various systems can be consolidated into a unified target.

**Independent Test**: Configure multiple data sources (e.g., 3 sources) and verify all data is extracted. Verify that if one source fails, the extract subprocess fails and does not proceed to transform.

### Tests for User Story 4

- [X] T118 [P] [US4] Unit test for SourceConfig nested class in src/test/java/com/sdd/etl/config/ETConfigurationTest.java
- [X] T119 [P] [US4] Unit test for ConfigurationLoader parsing multiple sources in src/test/java/com/sdd/etl/config/ConfigurationLoaderTest.java
- [X] T120 [P] [US4] Unit test for ExtractSubprocess.validateContext() in src/test/java/com/sdd/etl/subprocess/ExtractSubprocessTest.java
- [X] T121 [P] [US4] Unit test for ExtractSubprocess API interface in src/test/java/com/sdd/etl/subprocess/ExtractSubprocessTest.java
- [X] T122 [P] [US4] Integration test for multi-source extraction in src/test/java/com/sdd/etl/subprocess/ExtractSubprocessIntegrationTest.java (mock implementation for API-only subprocess)

### Implementation for User Story 4

- [X] T123 [US4] Implement SourceConfig nested class in ETConfiguration.java with validation for required fields (name, type, connectionString, primaryKeyField)
- [X] T124 [US4] Implement ConfigurationLoader parsing logic for [sources] section and multiple [sourceN] sections in ConfigurationLoader.load() method in src/main/java/com/sdd/etl/config/ConfigurationLoader.java
- [X] T125 [US4] Add sources list to ETConfiguration class in src/main/java/com/sdd/etl/config/ETConfiguration.java
- [X] T126 [US4] Implement ExtractSubprocess.validateContext() method in src/main/java/com/sdd/etl/subprocess/ExtractSubprocess.java to check config is not null and sources list is not empty
- [X] T127 [US4] Implement ExtractSubprocess.getType() method in src/main/java/com/sdd/etl/subprocess/ExtractSubprocess.java to return SubprocessType.EXTRACT
- [X] T128 [US4] Define ExtractSubprocess.execute() abstract method signature in src/main/java/com/sdd/etl/subprocess/ExtractSubprocess.java with JavaDoc specifying context contract (extractedData, extractedDataCount, currentSubprocess update)

**Checkpoint**: At this point, User Stories 1-4 should all work independently

---

## Phase 7: User Story 5 - Multi-Target Loading (Priority: P2)

**Goal**: Enable loading data to multiple targets in a single ETL process, so that processed data can be written to various destination systems simultaneously.

**Independent Test**: Configure multiple data targets (e.g., 3 targets) and verify all data is loaded. Verify that if one target fails, the load subprocess fails and does not proceed to validation.

### Tests for User Story 5

- [X] T129 [P] [US5] Unit test for TargetConfig nested class in src/test/java/com/sdd/etl/config/ETConfigurationTest.java
- [X] T130 [P] [US5] Unit test for ConfigurationLoader parsing multiple targets in src/test/java/com/sdd/etl/config/ConfigurationLoaderTest.java
- [X] T131 [P] [US5] Unit test for LoadSubprocess.validateContext() in src/test/java/com/sdd/etl/subprocess/LoadSubprocessTest.java
- [X] T132 [P] [US5] Unit test for LoadSubprocess API interface in src/test/java/com/sdd/etl/subprocess/LoadSubprocessTest.java
- [X] T133 [P] [US5] Integration test for multi-target loading in src/test/java/com/sdd/etl/subprocess/LoadSubprocessIntegrationTest.java (mock implementation for API-only subprocess)

### Implementation for User Story 5

- [X] T134 [US5] Implement TargetConfig nested class in ETConfiguration.java with validation for required fields (name, type, connectionString, batchSize)
- [X] T135 [US5] Implement ConfigurationLoader parsing logic for [targets] section and multiple [targetN] sections in ConfigurationLoader.load() method in src/main/java/com/sdd/etl/config/ConfigurationLoader.java
- [X] T136 [US5] Add targets list to ETConfiguration class in src/main/java/com/sdd/etl/config/ETConfiguration.java
- [X] T137 [US5] Implement LoadSubprocess.validateContext() method in src/main/java/com/sdd/etl/subprocess/LoadSubprocess.java to check config is not null and targets list is not empty
- [X] T138 [US5] Implement LoadSubprocess.getType() method in src/main/java/com/sdd/etl/subprocess/LoadSubprocess.java to return SubprocessType.LOAD
- [X] T139 [US5] Define LoadSubprocess.execute() abstract method signature in src/main/java/com/sdd/etl/subprocess/LoadSubprocess.java with JavaDoc specifying context contract (loadedDataCount, currentSubprocess update)

**Checkpoint**: At this point, User Stories 1-5 should all work independently

---

## Phase 8: User Story 6 - Status Logging (Priority: P2)

**Goal**: Enable the ETL process to log status updates to both file and console at each subprocess completion, so that progress can be tracked and issues can be troubleshot.

**Independent Test**: Run a simple ETL process and verify log messages appear on console and are written to a log file. Verify that subprocess completion, day completion, and errors are logged correctly.

### Tests for User Story 6

> **NOTE: ETLogger facade tests completed in Phase 2 (T021). This phase focuses on StatusLogger functionality and integration.**

- [X] T140 [P] [US6] Unit test for StatusLogger.logSubprocessCompletion() in src/test/java/com/sdd/etl/logging/StatusLoggerTest.java
- [X] T141 [P] [US6] Unit test for StatusLogger.logDayCompletion() in src/test/java/com/sdd/etl/logging/StatusLoggerTest.java
- [X] T142 [P] [US6] Unit test for StatusLogger.logError() in src/test/java/com/sdd/etl/logging/StatusLoggerTest.java
- [X] T143 [P] [US6] Integration test for logging to console in src/test/java/com/sdd/etl/logging/StatusLoggerIntegrationTest.java
- [X] T144 [P] [US6] Integration test for logging to file in src/test/java/com/sdd/etl/logging/StatusLoggerIntegrationTest.java

### Implementation for User Story 6

- [X] T145 [US6] Implement StatusLogger.logSubprocessCompletion() method in src/main/java/com/sdd/etl/logging/StatusLogger.java to log subprocess success with data count
- [X] T146 [US6] Implement StatusLogger.logDayCompletion() method in src/main/java/com/sdd/etl/logging/StatusLogger.java to log day completion status with all subprocess results
- [X] T147 [US6] Implement StatusLogger.logError() method in src/main/java/com/sdd/etl/logging/StatusLogger.java to log subprocess or day failure with error details
- [X] T148 [US6] Configure Logback in src/main/resources/logback.xml with console appender for INFO level
- [X] T149 [US6] Configure Logback in src/main/resources/logback.xml with file appender writing to configurable log path
- [X] T150 [US6] Configure Logback in src/main/resources/logback.xml with pattern layout including timestamp, level, logger, and message
- [X] T151 [US6] Integrate StatusLogger into SubprocessExecutor to log subprocess completion in src/main/java/com/sdd/etl/workflow/SubprocessExecutor.java
- [X] T152 [US6] Integrate StatusLogger into DailyETLWorkflow to log day completion in src/main/java/com/sdd/etl/workflow/DailyETLWorkflow.java
- [X] T153 [US6] Integrate StatusLogger into WorkflowEngine to log workflow summary in src/main/java/com/sdd/etl/workflow/WorkflowEngine.java

**Checkpoint**: At this point, User Stories 1-6 should all work independently

---

## Phase 9: User Story 7 - Context-Based Data Transfer (Priority: P1)

**Goal**: Enable all sub-components to use context to transfer data between each other, so that system has a standardized, maintainable data flow architecture.

**Independent Test**: Verify that subprocess components receive data from context rather than through direct method calls or parameter passing. Verify data flows correctly: extract writes to context, transform reads from and writes to context, load reads from and writes to context.

### Tests for User Story 7

> **NOTE: Basic ETLContext POJO tests completed in Phase 2 (T028). This phase focuses on advanced context functionality and integration.**

- [X] T154 [P] [US7] Unit test for ETLContext getters and setters in src/test/java/com/sdd/etl/context/ETLContextTest.java
- [X] T155 [P] [US7] Unit test for ETLContext generic get/set methods in src/test/java/com/sdd/etl/context/ETLContextTest.java
- [X] T156 [P] [US7] Unit test for ETLContext.getAll() method in src/test/java/com/sdd/etl/context/ETLContextTest.java
- [X] T157 [P] [US7] Unit test for ETLContext.clear() method in src/test/java/com/sdd/etl/context/ETLContextTest.java
- [X] T158 [P] [US7] Unit test for ContextManager.createContext() method in src/test/java/com/sdd/etl/context/ContextManagerTest.java
- [X] T159 [P] [US7] Unit test for ContextManager.validateContext() method in src/test/java/com/sdd/etl/context/ContextManagerTest.java
- [X] T160 [P] [US7] Unit test for ContextManager.snapshot() method in src/test/java/com/sdd/etl/context/ContextManagerTest.java
- [X] T161 [P] [US7] Unit test for context state transitions in src/test/java/com/sdd/etl/context/ETLContextStateTest.java
- [X] T162 [P] [US7] Integration test for extract writing to context in src/test/java/com/sdd/etl/context/ContextDataFlowIntegrationTest.java
- [X] T163 [P] [US7] Integration test for transform reading from and writing to context in src/test/java/com/sdd/etl/context/ContextDataFlowIntegrationTest.java
- [X] T164 [P] [US7] Integration test for load reading from and writing to context in src/test/java/com/sdd/etl/context/ContextDataFlowIntegrationTest.java

### Implementation for User Story 7

- [X] T165 [US7] Implement ETLContext.getCurrentDate() getter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T166 [US7] Implement ETLContext.setCurrentDate() setter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T167 [US7] Implement ETLContext.getCurrentSubprocess() getter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T168 [US7] Implement ETLContext.setCurrentSubprocess() setter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T169 [US7] Implement ETLContext.getConfig() getter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T170 [US7] Implement ETLContext.setConfig() setter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T171 [US7] Implement ETLContext.getExtractedDataCount() getter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T172 [US7] Implement ETLContext.setExtractedDataCount() setter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T173 [US7] Implement ETLContext.getExtractedData() getter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T174 [US7] Implement ETLContext.setExtractedData() setter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T175 [US7] Implement ETLContext.getTransformedDataCount() getter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T176 [US7] Implement ETLContext.setTransformedDataCount() setter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T177 [US7] Implement ETLContext.getTransformedData() getter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T178 [US7] Implement ETLContext.setTransformedData() setter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T179 [US7] Implement ETLContext.getLoadedDataCount() getter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T180 [US7] Implement ETLContext.setLoadedDataCount() setter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T181 [US7] Implement ETLContext.isValidationPassed() getter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T182 [US7] Implement ETLContext.setValidationPassed() setter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T183 [US7] Implement ETLContext.getValidationErrors() getter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T184 [US7] Implement ETLContext.setValidationErrors() setter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T185 [US7] Implement ETLContext.isCleanupPerformed() getter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T186 [US7] Implement ETLContext.setCleanupPerformed() setter in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T187 [US7] Implement ETLContext generic get() method with type safety in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T188 [US7] Implement ETLContext generic set() method in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T189 [US7] Implement ETLContext.getAll() method to return copy of internal HashMap in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T190 [US7] Implement ETLContext.clear() method to reset context state in src/main/java/com/sdd/etl/context/ETLContext.java
- [X] T191 [US7] Implement ContextManager.createContext() method in src/main/java/com/sdd/etl/context/ContextManager.java to create ETLContext with date and config
- [X] T192 [US7] Implement ContextManager.validateContext() method in src/main/java/com/sdd/etl/context/ContextManager.java to validate context before subprocess execution
- [X] T193 [US7] Implement ContextManager.snapshot() method in src/main/java/com/sdd/etl/context/ContextManager.java to create immutable copy of context
- [X] T194 [US7] Implement ContextManager.logContextState() method in src/main/java/com/sdd/etl/context/ContextManager.java to log context for troubleshooting
- [X] T195 [US7] Add context state validation rules in ContextManager.validateContext() method in src/main/java/com/sdd/etl/context/ContextManager.java (e.g., extractedDataCount ≥ 0, data null before process, non-null after process)

**Checkpoint**: All user stories should now be independently functional

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [X] T196 [P] Add comprehensive JavaDoc to all public classes and methods
- [X] T197 [P] Update example configuration file .etlconfig.ini.example with detailed comments for all sections
- [X] T198 [P] Run full test suite with mvn test and ensure >60% coverage
- [X] T199 [P] Run mvn clean install to verify full build passes
- [X] T200 Code cleanup: Remove unused imports and dead code
- [X] T201 Refactor: Extract common validation logic into utility class if code duplication exists
- [X] T202 Add error handling for configuration file parsing errors in ConfigurationLoader in src/main/java/com/sdd/etl/config/ConfigurationLoader.java
- [X] T203 Add error handling for context validation failures in ContextManager in src/main/java/com/sdd/etl/context/ContextManager.java
- [X] T204 Performance: Add batch processing hints for large data volumes (1M+ records per day) in ETConfiguration and subprocess APIs
- [X] T205 Security: Add input sanitization for configuration file paths in ETLCommandLine in src/main/java/com/sdd/etl/cli/ETLCommandLine.java
- [X] T206 Run quickstart.md validation scenarios to ensure all examples work correctly
- [X] T207 Verify all exit codes are correctly implemented (0=success, 1=input validation, 2=concurrent, 3=process error, 4=config, 5=unexpected)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-9)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order: P1 (US1, US2, US3, US7) → P2 (US4, US5, US6)
- **Polish (Phase 10)**: Depends on all desired user stories being complete

**IMPORTANT**: Foundational phase (T013-T055) includes unit tests (T021-T029) that MUST be written BEFORE implementing classes, ensuring TDD compliance per Constitution Principle #6.

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 3 (P1)**: Can start after Foundational (Phase 2) - Integrates with US2 (workflow orchestration) but independently testable
- **User Story 4 (P2)**: Can start after Foundational (Phase 2) - No dependencies on other stories (API-only)
- **User Story 5 (P2)**: Can start after Foundational (Phase 2) - No dependencies on other stories (API-only)
- **User Story 6 (P2)**: Can start after Foundational (Phase 2) - Integrates with US2, US3 but independently testable
- **User Story 7 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories (context implementation)

### Within Each User Story

- Tests MUST be written and FAIL before implementation (TDD - Constitution Principle #6)
- Models/POJOs before services/workflow
- Services/workflow before integration
- Core implementation before integration
- Story complete before moving to next priority

**TDD Compliance**:
- Foundational phase (Phase 2) includes unit tests for all foundation classes BEFORE implementation
- User story phases (Phases 3-9) include unit tests BEFORE implementation tasks
- All test tasks appear at the start of each phase with [P] marker
- Tests must FAIL before corresponding implementation is written

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Models within a story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members
- Polish tasks marked [P] can run in parallel

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together:
Task: "Unit test for CommandLineArguments POJO in src/test/java/com/sdd/etl/model/CommandLineArgumentsTest.java"
Task: "Unit test for date format validation in CommandLineValidator in src/test/java/com/sdd/etl/cli/CommandLineValidatorTest.java"
Task: "Unit test for date range validation in CommandLineValidator in src/test/java/com/sdd/etl/cli/CommandLineValidatorTest.java"
Task: "Unit test for configuration file existence validation in CommandLineValidator in src/test/java/com/sdd/etl/cli/CommandLineValidatorTest.java"

# Launch implementation tasks in parallel:
Task: "Implement CommandLineValidator.validateDateFormat() method in src/main/java/com/sdd/etl/cli/CommandLineValidator.java"
Task: "Implement CommandLineValidator.validateDateRange() method in src/main/java/com/sdd/etl/cli/CommandLineValidator.java"
Task: "Implement CommandLineValidator.validateConfigFileExists() method in src/main/java/com/sdd/etl/cli/CommandLineValidator.java"
Task: "Implement ConcurrentExecutionDetector.acquireLock() method in src/main/java/com/sdd/etl/util/ConcurrentExecutionDetector.java"
```

---

## Implementation Strategy

### MVP First (P1 Stories: US1, US2, US3, US7 Only)

1. Complete Phase 1: Setup (T001-T012)
2. Complete Phase 2: Foundational (T013-T055) - CRITICAL - blocks all stories
3. Complete Phase 3: User Story 1 (T056-T080)
4. Complete Phase 4: User Story 2 (T081-T098)
5. Complete Phase 5: User Story 3 (T099-T117)
6. Complete Phase 9: User Story 7 (T154-T195) [Context-Based Data Transfer - P1]
7. **STOP and VALIDATE**: Test all P1 stories independently
8. Deploy/demo if ready
9. Complete Phase 6: User Story 4 (T118-T128) [Multi-Source Extraction - P2]
10. Complete Phase 7: User Story 5 (T129-T139) [Multi-Target Loading - P2]
11. Complete Phase 8: User Story 6 (T140-T153) [Status Logging - P2]
12. Complete Phase 10: Polish (T196-T207)

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 (CLI) → Test independently → Deploy/Demo (MVP core!)
3. Add User Story 2 (Daily Orchestration) → Test independently → Deploy/Demo
4. Add User Story 3 (Subprocess Sequencing) → Test independently → Deploy/Demo
5. Add User Story 7 (Context-Based Data Transfer) → Test independently → Deploy/Demo
6. Add User Story 4 (Multi-Source) → Test independently → Deploy/Demo
7. Add User Story 5 (Multi-Target) → Test independently → Deploy/Demo
8. Add User Story 6 (Status Logging) → Test independently → Deploy/Demo
9. Complete Polish phase → Final production-ready delivery

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 (CLI)
   - Developer B: User Story 2 (Daily Orchestration)
   - Developer C: User Story 3 (Subprocess Sequencing)
   - Developer D: User Story 7 (Context)
3. After P1 stories complete:
   - Developer A: User Story 4 (Multi-Source)
   - Developer B: User Story 5 (Multi-Target)
   - Developer C: User Story 6 (Status Logging)
4. Stories complete and integrate independently

---

## Summary

- **Total Task Count**: 207 tasks
- **Task Count by User Story**:
  - Setup: 12 tasks (T001-T012)
  - Foundational: 43 tasks (T013-T055)
  - US1 (Command Line Execution): 25 tasks (T056-T080) - 8 tests + 18 implementations
  - US2 (Daily Process Orchestration): 18 tasks (T081-T098) - 8 tests + 10 implementations
  - US3 (Subprocess Sequential Execution): 19 tasks (T099-T117) - 8 tests + 11 implementations
  - US4 (Multi-Source Extraction): 11 tasks (T118-T128) - 5 tests + 6 implementations
  - US5 (Multi-Target Loading): 11 tasks (T129-T139) - 5 tests + 6 implementations
  - US6 (Status Logging): 14 tasks (T140-T153) - 5 tests + 9 implementations
  - US7 (Context-Based Data Transfer): 42 tasks (T154-T195) - 11 tests + 31 implementations
  - Polish: 12 tasks (T196-T207)

- **Parallel Opportunities Identified**:
  - Setup phase: 8 parallelizable tasks
  - Foundational phase: 20 parallelizable tasks
  - User Story phases: Multiple parallelizable tasks per story (tests, models, independent implementations)
  - Polish phase: 4 parallelizable tasks

- **Independent Test Criteria for Each Story**:
  - US1: Run tool with valid/invalid parameters, verify behavior
  - US2: Run multi-day job, verify sequential execution
  - US3: Run single day, verify subprocess order
  - US4: Configure multiple sources, verify extraction
  - US5: Configure multiple targets, verify loading
  - US6: Run ETL, verify console and file logging
  - US7: Verify data flows through context only

- **Suggested MVP Scope** (P1 Stories):
  - User Story 1 (CLI) - Essential for all functionality
  - User Story 2 (Daily Orchestration) - Core workflow
  - User Story 3 (Subprocess Sequencing) - Pipeline integrity
  - User Story 7 (Context-Based Data Transfer) - Critical architecture requirement

- **Format Validation**: ✅ ALL tasks follow the checklist format (checkbox, ID, [P] marker where applicable, [Story] label, file paths)

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- TDD approach: Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- API-only subprocesses (US4, US5) require only interface definitions, no concrete implementations
- This phase delivers: CLI interface, Context implementation, Daily ETL Workflow concrete implementation
- Subprocess concrete implementations, Source Data Model, and Target Data Model have API definitions only (per plan.md scope)
