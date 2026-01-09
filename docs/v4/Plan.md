## Phase 4: Build **Xbond Trade** Data Extractor from COS for ETL

In this phase, we need **Xbond Trade** data extractor from COS data source.

What we need to do in this phase include:

## I. Concrete **Xbond Trade** Extractor base on existing COS Extractor

1. It **MUST** extend the same base COS Extractor as existing **Xbond Quote** Data Extractor.
2. Its `CATEGORY` is **XbondCfetsDeal**
3. Its date format of `BUSINESS_DATE` is **YYYY-MM-DD**
4. Data structure of CSV is as below, which is raw data
```csv
 id | bond_key | bond_code | symbol | deal_time | act_dt | act_tm | pre_market | trade_method | side | net_price | set_days | yield | yield_type | deal_size | recv_time | hlid |
----|----------|-----------|---------|-----------|---------|---------|------------|--------------|------|-----------|----------|-------|------------|-----------|-----------|-------------|
11568725 | 250210.IB | 250210 | 25国开10 | 2026-01-05 10:07:45.068 | 20260105 | 100745068 | 0 | 3 | Y | 98.4289 | T+1 | 1.9875 | 1 | 5000 | 2026-01-05 10:07:45.102 | 4455380029616468 |
11577382 | 250210.IB | 250210 | 25国开10 | 2026-01-05 13:57:55.352 | 20260105 | 135755352 | 0 | 3 | Y | 98.4082 | T+1 | 1.99 | 1 | 5000 | 2026-01-05 13:57:55.384 | 4455380029893492 |
11590145 | 250210.IB | 250210 | 25国开10 | 2026-01-05 15:50:54.350 | 20260105 | 155054350 | 0 | 3 | Y | 98.3668 | T+1 | 1.995 | 1 | 3000 | 2026-01-05 15:50:54.385 | 4455380030301908 |
```
5. **THE MEANING** of CSV columns are as below:
   - id: The unique id of the record, there is no business meaning.
   - bond_key: The security id of the bond.
   - bond_code: The code of the bond, considering we already have 'bond_key', which is end with ".IB", ignore this field.
   - symbol: The short name of the bond, ignore it as well.
   - deal_time: The time of the deal happened, which should be mapped to 'event_time'. The format is "yyyy-MM-dd HH:mm:ss.SSS"
   - act_dt: The date of the deal happened, considering we already have 'deal_time', ignore this field
   - act_tm: The time of the deal happened, considering we already have 'deal_time', ignore this field.
   - pre_market: Always 0, ignore it.
   - trade_method: The method of the deal, ignore it.
   - side: The last trade side of the deal.
   - net_price: The clean price of the deal.
   - set_days: The settle speed of the deal.
   - yield: The yield of the deal.
   - yield_type: The yield type of the deal.
   - deal_size: The volume of the deal.
   - recv_time: The timestamp means what time we received this record, which should be mapped to 'receive_time'. The format is "yyyy-MM-dd HH:mm:ss.SSS" and **NOTE** as this field was added after the first version, it's not mandatory for us.
   - hlid: The unique id of the deal, ignore it.
6. Data Structure of output is as below, which should extend `SourceDataModel`
```csv
business_date | exch_product_id | product_type | exchange | source | settle_speed | last_trade_price | last_trade_yield | last_trade_yield_type | last_trade_volume | last_trade_turnover | last_trade_interest | last_trade_side | event_time | receive_time |
---------------|-----------------|--------------|----------|--------|--------------|------------------|-----------------|-----------------------|-------------------|---------------------|---------------------|-----------------|------------|--------------|
2026.01.06 | 250210.IB | BOND | CFETS | XBOND | 1 | 99.912 | 1.8096 | MATURITY | 10000000 | | | TKN | 2026.01.06T11:01:34.079 | 2026.01.06T11:01:34.246 |
2026.01.06 | 230023.IB | BOND | CFETS | XBOND | 1 | 122.8227 | 1.945 | MATURITY | 20000000 | | | TKN | 2026.01.06T11:01:36.085 | 2026.01.06T11:01:36.255 |
2026.01.06 | 230023.IB | BOND | CFETS | XBOND | 1 | 122.8227 | 1.945 | MATURITY | 20000000 | | | TKN | 2026.01.06T11:01:36.085 | 2026.01.06T11:01:36.258 |
```
7. Each column **MUST** be a field in output. And **THE MEANING** of each field in output and source are as below:
   - business_date: The trading date of the quote happened. It **MUST** be `{BUSINESS_DATE}`.
   - exch_product_id: The security id of the bond. **MUST** source from **'bond_key'** directly.
   - product_type: The product type of the bond, which is **ALWAYS** 'BOND'.
   - exchange: The exchange of the bond, which is **ALWAYS** 'CFETS'.
   - source: The source of the deal, which is **ALWAYS** 'XBOND'.
   - settle_speed: The settle speed of the deal, **MUST** source from **'set_days'** directly.
   - last_trade_price: The clean price of the deal, **MUST** source from 'net_price' directly.
   - last_trade_yield: The yield of the deal, **MUST** source from 'yield' directly.
   - last_trade_yield_type: The yield type of the deal, **MUST** source from 'yield_type' directly.
   - last_trade_volume: The volume of the deal, **MUST** source from 'deal_size' directly.
   - last_trade_turnover: The turnover of the deal, **ALWAYS** key null.
   - last_trade_interest: The interest of the deal, **ALWAYS** key null.
   - last_trade_side: The last trade side of the deal, **MUST** source from 'side' **WITH FOLLOWING MAPPING**: 'X'->'TKN', 'Y'->'GVN','Z'->'TRD', 'D'->'DONE'. 
   - event_time: The time of the deal happened, **MUST** source from 'deal_time' as LocalDate.
   - receive_time: The timestamp means what time we received this record, source from **'recv_time'** if it's not null as LocalDate. **OTHERWISE**, copy from above `event_time`.
