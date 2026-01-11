## Phase VI: Load Data to DolphinDB

This is phase VI for ETL tool.

In this phase, we will load all transformed data to DolphinDB, which is our target.

What we need to do in this phase include:

## I. Define Loader API
1. Data Loader set up connection to target system based on `com.sdd.etl.context.ETLContext`.
2. The data will be loaded **SHOULD** be a list of `TargetDataModel` extension and passed in as parameter.
3. Except DolphinDB, we also have other types of target system, such as MySql and so on. So the Loader API **MUST** be neutral.

## II. Provide an extension of `LoadSubprocess` for DolphinDB to load data
1. `LoadSubprocess` **MUST** execute temporary table creation script via DolphinDB Java API **BEFORE** data loading
2. The script had been provided in `@docs/v6/temporary_table_creation.dos`. You **MUST** read scripts from resources file at runtime.
3. Retrieve transformed data from `ETLContext` and pass it to embedded DolphinDB Data Loader.

## III. Provide an abstract DolphinDB `TargetDataModel`
1. Considering DolphinDB is a column based database, all fields in extension of DolphinDB `TargetDataModel` **MUST** have an indicator for order, which is a number. It can be implemented either by metadata or annotation. 

## IV. Provide a concrete Data Loader for DolphinDB
1. DolphinDB data loader **MUST** set up connection to DolphinDB instance base on configure in `ETLContext`
2. DolphinDB data loader **MUST** sort all `TargetDataModel` records by `receive_time`. If an object does not have `receive_time` field, then remove it and log a warning.
3. DolphinDB data loader **MUST** insert data into different target tables via DolphinDB Java API, base on the type of record.
4. DolphinDB data loader **MUST** break the ETL process while any exception happens during data insertion. User **MUST** be involved and check manually.

