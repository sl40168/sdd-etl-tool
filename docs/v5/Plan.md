## Phase V: Extract **Bond Future Quote** Data from MySql for ETL

This is phase V for ETL tool. In this phase, we need to extract **Bond Future Quote** data from MySql Database.

What we need to do in this phase include:

## I. Provide an abstract base Database Extractor implementation

1. It **MUST** extend existing Extractor API.
2. It **MUST** be able to read template SQL from INI configure.
3. It **MUST** give opportunity to any concrete implement to fill its own condition to template SQL.
4. To save memory, it **MUST** build `SourceDataModel` data in stream.
5. It returns the `SourceDataModel` results **ONLY WHEN** all data matches condition had been extracted.
6. Empty is acceptable if there is no data in source database with given condition, but it **MUST** be recorded in log.
7. Any failure **MUST** break the whole process and involve user to check manually.
8. The 3rd party component **SHOULD** be considered, such as `SqlTemplate`.

## II. Provide an concrete Extractor for MySql
1. It **MUST** extend the abstract Database Extractor defined above.
2. Its template SQL is 
```sql
select * from bond.fut_tick where trading_date = {BUSINESS_DATE} 
```
3. The `BUSINESS_DATE` is the only filter condition, which is the current data that ETL is processing and from context.
4. The type of `trading_date` in database is **Integer**, and the format is `YYYYMMDD`.
5. Exported data from `bond.fut_tick` is as below
```csv
| id | exchg | code | price | open | high | low | settle_price | upper_limit | lower_limit | total_volume | volume | total_turnover | turn_over | open_interest | diff_interest | trading_date | action_date | action_time | pre_close | pre_settle | pre_interest | bid_prices | ask_prices | bid_qty | ask_qty | receive_time |
----|-------|------|-------|------|------|-----|--------------|------------|-------------|--------------|--------|----------------|-----------|---------------|--------------|-------------|-------------|-------------|-----------|------------|--------------|------------|----------|---------|----------|-------------|
127991794 | CFFEX | T2503 | 109.025 | 109.02 | 109.075 | 109.02 | 0 | 111.085 | 106.735 | 207 | 0 | 225676000 | 0 | 167909 | 0 | 20250102 | 20250102 | 93000400 | 108.925 | 108.91 | 167863 | [109.025, 0, 0, 0, 0] | [109.045, 0, 0, 0, 0] | [52, 0, 0, 0, 0] | [3, 0, 0, 0, 0] | |
127991795 | CFFEX | T2506 | 108.98 | 108.95 | 108.98 | 108.95 | 0 | 110.985 | 106.635 | 11 | 0 | 11985600 | 0 | 20491 | 0 | 20250102 | 20250102 | 93000400 | 108.84 | 108.81 | 20492 | [108.98, 0, 0, 0, 0] | [108.995, 0, 0, 0, 0] | [3, 0, 0, 0, 0] | [3, 0, 0, 0, 0] | |
```
6. **THE MEANING** of columns in database are as below:
   - id: The unique id of the record, which is not business meaning.
   - exchg: The exchange of the bond future.
   - code: The security id of the quote.
   - price: The last trade price of the quoted security.
   - open: The open price of the quoted security today.
   - high: The highest price of the quoted security today.
   - low: The lowest price of the quoted security today.
   - settle_price: The settle price of the quoted security today. In trading time, it's always 0.
   - upper_limit: The upper limit price of the quoted security today.
   - lower_limit: The lower limit price of the quoted security today.
   - total_volume: The total deal volume of the quoted security today in market, the unit is lot.
   - volume: It's always 0 in this source, ignore it.
   - total_turnover: The total deal turnover of the quoted security today in market, the unit is yuan.
   - turn_over: It's always 0 in this source, ignore it.
   - open_interest: The open interest of the quoted security today, the unit is lot.
   - diff_interest: It's always 0 in this source, ignore it.
   - trading_date: Which day the record is for, **NOTE** the value of this field is a number, whose value is calcluated by `YEAR * 10000 + MONTH_OF_YEAR * 100 + DAY_OF_MONTH`.
   - action_date: Which day the trade is deal, **NOTE** the value of this field is a number, whose value is calcluated by `YEAR * 10000 + MONTH_OF_YEAR * 100 + DAY_OF_MONTH`..
   - action_time: What time the trade is deal. **NOTE** the value of this field is a number, whose value is calculated by `HOUR * 10000000 + MINUTE * 100000 + SECOND * 1000 + MILLISECOND`.
   - pre_close: The close price of the quoted security yesterday.
   - pre_settle: The settle price of the quoted security yesterday.
   - pre_interest: The open interest of the quoted security yesterday, the unit is lot.
   - bid_prices: The bid price of the quoted security today, the unit is yuan. **NOTE** the value of this field is a vector, whose value is calculated by `[bid_1_price, bid_2_price, bid_3_price, bid_4_price, bid _5_price]`, but as our source is 'L1', only 'bid_1_price' has valid value, others are all 0.
   - ask_prices: The ask price of the quoted security today, the unit is yuan. **NOTE** the value of this field is a vector, whose value is calculated by `[ask_1_price, ask_2_price, ask_3_price, ask_4_price, ask _5_price]`, but as our source is 'L1', only 'ask_1_price' has valid value, others are all 0.
   - bid_qty: The bid volume of the quoted security today, the unit is lot. **NOTE** the value of this field is a vector, whose value is calculated by `[bid_1_qty, bid_2_qty, bid_3_qty, bid_4_qty, bid _5_qty]`, but as our source is 'L1', only 'bid_1_qty' has valid value, others are all 0. The unit is lot.
   - ask_qty: The ask volume of the quoted security today, the unit is lot. **NOTE** the value of this field is a vector, whose value is calculated by `[ask_1_qty, ask_2_qty, ask_3_qty, ask_4_qty, ask _5_qty]`, but as our source is 'L1', only 'ask_1_qty' has valid value, others are all 0. The unit is lot.
   - receive_time: The timestamp means what time we received this record. The format is "yyyyMMdd HHmmss.SSS" and **NOTE** as this field was added after the first version, it's not mandatory for us.

