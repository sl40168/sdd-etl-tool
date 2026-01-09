## Phase III: Extract Build **Xbond Trade** Data from COS for ETL

This is phase III for ETL tool. In this phase, we need to extract **Xbond Trade** data from COS. COS(Cloud Object Storage) is a file system provided by Tencent, in which the files are stored in the sub directors of given bucket.

## I. Extract **Xbond Trade** data from COS

1. This extractor **MUST** follow the same principle as **Xbond Quote** Extractor in **Phase II**
2. It **MUST** extract data as its own extension of **SourceDataModel**

## II. Make API more reasonable

1. In `com.sdd.etl.context.ETLContext`, the return type of methods `getCurrentDate` is better to be `LocalDate` than `String`.
2. In `com.sdd.etl.model.WorkflowResult`, the type of `startDate` and `endDate` are also good to be `LocalDate`
3. Thus, `com.sdd.etl.util.DateRangeGenerator.generate` should return `List<LocalDate>` instead of `List<String>`
4. **ALL** dependencies to above 3 parts need to be updated as well.

## III. Create a default INI Configure File

1. A default INI configure file **MUST** be added to cover existing components. It would be a **DEMO** for real one.
