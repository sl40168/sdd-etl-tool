# Research: ETL Core Workflow

**Feature**: ETL Core Workflow  
**Date**: January 8, 2026  
**Purpose**: Research technology choices and patterns for implementing ETL core workflow in Java 8

## Research Topics

### 1. CLI Library for Java 8

**Decision**: Apache Commons CLI 1.4

**Rationale**: 
- Well-established, stable library compatible with Java 8
- Lightweight with minimal dependencies
- Provides robust argument parsing, validation, and help message generation
- Industry standard for Java CLI tools
- Active community and comprehensive documentation

**Alternatives Considered**:
- **Picocli**: More modern features but requires Java 8+ compatibility verification; larger footprint
- **Args4j**: Annotation-based but less actively maintained
- **Manual parsing**: Would require significant development effort and validation logic

---

### 2. INI File Parsing for Java 8

**Decision**: ini4j 0.5.4

**Rationale**:
- Mature, stable library specifically designed for INI file parsing
- Java 8 compatible
- Simple API for reading and writing INI files
- Supports sections, key-value pairs, and comments
- Lightweight with minimal dependencies

**Alternatives Considered**:
- **Apache Commons Configuration**: More feature-rich but heavier weight
- **Manual parsing**: Would require handling edge cases (comments, sections, escape characters)
- **System.getProperty()**: Not suitable for complex INI structure with sections

---

### 3. Logging Framework for Java 8

**Decision**: SLF4J 1.7.36 + Logback 1.2.12

**Rationale**:
- SLF4J is the de facto standard logging facade for Java
- Logback is the natural implementation for SLF4J, optimized for performance
- Both libraries are stable and Java 8 compatible
- Built-in support for file and console appenders
- Easy configuration via XML
- Extensive community support and documentation

**Alternatives Considered**:
- **Log4j 2**: Powerful but had security vulnerabilities (Log4Shell); more complex configuration
- **java.util.logging**: Built-in but limited functionality and configuration options
- **Log4j 1.x**: End-of-life and not recommended for new projects

---

### 4. Testing Framework for Java 8

**Decision**: JUnit 4.13.2 + Mockito 3.12.4

**Rationale**:
- JUnit 4 is the stable, widely-adopted testing framework for Java 8
- Mockito is the industry standard for mocking in Java
- Both libraries are mature, well-documented, and Java 8 compatible
- Excellent IDE support and integration
- TDD-friendly with clear assertion and mocking APIs

**Alternatives Considered**:
- **JUnit 5**: Modern but requires Java 8+ and may have compatibility issues in some IDEs
- **TestNG**: Feature-rich but steeper learning curve and less widely adopted
- **PowerMock**: More powerful for static method mocking but adds complexity

---

### 5. Concurrent Execution Detection Pattern

**Decision**: File-based lock mechanism using java.nio.file

**Rationale**:
- Simple, cross-platform solution for detecting running ETL processes
- Uses file locking APIs to attempt exclusive lock acquisition
- Fails fast with clear error message if another process is running
- No external dependencies required (uses Java NIO)
- Automatic cleanup on process termination (OS releases file lock)

**Implementation Pattern**:
```java
Path lockFile = Paths.get(System.getProperty("java.io.tmpdir"), "etl-tool.lock");
try (FileChannel channel = FileChannel.open(lockFile, 
        StandardOpenOption.CREATE, StandardOpenOption.WRITE);
     FileLock lock = channel.tryLock()) {
    if (lock == null) {
        throw new ETLException("Another ETL process is already running");
    }
    // Execute ETL process
} catch (OverlappingFileLockException e) {
    throw new ETLException("Another ETL process is already running");
}
```

**Alternatives Considered**:
- **Socket-based detection**: More complex, requires port management
- **Database-based detection**: Overkill for single-process CLI tool
- **PID file**: Cross-platform issues with PID detection and stale PID cleanup

---

### 6. Context Implementation Pattern

**Decision**: Immutable POJO with builder pattern

**Rationale**:
- Ensures thread-safety (important for potential future multi-threading)
- Clear, readable API for context creation and updates
- Immutability prevents accidental modification during subprocess execution
- Builder pattern provides flexibility for optional fields
- Java 8 compatible (no need for Java 8+ features like records)

**Implementation Pattern**:
```java
public final class DailyProcessContext {
    private final LocalDate date;
    private final SubprocessType currentSubprocess;
    private final int recordsExtracted;
    private final int recordsTransformed;
    private final int recordsLoaded;
    private final Configuration configuration;
    
    private DailyProcessContext(Builder builder) { ... }
    
    public static Builder builder() { return new Builder(); }
    
    // Getters only (no setters)
    
    public static class Builder {
        // Fluent setter methods
        public Builder date(LocalDate date) { ... }
        public Builder currentSubprocess(SubprocessType type) { ... }
        // ...
        public DailyProcessContext build() { ... }
    }
}
```

