# Quickstart Guide: ETL Core Workflow

**Feature**: ETL Core Workflow  
**Date**: January 8, 2026  
**Purpose**: Quickstart guide for developers implementing the ETL core workflow

## Overview

This guide provides step-by-step instructions for setting up and developing the ETL core workflow feature.

## Prerequisites

### Required Software

- **Java 8** (JDK 1.8.0_xxx)
- **Maven 3.6+** (with Maven wrapper included)
- **Git** (for version control)
- **IDE**: IntelliJ IDEA or Eclipse (recommended)

### System Requirements

- Java 8 compatible JVM
- At least 2GB RAM available for development
- 1GB free disk space

## Project Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd sdd-etl-tool
```

### 2. Switch to Feature Branch

```bash
git checkout 001-etl-core-workflow
```

### 3. Build Project

```bash
# Using Maven wrapper
./mvnw clean install

# Or using system Maven
mvn clean install
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX s
```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/sdd/etl/
│   │       ├── cli/                   # CLI interface implementation
│   │       ├── context/                # Context implementation
│   │       ├── workflow/               # Daily ETL workflow implementation
│   │       ├── api/                    # API definitions only
│   │       ├── logging/                # Logging implementation
│   │       └── exception/              # Exception definitions
│   └── resources/
│       └── logback.xml
└── test/
    └── java/
        └── com/sdd/etl/
            ├── cli/
            ├── context/
            ├── workflow/
            ├── logging/
            └── integration/
```

## Development Workflow

### Step 1: Add Dependencies

Update `pom.xml` with required dependencies:

```xml
<dependencies>
    <!-- Apache Commons CLI -->
    <dependency>
        <groupId>commons-cli</groupId>
        <artifactId>commons-cli</artifactId>
        <version>1.4</version>
    </dependency>
    
    <!-- ini4j for INI file parsing -->
    <dependency>
        <groupId>org.ini4j</groupId>
        <artifactId>ini4j</artifactId>
        <version>0.5.4</version>
    </dependency>
    
    <!-- SLF4J + Logback -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>1.7.36</version>
    </dependency>
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.2.12</version>
    </dependency>
    
    <!-- JUnit 4 -->
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.13.2</version>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>3.12.4</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Run:
```bash
./mvnw dependency:resolve
```

### Step 2: Create Configuration Logging

Create `src/main/resources/logback.xml`:

```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/etl-tool.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/etl-tool.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

### Step 3: Implement Exception Classes

Create base exception class:

```java
// src/main/java/com/sdd/etl/exception/ETLException.java
package com.sdd.etl.exception;

public class ETLException extends Exception {
    public ETLException(String message) {
        super(message);
    }
    
    public ETLException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

Create specialized exceptions:

```java
// src/main/java/com/sdd/etl/exception/ParameterValidationException.java
package com.sdd.etl.exception;

public class ParameterValidationException extends ETLException {
    public ParameterValidationException(String message) {
        super(message);
    }
}
```

```java
// src/main/java/com/sdd/etl/exception/ConfigurationException.java
package com.sdd.etl.exception;

public class ConfigurationException extends ETLException {
    public ConfigurationException(String message) {
        super(message);
    }
}
```

```java
// src/main/java/com/sdd/etl/exception/SubprocessException.java
package com.sdd.etl.exception;

public class SubprocessException extends ETLException {
    public SubprocessException(String message) {
        super(message);
    }
    