## V. Load **Xbond Quote** data
1. The target table for **Xbond Quote** is `xbond_quote_stream_temp`
2. The `TargetDataModel` for **Xbond Quote** **MUST** be as below. The `Type` column shows the data type in DolphinDB, while creating the extension of `TargetDataModel`, you **MUST** choose the compatible type.
```csv
| Order | Name | Type |
|-------|------|------|
| 1 | business_date | DATE |
| 2 | exch_product_id | SYMBOL |
| 3 | product_type | SYMBOL |
| 4 | exchange | SYMBOL |
| 5 | source | SYMBOL |
| 6 | settle_speed | INT |
| 7 | level | SYMBOL |
| 8 | status | SYMBOL |
| 9 | pre_close_price | DOUBLE |
| 10 | pre_settle_price | DOUBLE |
| 11 | pre_interest | DOUBLE |
| 12 | open_price | DOUBLE |
| 13 | high_price | DOUBLE |
| 14 | low_price | DOUBLE |
| 15 | close_price | DOUBLE |
| 16 | settle_price | DOUBLE |
| 17 | upper_limit | DOUBLE |
| 18 | lower_limit | DOUBLE |
| 19 | total_volume | DOUBLE |
| 20 | total_turnover | DOUBLE |
| 21 | open_interest | DOUBLE |
| 22 | bid_0_price | DOUBLE |
| 23 | bid_0_yield | DOUBLE |
| 24 | bid_0_yield_type | SYMBOL |
| 25 | bid_0_tradable_volume | DOUBLE |
| 26 | bid_0_volume | DOUBLE |
| 27 | offer_0_price | DOUBLE |
| 28 | offer_0_yield | DOUBLE |
| 29 | offer_0_yield_type | SYMBOL |
| 30 | offer_0_tradable_volume | DOUBLE |
| 31 | offer_0_volume | DOUBLE |
| 32 | bid_1_price | DOUBLE |
| 33 | bid_1_yield | DOUBLE |
| 34 | bid_1_yield_type | SYMBOL |
| 35 | bid_1_tradable_volume | DOUBLE |
| 36 | bid_1_volume | DOUBLE |
| 37 | offer_1_price | DOUBLE |
| 38 | offer_1_yield | DOUBLE |
| 39 | offer_1_yield_type | SYMBOL |
| 40 | offer_1_tradable_volume | DOUBLE |
| 41 | offer_1_volume | DOUBLE |
| 42 | bid_2_price | DOUBLE |
| 43 | bid_2_yield | DOUBLE |
| 44 | bid_2_yield_type | SYMBOL |
| 45 | bid_2_tradable_volume | DOUBLE |
| 46 | bid_2_volume | DOUBLE |
| 47 | offer_2_price | DOUBLE |
| 48 | offer_2_yield | DOUBLE |
| 49 | offer_2_yield_type | SYMBOL |
| 50 | offer_2_tradable_volume | DOUBLE |
| 51 | offer_2_volume | DOUBLE |
| 52 | bid_3_price | DOUBLE |
| 53 | bid_3_yield | DOUBLE |
| 54 | bid_3_yield_type | SYMBOL |
| 55 | bid_3_tradable_volume | DOUBLE |
| 56 | bid_3_volume | DOUBLE |
| 57 | offer_3_price | DOUBLE |
| 58 | offer_3_yield | DOUBLE |
| 59 | offer_3_yield_type | SYMBOL |
| 60 | offer_3_tradable_volume | DOUBLE |
| 61 | offer_3_volume | DOUBLE |
| 62 | bid_4_price | DOUBLE |
| 63 | bid_4_yield | DOUBLE |
| 64 | bid_4_yield_type | SYMBOL |
| 65 | bid_4_tradable_volume | DOUBLE |
| 66 | bid_4_volume | DOUBLE |
| 67 | offer_4_price | DOUBLE |
| 68 | offer_4_yield | DOUBLE |
| 69 | offer_4_yield_type | SYMBOL |
| 70 | offer_4_tradable_volume | DOUBLE |
| 71 | offer_4_volume | DOUBLE |
| 72 | bid_5_price | DOUBLE |
| 73 | bid_5_yield | DOUBLE |
| 74 | bid_5_yield_type | SYMBOL |
| 75 | bid_5_tradable_volume | DOUBLE |
| 76 | bid_5_volume | DOUBLE |
| 77 | offer_5_price | DOUBLE |
| 78 | offer_5_yield | DOUBLE |
| 79 | offer_5_yield_type | SYMBOL |
| 80 | offer_5_tradable_volume | DOUBLE |
| 81 | offer_5_volume | DOUBLE |
| 82 | event_time | TIMESTAMP |
| 83 | receive_time | TIMESTAMP |
```
## VI. Load **Xbond Trade** data
1. The target table for **Xbond Trade** is `xbond_trade_stream_temp`
2. The `TargetDataModel` for **Xbond Trade** **MUST** be as below. The `Type` column shows the data type in DolphinDB, while creating the extension of `TargetDataModel`, you **MUST** choose the compatible type.
```csv
| Order | Name | Type |
|-------|------|------|
| 1 | business_date | DATE |
| 2 | exch_product_id | SYMBOL |
| 3 | product_type | SYMBOL |
| 4 | exchange | SYMBOL |
| 5 | source | SYMBOL |
| 6 | settle_speed | INT |
| 7 | last_trade_price | DOUBLE |
| 8 | last_trade_yield | DOUBLE |
| 9 | last_trade_yield_type | SYMBOL |
| 10 | last_trade_volume | DOUBLE |
| 11 | last_trade_turnover | DOUBLE |
| 12 | last_trade_interest | DOUBLE |
| 13 | last_trade_side | SYMBOL |
| 14 | event_time | TIMESTAMP |
| 15 | receive_time | TIMESTAMP |
```
## VII. Load **Bond Future Quote** data
1. The target table for **Bond Future Quote** is `fut_market_price_stream_temp`
2. The `TargetDataModel` for **Bond Future Quote** **MUST** be as below. The `Type` column shows the data type in DolphinDB, while creating the extension of `TargetDataModel`, you **MUST** choose the compatible type.
```csv
| Order | Name | Type |
|-------|------|------|
| 1 | business_date | DATE |
| 2 | exch_product_id | SYMBOL |
| 3 | product_type | SYMBOL |
| 4 | exchange | SYMBOL |
| 5 | source | SYMBOL |
| 6 | settle_speed | INT |
| 7 | last_trade_price | DOUBLE |
| 8 | last_trade_yield | DOUBLE |
| 9 | last_trade_yield_type | SYMBOL |
| 10 | last_trade_volume | DOUBLE |
| 11 | last_trade_turnover | DOUBLE |
| 12 | last_trade_interest | DOUBLE |
| 13 | last_trade_side | SYMBOL |
| 14 | level | SYMBOL |
| 15 | status | SYMBOL |
| 16 | pre_close_price | DOUBLE |
| 17 | pre_settle_price | DOUBLE |
| 18 | pre_interest | DOUBLE |
| 19 | open_price | DOUBLE |
| 20 | high_price | DOUBLE |
| 21 | low_price | DOUBLE |
| 22 | close_price | DOUBLE |
| 23 | settle_price | DOUBLE |
| 24 | upper_limit | DOUBLE |
| 25 | lower_limit | DOUBLE |
| 26 | total_volume | DOUBLE |
| 27 | total_turnover | DOUBLE |
| 28 | open_interest | DOUBLE |
| 29 | bid_0_price | DOUBLE |
| 30 | bid_0_yield | DOUBLE |
| 31 | bid_0_yield_type | SYMBOL |
| 32 | bid_0_tradable_volume | DOUBLE |
| 33 | bid_0_volume | DOUBLE |
| 34 | offer_0_price | DOUBLE |
| 35 | offer_0_yield | DOUBLE |
| 36 | offer_0_yield_type | SYMBOL |
| 37 | offer_0_tradable_volume | DOUBLE |
| 38 | offer_0_volume | DOUBLE |
| 39 | bid_1_price | DOUBLE |
| 40 | bid_1_yield | DOUBLE |
| 41 | bid_1_yield_type | SYMBOL |
| 42 | bid_1_tradable_volume | DOUBLE |
| 43 | bid_1_volume | DOUBLE |
| 44 | offer_1_price | DOUBLE |
| 45 | offer_1_yield | DOUBLE |
| 46 | offer_1_yield_type | SYMBOL |
| 47 | offer_1_tradable_volume | DOUBLE |
| 48 | offer_1_volume | DOUBLE |
| 49 | bid_2_price | DOUBLE |
| 50 | bid_2_yield | DOUBLE |
| 51 | bid_2_yield_type | SYMBOL |
| 52 | bid_2_tradable_volume | DOUBLE |
| 53 | bid_2_volume | DOUBLE |
| 54 | offer_2_price | DOUBLE |
| 55 | offer_2_yield | DOUBLE |
| 56 | offer_2_yield_type | SYMBOL |
| 57 | offer_2_tradable_volume | DOUBLE |
| 58 | offer_2_volume | DOUBLE |
| 59 | bid_3_price | DOUBLE |
| 60 | bid_3_yield | DOUBLE |
| 61 | bid_3_yield_type | SYMBOL |
| 62 | bid_3_tradable_volume | DOUBLE |
| 63 | bid_3_volume | DOUBLE |
| 64 | offer_3_price | DOUBLE |
| 65 | offer_3_yield | DOUBLE |
| 66 | offer_3_yield_type | SYMBOL |
| 67 | offer_3_tradable_volume | DOUBLE |
| 68 | offer_3_volume | DOUBLE |
| 69 | bid_4_price | DOUBLE |
| 70 | bid_4_yield | DOUBLE |
| 71 | bid_4_yield_type | SYMBOL |
| 72 | bid_4_tradable_volume | DOUBLE |
| 73 | bid_4_volume | DOUBLE |
| 74 | offer_4_price | DOUBLE |
| 75 | offer_4_yield | DOUBLE |
| 76 | offer_4_yield_type | SYMBOL |
| 77 | offer_4_tradable_volume | DOUBLE |
| 78 | offer_4_volume | DOUBLE |
| 79 | bid_5_price | DOUBLE |
| 80 | bid_5_yield | DOUBLE |
| 81 | bid_5_yield_type | SYMBOL |
| 82 | bid_5_tradable_volume | DOUBLE |
| 83 | bid_5_volume | DOUBLE |
| 84 | offer_5_price | DOUBLE |
| 85 | offer_5_yield | DOUBLE |
| 86 | offer_5_yield_type | SYMBOL |
| 87 | offer_5_tradable_volume | DOUBLE |
| 88 | offer_5_volume | DOUBLE |
| 89 | event_time_trade | TIMESTAMP |
| 90 | receive_time_trade | TIMESTAMP |
| 91 | create_time_trade | TIMESTAMP |
| 92 | event_time_quote | TIMESTAMP |
| 93 | receive_time_quote | TIMESTAMP |
| 94 | create_time_quote | TIMESTAMP |
| 95 | tick_type | SYMBOL |
| 96 | receive_time | TIMESTAMP |
```

