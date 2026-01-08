## Background
This is a ETL CLI Tool which can help you to extract data from different data sources, transform and load data to different data targets crossing days.

## Core Principles

1. Project **MUST** be built on Java 8
2. Project **MUST** be built based on maven build tool wrapper
3. All functionalities **MUST** be exposed on CLI interface
4. All configurations **MUST** be configured and load from INI configuration file
5. **MUST** keep the boundaries of components clearly
6. Test Driven Development, the unit test coverage **MUST** be> 60%. At any time, a new unit test **MUST** be added before bug fix
7. All business bug fixes **MUST** be recorded by version and referred in future
8. **ENCOURAGE** to use well-konw Third Party Open Source Libraries, instread of building your own
9. At the end of each implement round, full build and test **MUST** be passed
10. **DO NOT** make any change the files below:
    - `./specify/scripts/*.*`
    - `./specify/templates/*.*`
    - `./codebuddy`
