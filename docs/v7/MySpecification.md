## Phase VII: Transform data from `SourceDataModel` to `TargetDataModel`

This is phase VII for ETL tool.

In this phase, we will transform data from `SourceDataModel` extensions to `TargetDataModel` extensions.

## I. A common Transformer API is needed
1. Considering we support multiple sources and targets, transform sub process has to handle a many-to-many mapping, so a common transformer API **MUST** be defined.
2. Transformer **MUST** receive a list of `SourceDataModel` with the same type, and transform them to another list of `TargetDataModel` with the same type.
3. Transformer **MUST** transform data one by one.

## II. Three concrete Transformers are needed
1. We need three concrete transformers in this application, which transform below data:
   - `com.sdd.etl.model.XbondQuoteDataModel` -> `com.sdd.etl.loader.model.XbondQuoteDataModel`
   - `com.sdd.etl.model.XbondTradeDataModel` -> `com.sdd.etl.loader.model.XbondTradeDataModel`
   - `com.sdd.etl.source.extract.db.quote.BondFutureQuoteDataModel` -> `com.sdd.etl.loader.model.BondFutureQuoteDataModel`
2. Transformer **MUST** be chosen by the type of `SourceDataModel`
3. Data transformation **MUST** be based on fields name. If the field does not exist in `SourceDataModel` side, keep it unassigned in `TargetDataModel`.
4. All three above transformation are one-to-one.

## III. Data transform **MUST** be executed in `TransformSubprocess`
1. `TransformSubprocess` **MUST** retrieve all extracted data from `ETLContext`
2. `TransformSubprocess` **MUST** group all extracted data by type
3. `TransformSubprocess` **MUST** choose Transformer for each group and execute data transformation concurrently
4. `TransformSubprocess` **MUST** consolidates all transformed data together as a list.
5. `TransformSubprocess` **MUST** transfer transformed data to downstream via `ETLContext`
6. `TransformSubprocess` **MUST** break the whole process once an exception thrown during transformation and involve user check manually