## VIII. Delete all crated temporary tables in clean up sub process
1. Temporary tables deletion **MUST** be executed in `CleanSubprocess`
2. Temporary tables deletion **MUST** be implemented by invocation DolphinDB Java API to execute script
3. The script to delete temporary tables **MUST** use the one in `@docs/v6/temporary_table_deletion.dos`
4. You **MUST** read script from resources file at the runtime.

## Reference Documents

Document regarding Tencent COS **FOR YOUR INFORMATION**, you **CAN** also research by yourself.
- https://docs.dolphindb.cn/zh/javadoc/newjava.html
- https://docs.dolphindb.cn/zh/javadoc/install.html
- https://docs.dolphindb.cn/zh/javadoc/quickstart.html
- https://docs.dolphindb.cn/zh/javadoc/java_api_data_types_forms.html
- https://docs.dolphindb.cn/zh/javadoc/data_types_and_forms/scalar.html
- https://docs.dolphindb.cn/zh/javadoc/connect/create.html
- https://docs.dolphindb.cn/zh/javadoc/connect/connect.html
- https://docs.dolphindb.cn/zh/javadoc/connect/login.html
- https://docs.dolphindb.cn/zh/javadoc/connect/run.html
- https://docs.dolphindb.cn/zh/javadoc/connect/upload.html
