## Phase V: Extract **Bond Future Quote** Data from MySql for ETL

This is phase V for ETL tool. In this phase, we need to extract **Bond Future Quote** data from MySql Database. 

## I. A base Extractor for Database is needed

1. We have already defined the API of Extractor in **Phase II**. Database Extractor **MUST** extend the same API.
2. A template SQL **MUST** be provided in INI configure file. But each concrete extractor implement **CAN** have its own condition to fill template.

## II. A concrete Database Extractor for **Bond Future Quote** is needed

1. It **MUST** extract data from MySql.
2. The template SQL is as below
```sql
select * from bond.fut_tick where trading_date = {BUSINESS_DATE}
```
3. For **Bond Future Quote** Extractor, the only filter condition is `BUSINESS_DATE`, which is current date ETL processing.
4. Extractor builds the extension of `SourceDataModel` directly on row level.

## III. Extract **Bond Future Quote** concurrent with other existing Extractors
1. We have already defined COS extractors for **Xbond Quote** and **Xbond Trade**, this extractor **MUST** be executed with them concurrently.