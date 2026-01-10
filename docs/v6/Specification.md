## Phase VI: Load Data to DolphinDB

This is phase VI for ETL tool. 

In this phase, we will load all transformed data to DolphinDB, which is our target.

## I. A common Loader API is needed
1. Although the target system in this case is DolphinDB, but we still need a common Loader API for future extension.
2. It **MUST** be easy to extend against other target system, such as MySql.

## II. Data loading to DolphinDB **MUST** via its Java API
1. DolphinDB provides a Java API for application. We **MUST** use it.

## III. Load data to DolphinDB **MUST** follow below steps in a **DAILY** ETL process
1. Create all temporary tables **BEFORE** data loading
2. Sorting all transformed `TargetDataModel` base on given fields
3. Load sorted data into different target tables **IN SEQUENCE**, base on the data type of `TargetDataModel`
4. Clean up all created temporary tables **AFTER** data validation.

## IV. Integrate Data Loader with `LoadSubprocess`
1. Data loading **MUST** be executed in `LoadSubprocess`
2. `LoadSubprocess` **MUST** receive all transformed together.
3. `LoadSubprocess` **MUST** support to load data into different target system, but data **MUST** be loaded one by one. **NOTE** this may conflict with existing design, if so update it.
4. **ONLY** when all data are loaded successfully, `LoadSubprocess` can be marked as complete

## V. Clean up all created temporary tables in `CleanSubprocess`
1. **MUST** clean up all temporary tables used in loading, thus the environment is transparent for next ETL process.

## VI. Exception handle during data loading
1. Any exception **MUST** break the ETL process and let user to check manually.