    public SubprocessException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

```java
// src/main/java/com/sdd/etl/exception/ConcurrentExecutionException.java
package com.sdd.etl.exception;

public class ConcurrentExecutionException extends ETLException {
    public ConcurrentExecutionException(String message) {
        super(message);
    }
}
```

### Step 4: Implement Enums

```java
// src/main/java/com/sdd/etl/enums/SubprocessType.java
package com.sdd.etl.enums;

public enum SubprocessType {
    NOT_STARTED,
    EXTRACT,
    TRANSFORM,
    LOAD,
    VALIDATE,
    CLEAN,
    COMPLETED,
    FAILED
}
```

### Step 5: Implement API Definitions (Phase 1 Only)

These are interface definitions - no implementations required.

Create subprocess interfaces in `src/main/java/com/sdd/etl/api/subprocess/`:

- `Subprocess.java` (base interface)
- `ExtractProcess.java`
- `TransformProcess.java`
- `LoadProcess.java`
- `ValidateProcess.java`
- `CleanProcess.java`

Create model interfaces in `src/main/java/com/sdd/etl/api/model/`:

- `SourceDataModel.java`
- `TargetDataModel.java`

Create workflow interface in `src/main/java/com/sdd/etl/api/workflow/`:

- `ETLDailyProcess.java`

Refer to contracts in `specs/001-etl-core-workflow/contracts/` for detailed API specifications.

### Step 6: Implement Data Models (Phase 1 Only)

Create configuration classes in `src/main/java/com/sdd/etl/config/`:

- `Configuration.java`
- `SourceConfig.java`
- `TargetConfig.java`
- `Credentials.java`
- `TransformConfig.java`
- `FilterConfig.java`
- `ValidationConfig.java`
- `LoggingConfig.java`

Refer to `specs/001-etl-core-workflow/data-model.md` for detailed specifications.

### Step 7: Implement Context (Phase 1 - Concrete Implementation)

Create `DailyProcessContext.java` in `src/main/java/com/sdd/etl/context/`:

- Use immutable design with Builder pattern
- Implement all getters
- Implement builder with fluent API
- Implement validation logic
- Implement utility methods (withSubprocess, withRecordsExtracted, etc.)

Refer to `specs/001-etl-core-workflow/contracts/context-api.md` for detailed specification.

### Step 8: Implement INI Configuration Parser

Create `INIConfigurationParser.java` in `src/main/java/com/sdd/etl/cli/parser/`:

```java
package com.sdd.etl.cli.parser;

import com.sdd.etl.config.Configuration;
import com.sdd.etl.exception.ConfigurationException;
import org.ini4j.Ini;

import java.nio.file.Path;

public class INIConfigurationParser {
    
    public Configuration parse(Path configPath) throws ConfigurationException {
        try {
            Ini ini = new Ini(configPath.toFile());
            
            // Parse sources section
            List<SourceConfig> sources = parseSources(ini);
            
            // Parse targets section
            List<TargetConfig> targets = parseTargets(ini);
            
            // Parse transforms section
            List<TransformConfig> transforms = parseTransforms(ini, sources, targets);
            
            // Parse validation section
            ValidationConfig validation = parseValidation(ini);
            
            // Parse logging section
            LoggingConfig logging = parseLogging(ini);
            
            return new Configuration(sources, targets, transforms, validation, logging);
            
        } catch (Exception e) {
            throw new ConfigurationException("Failed to parse configuration: " + e.getMessage(), e);
        }
    }
    
    private List<SourceConfig> parseSources(Ini ini) {
        // Implementation
    }
    
    private List<TargetConfig> parseTargets(Ini ini) {
        // Implementation
    }
    
    // Other parsing methods...
}
```

### Step 9: Implement CLI Components

Create CLI components in `src/main/java/com/sdd/etl/cli/command/`:

#### ParameterValidator.java

```java
package com.sdd.etl.cli.command;

import com.sdd.etl.exception.ParameterValidationException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ParameterValidator {
    
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyyMMdd");
    
    public static void validateParameters(LocalDate fromDate, LocalDate toDate, Path configPath) 
            throws ParameterValidationException {
        
        if (fromDate == null || toDate == null) {
            throw new ParameterValidationException("Date parameters are required");
        }
        
        if (fromDate.isAfter(toDate)) {
            throw new ParameterValidationException(
                String.format("From date must be before or equal to to date (%s > %s)", 
                            fromDate, toDate));
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
    
    public static LocalDate parseDate(String dateStr) throws ParameterValidationException {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ParameterValidationException(
                "Invalid date format: " + dateStr + " (expected YYYYMMDD)");
        }
    }
}
```

#### ETLCliCommand.java

```java
package com.sdd.etl.cli.command;

import com.sdd.etl.context.DailyProcessContext;
import com.sdd.etl.exception.*;
import org.apache.commons.cli.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

public class ETLCliCommand {
    
    public int execute(String[] args) {
        Options options = createOptions();
        CommandLineParser parser = new DefaultParser();
        
        try {
            CommandLine cmd = parser.parse(options, args);
            
            // Handle help
            if (cmd.hasOption("help")) {
                displayHelp();
                return 0;
            }
            
            // Handle version
            if (cmd.hasOption("version")) {
                displayVersion();
                return 0;
            }
            
            // Parse required parameters
            LocalDate fromDate = ParameterValidator.parseDate(cmd.getOptionValue("from"));
            LocalDate toDate = ParameterValidator.parseDate(cmd.getOptionValue("to"));
            Path configPath = Paths.get(cmd.getOptionValue("config"));
            
            // Validate parameters
            ParameterValidator.validateParameters(fromDate, toDate, configPath);
            
            // TODO: Load configuration
            // TODO: Detect concurrent execution
            // TODO: Execute ETL process
            
            return 0;
            
        } catch (ParameterValidationException e) {
            System.err.println("Error: " + e.getMessage());
            displayUsage();
            return 1;
        } catch (ParseException e) {
            System.err.println("Error: Failed to parse command line arguments");
            displayUsage();
            return 1;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 2;
        }
    }
    
    private Options createOptions() {
        Options options = new Options();
        
        options.addOption(Option.builder("from")
            .hasArg()
            .required()
            .desc("Inclusive start date in YYYYMMDD format")
            .build());
        
        options.addOption(Option.builder("to")
            .hasArg()
            .required()
            .desc("Inclusive end date in YYYYMMDD format")
            .build());
        
        options.addOption(Option.builder("config")
            .hasArg()
            .required()
            .desc("Path to INI configuration file")
            .build());
        
        options.addOption(Option.builder("help")
            .desc("Display this help message and exit")
            .build());
        
        options.addOption(Option.builder("version")
            .desc("Display version information and exit")
            .build());
        
        return options;
    }
    
    private void displayHelp() {
        System.out.println("ETL Tool - Extract, Transform, Load Data Pipeline\n");
        System.out.println("Usage:");
        System.out.println("  etl --from YYYYMMDD --to YYYYMMDD --config <path> [options]\n");
        System.out.println("Required Parameters:");
        System.out.println("  --from <date>    Inclusive start date in YYYYMMDD format");
        System.out.println("  --to <date>      Inclusive end date in YYYYMMDD format");
        System.out.println("  --config <path>  Path to INI configuration file\n");
        System.out.println("Optional Parameters:");
        System.out.println("  --help           Display this help message and exit");
        System.out.println("  --version        Display version information and exit");
    }
    
    private void displayVersion() {
        System.out.println("ETL Tool version 1.0.0");
    }
    
    private void displayUsage() {
        System.out.println("\nUsage: etl --from <date> --to <date> --config <path>");
        System.out.println("  etl --help for more information\n");
    }
}
```

#### HelpCommand.java

```java
package com.sdd.etl.cli.command;

public class HelpCommand {
    
    public void display() {
        System.out.println("ETL Tool - Extract, Transform, Load Data Pipeline\n");
        System.out.println("Usage:");
        System.out.println("  etl --from YYYYMMDD --to YYYYMMDD --config <path> [options]\n");
        System.out.println("Required Parameters:");
        System.out.println("  --from <date>    Inclusive start date in YYYYMMDD format (e.g., 20250101)");
        System.out.println("  --to <date>      Inclusive end date in YYYYMMDD format (e.g., 20250131)");
        System.out.println("  --config <path>  Path to INI configuration file\n");
        System.out.println("Optional Parameters:");
        System.out.println("  --help           Display this help message and exit");
        System.out.println("  --version        Display version information and exit\n");
        System.out.println("Examples:");
        System.out.println("  etl --from 20250101 --to 20250131 --config config.ini");
        System.out.println("  etl --from 20250201 --to 20250201 --config /path/to/config.ini\n");
        System.out.println("For more information, visit: https://github.com/example/sdd-etl-tool");
    }
}
```

### Step 10: Implement Logging Components

Create logging components in `src/main/java/com/sdd/etl/logging/`:

#### ProcessStatusLogger.java

```java
package com.sdd.etl.logging;

import com.sdd.etl.context.DailyProcessContext;
import com.sdd.etl.enums.SubprocessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ProcessStatusLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessStatusLogger.class);
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public void logProcessStart(LocalDate date) {
        logger.info("=== ETL Process Started for Date: {} ===", 
                   date.format(DATE_FORMATTER));
    }
    
    public void logProcessComplete(LocalDate date, DailyProcessContext context) {
        logger.info("=== ETL Process Completed for Date: {} ===", 
                   date.format(DATE_FORMATTER));
        logger.info("Records: Extracted={}, Transformed={}, Loaded={}", 
                   context.getRecordsExtracted(),
                   context.getRecordsTransformed(),
                   context.getRecordsLoaded());
    }
    
    public void logProcessFailed(LocalDate date, DailyProcessContext context, Exception e) {
        logger.error("=== ETL Process Failed for Date: {} ===", 
                    date.format(DATE_FORMATTER), e);
    }
    
    public void logSubprocessStart(DailyProcessContext context, SubprocessType type) {
        logger.info("Subprocess Started: {} [Extracted={}, Transformed={}, Loaded={}]",
                   type,
                   context.getRecordsExtracted(),
                   context.getRecordsTransformed(),
                   context.getRecordsLoaded());
    }
    
    public void logSubprocessComplete(DailyProcessContext context, SubprocessType type) {
        logger.info("Subprocess Completed: {} [Extracted={}, Transformed={}, Loaded={}]",
                   type,
                   context.getRecordsExtracted(),
                   context.getRecordsTransformed(),
                   context.getRecordsLoaded());
    }
}
```

#### ConcurrentExecutionDetector.java

```java
package com.sdd.etl.logging;

import com.sdd.etl.exception.ConcurrentExecutionException;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ConcurrentExecutionDetector {
    
    private static final Path LOCK_FILE_PATH = 
        Paths.get(System.getProperty("java.io.tmpdir"), "etl-tool.lock");
    
    public void detectAndPreventConcurrentExecution() throws ConcurrentExecutionException {
        try {
            // Try to acquire exclusive lock on lock file
            FileChannel channel = FileChannel.open(LOCK_FILE_PATH, 
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            FileLock lock = channel.tryLock();
            
            if (lock == null) {
                throw new ConcurrentExecutionException(
                    "Another ETL process is already running. " +
                    "Only one ETL process can run at a time.");
            }
            
            // Lock acquired successfully - detector will release lock later
            
        } catch (ConcurrentExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new ConcurrentExecutionException(
                "Failed to detect concurrent execution: " + e.getMessage(), e);
        }
    }
    
    public AutoCloseable acquireExecutionLock() throws ConcurrentExecutionException {
        try {
            FileChannel channel = FileChannel.open(LOCK_FILE_PATH, 
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            FileLock lock = channel.tryLock();
            
            if (lock == null) {
                throw new ConcurrentExecutionException(
                    "Another ETL process is already running.");
            }
            
            return new LockReleaser(channel, lock);
            
        } catch (ConcurrentExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new ConcurrentExecutionException(
                "Failed to acquire execution lock: " + e.getMessage(), e);
        }
    }
    
    private static class LockReleaser implements AutoCloseable {
        private final FileChannel channel;
        private final FileLock lock;
        
        LockReleaser(FileChannel channel, FileLock lock) {
            this.channel = channel;
            this.lock = lock;
        }
        
        @Override
        public void close() throws Exception {
            if (lock != null && lock.isValid()) {
                lock.release();
            }
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        }
    }
}
```

### Step 11: Implement Workflow Components (Phase 1 - Concrete Implementation)

Create workflow components in `src/main/java/com/sdd/etl/workflow/`:

- `DailyETLWorkflow.java` (concrete implementation)
- `ProcessExecutionOrchestrator.java` (concrete implementation)

Refer to `specs/001-etl-core-workflow/contracts/workflow-api.md` for detailed specifications.

### Step 12: Create Main Entry Point

Create `src/main/java/com/sdd/etl/Main.java`:

```java
package com.sdd.etl;

import com.sdd.etl.cli.command.ETLCliCommand;

public class Main {
    
    public static void main(String[] args) {
        ETLCliCommand command = new ETLCliCommand();
        int exitCode = command.execute(args);
        System.exit(exitCode);
    }
}
```

## Testing

### Run Unit Tests

```bash
./mvnw test
```

### Run Specific Test

```bash
./mvnw test -Dtest=DailyProcessContextTest
```

### Generate Test Coverage Report

```bash
./mvnw clean test jacoco:report
```

View report at: `target/site/jacoco/index.html`

**Note**: Test coverage must be >60% (per constitution requirement).

## Running the Tool

### Build the Tool

```bash
./mvnw clean package
```

### Run with Help

```bash
java -jar target/sdd-etl-tool-1.0.0.jar --help
```

### Run with Sample Configuration

```bash
java -jar target/sdd-etl-tool-1.0.0.jar \
  --from 20250101 \
  --to 20250131 \
  --config config/etl-config.ini
```

## Sample Configuration File

Create `config/etl-config.ini`:

```ini
[sources]
count=1

[source.0]
name=source_database
type=database
connectionString=jdbc:postgresql://localhost:5432/sourcedb
credentials.username=etl_user
credentials.password=secure_password
primaryKeyField=id

[targets]
count=1

[target.0]
name=target_warehouse
type=database
connectionString=jdbc:mysql://localhost:3306/warehouse
credentials.username=etl_user
credentials.password=secure_password

[transforms]
count=1

[transform.0]
sourceName=source_database
targetName=target_warehouse

[transform.0.fieldMapping]
id=id
name=name
timestamp=timestamp

[validation]
checkCompleteness=true
checkQuality=true
checkConsistency=true
completenessRules=id,name,timestamp

[logging]
logDirectory=./logs
consoleLogging=true
fileLogging=true
logLevel=INFO
```

## Development Tips

### TDD Approach

1. Write failing test first
2. Implement minimal code to pass test
3. Refactor
4. Repeat

### Code Style

- Follow Java naming conventions
- Use meaningful variable and method names
- Add Javadoc comments for public APIs
- Keep methods short and focused

### Debugging

- Use IDE debugger for stepping through code
- Enable DEBUG logging level in `logback.xml`
- Check logs in `logs/etl-tool.log`

## Common Issues

### Issue: "Unsupported class file version"

**Solution**: Ensure you're using Java 8 (JDK 1.8)

```bash
java -version
```

### Issue: "Failed to parse configuration"

**Solution**: Check that:
- Configuration file exists
- File has `.ini` extension
- File is readable
- INI format is correct

### Issue: "Another ETL process is already running"

**Solution**: 
- Check if another ETL process is running
- If not, delete lock file manually: `rm /tmp/etl-tool.lock`

## Next Steps

After completing Phase 1 implementation:

1. Run all tests: `./mvnw test`
2. Check test coverage: `./mvnw jacoco:report`
3. Verify coverage >60%
4. Review implementation against contracts
5. Run integration tests
6. Commit changes

## Resources

- **Spec**: `specs/001-etl-core-workflow/spec.md`
- **Plan**: `specs/001-etl-core-workflow/plan.md`
- **Research**: `specs/001-etl-core-workflow/research.md`
- **Data Model**: `specs/001-etl-core-workflow/data-model.md`
- **Contracts**: `specs/001-etl-core-workflow/contracts/`
- **Constitution**: `.specify/memory/constitution.md`

## Support

For issues or questions:
1. Check the specification documents
2. Review the plan and research
3. Consult the contracts for API details
4. Check the constitution for project principles
