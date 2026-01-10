## Phase II: Extract Build **Xbond Quote** Data from COS for ETL

This is phase II for ETL tool. In this phase, we need to extract **Xbond Quote** data from COS. COS(Cloud Object Storage) is a file system provided by Tencent, in which the files are stored in the sub directors of given bucket.

## I. A common Extractor API is needed

1. As COS is not the only source data system, a common Extractor API **MUST** be defined, without any special data source information.
2. It **MUST** be easy for extension against other source data system, such as MySql.
3. API **MUST** allow extractor to filter source data, based on the information from Context.

## II. Data extraction of COS **MUST** include 5 steps

1. Filter out all files match the condition, via the API provided by COS
2. Download all matched files to local sub director in working director.
3. Read all single files back memory as raw data
4. Convert all raw data together to a set of `SourceDataModel`, **NOTE** that raw data may not be in the same data structure as `SourceDataModel`
5. Return the set of converted `SourceDataModel` as output

## III. Integrate Data Extractor with `ExtractSubprocess`

1. Data extraction **MUST** be executed in `ExtractSubprocess`
2. Integration **MUST** support multiple data extractions from different source to be executed simultaneously. `ExtractSubprocess` **MUST** consolidate all extracted as one output.
3. **ONLY** when all data extractions are complete, extract sub process can be marked as complete.