7. Data Structure of output is as below, which should extend `SourceDataModel`
```csv
| business_date | exch_product_id | product_type | exchange | source | settle_speed | last_trade_price | last_trade_yield | last_trade_yield_type | last_trade_volume | last_trade_turnover | last_trade_interest | last_trade_side | level | status | pre_close_price | pre_settle_price | pre_interest | open_price | high_price | low_price | close_price | settle_price | upper_limit | lower_limit | total_volume | total_turnover | open_interest | bid_0_price | bid_0_yield | bid_0_yield_type | bid_0_tradable_volume | bid_0_volume | offer_0_price | offer_0_yield | offer_0_yield_type | offer_0_tradable_volume | offer_0_volume | bid_1_price | bid_1_yield | bid_1_yield_type | bid_1_tradable_volume | bid_1_volume | offer_1_price | offer_1_yield | offer_1_yield_type | offer_1_tradable_volume | offer_1_volume | bid_2_price | bid_2_yield | bid_2_yield_type | bid_2_tradable_volume | bid_2_volume | offer_2_price | offer_2_yield | offer_2_yield_type | offer_2_tradable_volume | offer_2_volume | bid_3_price | bid_3_yield | bid_3_yield_type | bid_3_tradable_volume | bid_3_volume | offer_3_price | offer_3_yield | offer_3_yield_type | offer_3_tradable_volume | offer_3_volume | bid_4_price | bid_4_yield | bid_4_yield_type | bid_4_tradable_volume | bid_4_volume | offer_4_price | offer_4_yield | offer_4_yield_type | offer_4_tradable_volume | offer_4_volume | bid_5_price | bid_5_yield | bid_5_yield_type | bid_5_tradable_volume | bid_5_volume | offer_5_price | offer_5_yield | offer_5_yield_type | offer_5_tradable_volume | offer_5_volume | event_time_trade | receive_time_trade | create_time_trade | event_time_quote | receive_time_quote | create_time_quote | tick_type | receive_time | create_time |
|:-------------|:----------------|:-------------|:---------|:-------|:-------------|:------------------|:----------------|:---------------------|:-------------------|:---------------------|:-----------------|:-----|:-------|:-----------------|:-----------------|:-------------|:-----------|:-----------|:----------|:------------|:-------------|:------------|:-------------|:--------------|:----------------|:-------------|:------------|:----------------|:-------------------|:----------------------|:---------------|:--------------|:---------------|:-----------------|:---------------------|:-----------------|:-------------|:------------|:-----------------|:----------------------|:---------------|:--------------|:---------------|:-----------------|:---------------------|:-----------------|:-------------|:------------|:-----------------|:----------------------|:---------------|:--------------|:---------------|:-----------------|:---------------------|:-----------------|:-------------|:------------|:-----------------|:----------------------|:---------------|:--------------|:---------------|:-----------------|:---------------------|:-----------------|:-------------|:------------|:-----------------|:----------------------|:---------------|:--------------|:---------------|:-----------------|:---------------------|:-----------------|:-------------|:------------|:-----------------|:----------------------|:---------------|:--------------|:-----------------|:-----------------|:---------------------|:-----------------|:-------------|:-----------------|:-----------------|
| 2026.01.06 | TS2512 | BOND_FUT | CFFEX | CFFEX | 0 | 102.471999999999994 | | | | | | | L1 | Normal | 102.468000000000003 | 102.465999999999993 | 67365 | 102.475999999999999 | 102.481999999999999 | 102.469999999999998 | | 0 | 102.977999999999994 | 101.953999999999993 | 9810 | 2.01059E10 | 66148 | 102.471999999999994 | | | 144 | | 102.474000000000003 | | | 125 | | 102.471999999999994 | | | 144 | | 102.474000000000003 | | | 125 | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | 2026.01.06T11:01:36.300 | 2026.01.06T11:01:36.469 | 2026.01.06T11:01:36.785 | 2026.01.06T11:01:36.300 | 2026.01.06T11:01:36.469 | 2026.01.06T11:01:36.785 | SNAPSHOT | 2026.01.06T11:01:36.469 | |
| 2026.01.06 | TS2603 | BOND_FUT | CFFEX | CFFEX | 0 | 102.424000000000006 | | | | | | | L1 | Normal | 102.415999999999996 | 102.415999999999996 | 16441 | 102.430000000000006 | 102.433999999999997 | 102.418000000000006 | | 0 | 102.927999999999997 | 101.903999999999996 | 3035 | 6.217190000000001E9 | 16813 | 102.421999999999997 | | | 39 | | 102.424000000000006 | | | 20 | | 102.421999999999997 | | | 39 | | 102.424000000000006 | | | 20 | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | | 0 | | 2026.01.06T11:01:36.300 | 2026.01.06T11:01:36.469 | 2026.01.06T11:01:36.787 | 2026.01.06T11:01:36.300 | 2026.01.06T11:01:36.469 | 2026.01.06T11:01:36.787 | SNAPSHOT | 2026.01.06T11:01:36.469 | |
```
8. Each column **MUST** be a field in output. And **THE MEANING** of each field in output and source are as below:
   - business_date: The trading date of the quote happened. It **MUST** be `{BUSINESS_DATE}`.
   - exch_product_id: The security id of the quote, **MUST** source from `code`.
   - product_type: The product type of the quote, **ALWAYS** is 'BOND_FUT'.
   - exchange: The exchange of the bond future, **ALWAYS** is 'CFFEX'.
   - source: The source of the quote, **ALWAYS** is 'CFFEX'.
   - settle_speed: The settle speed of the quote, **ALWAYS** is 0.
   - last_trade_price: The last trade price of the quote, **MUST** source from `price`.
   - level: The level of the quote, **ALWAYS** is 'L1'.
   - status: The status of the quote, **ALWAYS** is 'Normal'.
   - pre_close_price: The close price of the quoted security yesterday, **MUST** source from `pre_close`.
   - pre_settle_price: The settle price of the quoted security yesterday, **MUST** source from `pre_settle`.
   - pre_interest: The open interest of the quoted security yesterday, **MUST** source from `pre_interest`.
   - open_price: The open price of the quoted security today, **MUST** source from`open`.
   - high_price: The high price of the quoted security today, **MUST** source from `high`.
   - low_price: The low price of the quoted security today, **MUST** source from `low`.
   - settle_price: The settle price of the quoted security today, **MUST** source from `settle_price`.
   - upper_limit: The upper limit price of the quoted security today, **MUST** source from `upper_limit`.
   - lower_limit: The lower limit price of the quoted security today, **MUST** source from `lower_limit`.
   - total_volume: The total volume of the quoted security today, **MUST** source from `total_volume`.
   - total_turnover: The total turnover of the quoted security today, **MUST** source from `total_turnover .
   - open_interest: The open interest of the quoted security today, **MUST** source from `open_interest`.
   - bid_1_price: The best tradable bid price for us, **MUST** source from `bid_prices` and pick up the **FIRST ONE**.
   - bid_1_tradable_volume: The best tradable bid volume for us, **MUST** source from `bid_qty` and pick up the **FIRST ONE**.
   - offer_1_price: The best tradable offer price for us, **MUST** source from `ask_prices` and pick up the **FIRST ONE**.
   - offer_1_tradable_volume: The best tradable offer volume for us, **MUST** source from `ask_qty` and pick up the **FIRST ONE**.
   - bid_2_price ~ bid_5_price: **MUST** source from `bid_prices`, but pick up the second to fifth accordingly.
   - offer_2_price ~ offer_5_price: **MUST** source from `ask_prices`, but pick up the second to fifth accordingly.
   - bid_2_tradable_volume ~ bid_5_tradable_volume: **MUST** source from `bid_qty`, but pick up the second to fifth accordingly.
   - offer_2_tradable_volume ~ offer_5_tradable_volume: **MUST** source from `ask_qty`, but pick up the second to fifth accordingly.
   - event_time_trade & event_time_quote: The **TIMESTAMP** of the quote happened, **MUST** source the combination of `action_date` and `action_time`. Please **NOTE** both `action_date` and `action_time` are in number type, so we need to convert them to string type firstly. **NOTE** the special case when `action_time` is earlier than 10am while convert to string.
   - receive_time_trade & receive_time_quote: The **TIMESTAMP** when this quote arrives at our system. It should source from `receive_time`, but if it's empty or does not exist, use the value from `event_time_trade` instead.
   - tick_type: The tick type of the quote, **ALWAYS** is 'SNAPSHOT'.
   - create_time_trade & create_time_quote: The **TIMESTAMP** when this quote is created. Generate it with current timestamp.