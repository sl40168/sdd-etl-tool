This project will build a ETL tool, which help you to extract data from different data sources, transform and load data to different data targets crossing days.

In this phase, we will focus on the API, Data Model, CLI interface and work flow design ONLY. **No concrete implementation** is required at this phase, **EXCEPT** the work flow.

## Phase 1: API/Data Model Design

## I. CLI Interface

1. 3 parameters are required to start the tool:

    - `--from`: a date string in format `YYYYMMDD`, to indicate after which date(included), the data **SHOULD** be extracted.

    - `--to`: a date string in format `YYYYMMDD`, to indicate before which date(included), the data **SHOULD** be extracted.

    - `--config`: a path to the configuration file, which contains the source, target and transform configuration.

2. 1 command is required to supported by the tool:

    - `--help`: print the help message.

3. All user input must be validated.

## II. Work Flow

1. The ETL process, including extract, transform and load, **MUST** be executed day by day, starting from the `--from` date(included), and ending at the `--to` date(included). Every day is a separate and complete process executed in sequence.

2. A ETL process **MUST** include 5 sub processes: extract, transform, load, validate and clean.

3. In each day's process, all sub processes, including extract, transform, load, validate and clean, **MUST** be executed in sequence. **ONLY WHEN** the current sub process is completed, the next sub process **CAN** be triggered. **ONLY WHEN** all sub processes are completed, the day's process **CAN** be triggered.

4. The extract process **MUST** support multiple data sources, and the load process **MUST** support multiple data targets. Thus the transform process **MUST** support data transform from multiple sources to multiple targets. 

5. **ONLY WHEN** all data sources extraction are completed, the extract process **CAN** be marked as complete. **ONLY WHEN** all source data has been transformed to target data, the transform process **CAN** be marked as complete. **ONLY WHEN** all data targets loading are completed, the load process **CAN** be marked as complete.

6. A status update **MUST** be logged in file and console at each sub process and day's process complete.

## III. 2 Groups of Data Model

1. **Source Data Model**: This model represents the structure of data from various sources. It includes metadata about the data fields, data types, and any transformations needed to standardize the data for processing. Specified data source **WILL EXTEND** this model according to the real data structure.

2. **Target Data Model**: This model represents the structure of data in the target systems. It includes metadata about the data fields, data types, and any constraints or requirements of the target system. Specified target system **WILL EXTEND** this model according to the real data structure.

## IV. Context

1. A context **MUST** be created for each day's process, which contains the current day's date, the current sub process, the number of data had been extracted, transformed and loaded, the configure which contains the source, target and transform configuration.

2. A context **MUST** be passed to each sub process.

3. All sub components **MUST** use context to transfer data.

## V. APIs

1. A component API **MUST** be created for each sub process, which contains the sub process logic and the context.

2. A ELT API **MUST** be created for each day's process, which contains the day's process logic and the context.