**Alternatives Considered**:
- **Mutable POJO**: Simpler but less thread-safe, risk of accidental modification
- **Map-based context**: Type-unsafe, less maintainable
- **Record (Java 14+)**: Not Java 8 compatible

---

### 7. Workflow Orchestration Pattern

**Decision**: Strategy pattern for subprocess execution with pipeline chaining

**Rationale**:
- Clear separation of concerns: orchestrator manages sequence, subprocesses implement execution
- Easy to extend with new subprocess types
- Follows Open/Closed principle (open for extension, closed for modification)
- Each subprocess has well-defined API (input: Context, output: Context)
- Facilitates testing (subprocesses can be mocked)

**Implementation Pattern**:
```java
public interface Subprocess {
    String getName();
    SubprocessType getType();
    DailyProcessContext execute(DailyProcessContext context) throws SubprocessException;
}

public class DailyETLWorkflow {
    private final List<Subprocess> subprocesses;
    
    public DailyETLWorkflow(List<Subprocess> subprocesses) {
        this.subprocesses = Collections.unmodifiableList(subprocesses);
    }
    
    public void execute(LocalDate date, Configuration config) {
        DailyProcessContext context = DailyProcessContext.builder()
            .date(date)
            .configuration(config)
            .build();
        
        for (Subprocess subprocess : subprocesses) {
            logStart(subprocess);
            context = subprocess.execute(context);
            logComplete(subprocess, context);
        }
    }
}
```

**Alternatives Considered**:
- **Chain of Responsibility**: More complex than needed for linear execution
- **Template Method**: Less flexible, requires inheritance
- **Observer/Event-driven**: Overkill for synchronous sequential execution

---

### 8. Date Range Iteration Pattern

**Decision**: Stream-based iteration with LocalDate

**Rationale**:
- Java 8's java.time.LocalDate provides robust date arithmetic
- Stream API enables functional iteration and potential parallelization in future
- Clear, readable code for date range iteration
- Handles leap years and month boundaries correctly

**Implementation Pattern**:
```java
public static void processDateRange(LocalDate from, LocalDate to, 
                                     ETLWorkflow workflow, Configuration config) {
    if (from.isAfter(to)) {
        throw new IllegalArgumentException("From date must be before or equal to to date");
    }
    
    from.datesUntil(to.plusDays(1))
         .forEach(date -> {
             try {
                 workflow.execute(date, config);
             } catch (ETLException e) {
                 throw new ETLException("Failed to process date: " + date, e);
             }
         });
}
```

**Alternatives Considered**:
- **Traditional for loop**: More verbose, less functional
- **Calendar-based iteration**: Deprecated API, error-prone
- **Recursive approach**: Risk of stack overflow for large date ranges

---

### 9. Parameter Validation Pattern

**Decision**: Fluent validation chain with custom exceptions

**Rationale**:
- Clear validation logic with descriptive error messages
- Early validation fails fast before ETL execution
- Custom exceptions provide granular error handling
- Fluent API makes validation rules readable

**Implementation Pattern**:
```java
public class ParameterValidator {
    public static void validateParameters(LocalDate from, LocalDate to, Path configPath) {
        if (from == null || to == null) {
            throw new ParameterValidationException("Date parameters are required");
        }
        
        if (from.isAfter(to)) {
            throw new ParameterValidationException(
                "From date must be before or equal to to date");
        }
        
        if (configPath == null) {
            throw new ParameterValidationException("Configuration file path is required");
        }
        
        if (!Files.exists(configPath)) {
            throw new ParameterValidationException(
                "Configuration file not found: " + configPath);
        }
        
        if (!configPath.toString().toLowerCase().endsWith(".ini")) {
            throw new ParameterValidationException(
                "Configuration file must be in INI format (.ini extension)");
        }
    }
}
```

**Alternatives Considered**:
- **Bean Validation (JSR 303)**: Overkill for CLI parameter validation
- **Apache Commons Validator**: Heavy dependency for simple validation rules
- **Inline validation**: Less readable, harder to maintain

---

## Summary

All technology choices align with:
- Java 8 platform requirement (Constitution Principle 1)
- Maven build tool usage (Constitution Principle 2)
- CLI interface exclusivity (Constitution Principle 3)
- INI configuration format (Constitution Principle 4)
- Component boundary clarity (Constitution Principle 5)
- Third-party library usage (Constitution Principle 8)

No NEEDS CLARIFICATION items remain. All technical decisions have been researched and documented.

## Next Steps

Proceed to Phase 1: Design & Contracts
1. Generate data-model.md from research decisions
2. Generate API contracts in /contracts/ directory
3. Generate quickstart.md for development setup
4. Update agent context
5. Re-evaluate Constitution Check post-design
