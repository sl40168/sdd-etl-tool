## Background
This is a ETL CLI Tool which can help you to extract data from different data sources, transform and load data to different data targets crossing days.

## Core Principles

1. Project **MUST** be built on Java 8
2. Project **MUST** be built based on Maven Build Tool With Wrapper
3. All Functionalities **MUST** be Exposed on CLI Interface
4. All Configurations **MUST** be Configured and Load From INI Configuration File
5. Keep the Boundaries Among Components Clearly By Good Interface Definition
6. Test Driven Development, Unit Test Coverage > 60%. At any time, a new unit test **MUST** be added before bug fix
7. All business bug fixes **MUST** be recorded by version and referred in future
8. **ENCOURAGE** to use well-konw Third Party Open Source Libraries, instread of building your own
9. At the end of each implement round, full build and test **MUST** be passed
