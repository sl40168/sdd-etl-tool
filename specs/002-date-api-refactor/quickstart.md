# Quick Start: Updated Date APIs and Default Config

**Feature Branch**: `002-date-api-refactor`  
**Date**: 2026-01-10  
**Status**: Phase 1 Design

## Overview

This guide helps developers understand and use the refactored date APIs and default INI configuration file. The changes improve type safety and reduce parsing errors.

## What's Changed

### 1. Date Types Changed
- `ETLContext.getCurrentDate()` now returns `LocalDate` (was `String`)
- `WorkflowResult.startDate` and `.endDate` are now `LocalDate` (was `String`)
- `DateRangeGenerator.generate()` returns `List<LocalDate>` (was `List<String>`)

### 2. New Default Configuration
- Default INI file at `src/main/resources/config.ini`
- Demonstrates all existing components
- Loadable via `ConfigurationLoader`

## Code Examples

### Using Updated ETLContext
```java
// Before (old API)
ETLContext context = new ETLContext();
context.setCurrentDate("20250101");
String dateString = context.getCurrentDate(); // Returns "20250101"

// After (new API)
ETLContext context = new ETLContext();
LocalDate date = LocalDate.of(2025, 1, 1);
context.setCurrentDate(date);
LocalDate retrievedDate = context.getCurrentDate(); // Returns LocalDate(2025-01-01)

// Formatting for display/logging
String formatted = retrievedDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
```

### Using Updated WorkflowResult
```java
// Before
WorkflowResult result = new WorkflowResult();
result.setStartDate("20250101");
result.setEndDate("20250107");
String start = result.getStartDate(); // "20250101"
String end = result.getEndDate(); // "20250107"

// After
WorkflowResult result = new WorkflowResult();
result.setStartDate(LocalDate.of(2025, 1, 1));
result.setEndDate(LocalDate.of(2025, 1, 7));
LocalDate start = result.getStartDate(); // LocalDate(2025-01-01)
LocalDate end = result.getEndDate(); // LocalDate(2025-01-07)

// Date arithmetic
long daysBetween = ChronoUnit.DAYS.between(start, end); // 6 days
```

### Using Updated DateRangeGenerator
```java
// Before
List<String> dateStrings = DateRangeGenerator.generate("20250101", "20250103");
// Returns ["20250101", "20250102", "20250103"]

// After  
List<LocalDate> dates = DateRangeGenerator.generate("20250101", "20250103");
// Returns [LocalDate(2025-01-01), LocalDate(2025-01-02), LocalDate(2025-01-03)]

// Iterating with type safety
for (LocalDate date : dates) {
    System.out.println(date.getDayOfWeek()); // MONDAY, TUESDAY, WEDNESDAY
}
```

### Using Default Configuration
```java
// Load default configuration
ETConfiguration config = ConfigurationLoader.loadClasspathConfig("config.ini");

// Access configuration properties
String dbUrl = config.getDatabase().getUrl();
int batchSize = config.getEtl().getBatchSize();
String logLevel = config.getLogging().getLevel();

// Use in ETL workflow
ETLContext context = new ETLContext();
context.setConfig(config);
```

## Migration Steps for Existing Code

### 1. Identify Usages
```bash
# Use IDE refactoring tools to find all usages of:
# - ETLContext.getCurrentDate()
# - WorkflowResult.getStartDate()/.getEndDate()
# - WorkflowResult.setStartDate()/.setEndDate()
# - DateRangeGenerator.generate()
```

### 2. Update Callers
```java
// Example: Updating a caller of getCurrentDate()
// Old code
String dateString = context.getCurrentDate();
LocalDate date = LocalDate.parse(dateString, DATE_FORMATTER);

// New code (direct usage)
LocalDate date = context.getCurrentDate(); // Already LocalDate
```

### 3. Add Conversion Helpers
```java
// If you need to format dates for logging/output
public class DateFormatter {
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyyMMdd");
    
    public static String format(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }
    
    public static LocalDate parse(String dateString) {
        return LocalDate.parse(dateString, DATE_FORMATTER);
    }
}
```

## Testing the Changes

### Unit Tests
```java
@Test
public void testETLContextReturnsLocalDate() {
    ETLContext context = new ETLContext();
    LocalDate expected = LocalDate.of(2025, 1, 1);
    context.setCurrentDate(expected);
    LocalDate actual = context.getCurrentDate();
    assertEquals(expected, actual);
}

@Test
public void testDateRangeGeneratorReturnsLocalDateList() {
    List<LocalDate> dates = DateRangeGenerator.generate("20250101", "20250103");
    assertEquals(3, dates.size());
    assertEquals(LocalDate.of(2025, 1, 1), dates.get(0));
}
```

### Integration Test
```java
@Test
public void testDefaultConfigLoadsSuccessfully() {
    // Should not throw
    ETConfiguration config = ConfigurationLoader.loadClasspathConfig("config.ini");
    assertNotNull(config);
    assertNotNull(config.getDatabase().getUrl());
}
```

## Troubleshooting

### Common Issues

1. **ClassCastException when calling getCurrentDate()**
   - Cause: Old code expecting `String` but now returns `LocalDate`
   - Fix: Update caller to handle `LocalDate` or use `DateFormatter.format()`

2. **DateTimeParseException in DateRangeGenerator**
   - Cause: Invalid date format (not YYYYMMDD)
   - Fix: Ensure input strings are in correct format

3. **Configuration file not found**
   - Cause: File not in classpath
   - Fix: Ensure `config.ini` is in `src/main/resources`

### Debugging Tips
- Use `System.out.println(date.getClass())` to verify type
- Check `pom.xml` dependencies for `java.time` support
- Verify Java version is 8 or higher

## Next Steps

1. **Review the API contracts** in `contracts/date-api-refactor-contract.md`
2. **Run existing tests** to ensure backward compatibility
3. **Update any dependent code** using IDE refactoring tools
4. **Test the default configuration** by running a sample ETL workflow

## Support

- **API Documentation**: See individual class javadocs
- **Code Examples**: Refer to existing test files
- **Issues**: Use the project's issue tracker for bugs/questions