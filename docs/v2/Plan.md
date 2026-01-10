## Phase 2: Build Xbond Quote Data Extractor from COS for ETL

In this phase, we will start to build data extractor from COS data source. COS(Cloud Object Storage) is provided by Tencent and data is stored as csv file format below directories.

What we need to do in this phase include:

## I. Define Extractor API

1. Data Extractor set up connection to source system based on `com.sdd.etl.context.ETLContext`.
2. Data Extractor decides which day of data will be extracted based on `com.sdd.etl.context.ETLContext`, which is `BUSINESS_DATE`.
3. Data Extractor may also need other information from the same context to filter out the source data to extract.
4. The output is a set of extracted data extends `com.sdd.etl.model.SourceDataModel`
5. Except COS, we also have other types of data source, such as MySql and so on. So the Extractor API **MUST NOT** have any detail of special type of data source.

## II. Provide an abstract base COS Extractor implementation

1. Each COS Extractor **MUST** be assigned an identification, which is `CATEGORY`
2. The process to extract data from COS includes below steps:
    - Filter out all files match the condition, via the API provided by COS
    - Download all matched files to local sub director in working director, which is `LOCAL_STORAGE`
    - Read all files in `LOCAL_STORAGE` director back memory as raw data
    - Convert all raw data together to a set of `SourceDataModel`, **NOTE** that raw data may not be in the same data structure as `SourceDataModel`
    - Return the set of converted `SourceDataModel` as output
3. To set up the connection to COS, we need below information, which should be provided in INI configure file as an extension of `SourceConfig`:
    - EndPoint
    - Bucket
    - Certification
4. Except `BUSINESS_DATE`, COS needs an additional information `CATEGORY`, which will be provided by concrete extractors, to filter source data.
5. The filter condition of COS is `/CATEGORY/BUSINESS_DATE/*.csv`, but **NOTE** the format of `BUSINESS_DATE` may be different in concrete extractors.
6. The `LOCAL_STORAGE` is `/BUSINESS_DATE/CATEGORY` for any concrete extractor, and the format of `BUSINESS_DATE` **MUST** be `YYYYMMDD`

## III. Provide a concrete implementation for **Xbond Quote** Data Source

1. **Xbond Quote** Data **MUST** be extracted by an extension of base COS Extractor
2. The `CATEGORY` of Xbond Quote is `AllPriceDepth` and the format of `BUSINESS_DATE` is `YYYYMMDD`
3. Data structure of CSV is as below, which is raw data
```csv
id | underlying_symbol | underlying_security_id | underlying_settlement_type | underlying_md_entry_type | underlying_trade_volume | underlying_md_entry_px | underlying_md_price_level | underlying_md_entry_size | underlying_un_match_qty | underlying_yield_type | underlying_yield | transact_time | mq_partition | mq_offset | recv_time |
----|-------------------|------------------------|----------------------------|---------------------------|--------------------------|------------------------|---------------------------|--------------------------|-------------------------|----------------------|-----------------|---------------|--------------|-----------|-----------|
313852591 | - | 210210 | 2 | 0 | | 107.9197 | 1 | 10000000 | | MATURITY | 1.858 | 20260105-09:03:45.377 | 0 | 2926859 | 20260105-09:03:45.421 |
313852592 | - | 210210 | 2 | 1 | | 108.1531 | 1 | 10000000 | | MATURITY | 1.8145 | 20260105-09:03:45.377 | 0 | 2926859 | 20260105-09:03:45.421 |
313852593 | - | 210210 | 2 | 0 | | 107.9197 | 2 | 10000000 | | MATURITY | 1.858 | 20260105-09:03:45.377 | 0 | 2926859 | 20260105-09:03:45.421 |
313852594 | - | 210210 | 2 | 1 | | 108.1531 | 2 | 10000000 | | MATURITY | 1.8145 | 20260105-09:03:45.377 | 0 | 2926859 | 20260105-09:03:45.421 | 
```
4. **THE MEANING** of CSV columns are as below:
    - id: The unique id of the record without any business meaning.
    - underlying_symbol: Always '-', ignore it.
    - underlying_security_id: The underlying security id of the product, ".IB" suffix **MUST** be added if missed. For example, "210210" **MUST** be "210210.IB".
    - underlying_settlement_type: The settlement type of the underlying product which indicate the settle speed of the quote. 1 means settle speed = 0(day), and 2 means settle speed = 1(day).
    - underlying_md_entry_type: The side of the quote, 0 means bid, 1 means offer.
    - underlying_trade_volume: Always empty, ignore it.
    - underlying_md_entry_px: The clean price of the quote.
    - underlying_md_price_level: The level of the quote, from 1 to 6, in which the level 1 is the best quote in global market, but may not be tradable.
    - underlying_md_entry_size: The volume of the quote.
    - underlying_un_match_qty: Always empty, ignore it.
    - underlying_yield_type: The yield type of the underlying product, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
    - underlying_yield: The yield of the underlying product.
    - transact_time: The transact time of the record, means the time when the quote is generated in market.
    - mq_partition: Always 0, ignore it.
    - mq_offset: The offset of the record. The records with the same offset **MUST** be treated as the same quote, but on different level and side.
    - c: The receive time of the record, means the time when the record is received by our system.
5. Data Structure of output is as below, which should extend `SourceDataModel`
```csv
| business_date | exch_product_id | product_type | exchange | source | settle_speed | level | status | pre_close_price | pre_settle_price | pre_interest | open_price | high_price | low_price | close_price | settle_price | upper_limit | lower_limit | total_volume | total_turnover | open_interest | bid_0_price | bid_0_yield | bid_0_yield_type | bid_0_tradable_volume | bid_0_volume | offer_0_price | offer_0_yield | offer_0_yield_type | offer_0_tradable_volume | offer_0_volume | bid_1_price | bid_1_yield | bid_1_yield_type | bid_1_tradable_volume | bid_1_volume | offer_1_price | offer_1_yield | offer_1_yield_type | offer_1_tradable_volume | offer_1_volume | bid_2_price | bid_2_yield | bid_2_yield_type | bid_2_tradable_volume | bid_2_volume | offer_2_price | offer_2_yield | offer_2_yield_type | offer_2_tradable_volume | offer_2_volume | bid_3_price | bid_3_yield | bid_3_yield_type | bid_3_tradable_volume | bid_3_volume | offer_3_price | offer_3_yield | offer_3_yield_type | offer_3_tradable_volume | offer_3_volume | bid_4_price | bid_4_yield | bid_4_yield_type | bid_4_tradable_volume | bid_4_volume | offer_4_price | offer_4_yield | offer_4_yield_type | offer_4_tradable_volume | offer_4_volume | bid_5_price | bid_5_yield | bid_5_yield_type | bid_5_tradable_volume | bid_5_volume | offer_5_price | offer_5_yield | offer_5_yield_type | offer_5_tradable_volume | offer_5_volume | event_time | receive_time |
|----------------|-----------------|--------------|----------|--------|--------------|-------|--------|-----------------|-----------------|---------------|------------|------------|-----------|-------------|--------------|------------|------------|---------------|----------------|----------------|-------------|------------|-----------------|-------------------|----------------------|---------------|--------------|---------------|-----------------|---------------------|-----------------|-------------|------------|-----------------|----------------------|---------------|--------------|---------------|-----------------|---------------------|-----------------|-------------|------------|-----------------|----------------------|---------------|--------------|---------------|-----------------|---------------------|-----------------|-------------|------------|-----------------|----------------------|---------------|--------------|---------------|-----------------|---------------------|-----------------|-------------|------------|-----------------|----------------------|---------------|--------------|---------------|-----------------|---------------------|-----------------|-------------|------------|-----------------|----------------------|---------------|--------------|-----------------|-----------------|---------------------|-----------------|-------------|------------|-----------------|----------------------|---------------|--------------|---------------|-----------------|---------------------|-----------------|-----------|-------------|
| 2026.01.06 | 092018002.IB | BOND | CFETS | XBOND | 1 | L2 | Normal | | | | | | | | | | | | 102.1069 | 1.6645 | MATURITY | | 30000000 | 102.2136 | 1.6047 | MATURITY | | 30000000 | 102.1069 | 1.6645 | MATURITY | 30000000 | | 102.2136 | 1.6047 | MATURITY | 30000000 | | 102.097 | 1.6701 | MATURITY | 30000000 | | 102.2216 | 1.6002 | MATURITY | 30000000 | | 102.069 | 1.6858 | MATURITY | 30000000 | | 102.2236 | 1.5991 | MATURITY | 30000000 | | | | | | | | 102.2602 | 1.5786 | MATURITY | 30000000 | | | | | | | | | | | | | 2026.01.06T11:01:33.300 | 2026.01.06T11:01:33.507 |
| 2026.01.06 | 09240422.IB | BOND | CFETS | XBOND | 1 | L2 | Normal | | | | | | | | | | | | 100.1902 | 1.5444 | MATURITY | | 30000000 | 100.2182 | 1.5172 | MATURITY | | 30000000 | 100.1902 | 1.5444 | MATURITY | 30000000 | | 100.2182 | 1.5172 | MATURITY | 30000000 | | 100.1866 | 1.5479 | MATURITY | 50000000 | | 100.2205 | 1.515 | MATURITY | 30000000 | | 100.1844 | 1.55 | MATURITY | 30000000 | | 100.2218 | 1.5137 | MATURITY | 30000000 | | 100.1837 | 1.5507 | MATURITY | 30000000 | | 100.2226 | 1.5129 | MATURITY | 50000000 | | 100.1824 | 1.5519 | MATURITY | 30000000 | | 100.2247 | 1.5109 | MATURITY | 30000000 | | 2026.01.06T11:01:33.300 | 2026.01.06T11:01:33.507 |
```

6. Each column **MUST** be a field in output. And **THE MEANING** of each field in output and source are as below:
   - business_date: The trading date of the quote happened. It **MUST** be `{BUSINESS_DATE}`.
   - exch_product_id: The exchange product id of the quote, which is the security id end with ".IB". For AllPriceDepth, **MUST** source from **`underlying_security_id`**
   - product_type: The product type of the quote,**MUST** be "BOND" here.
   - exchange: The exchange of the quote, **MUST** be "CFETS".
   - source: The source of the quote, **MUST** be "XBOND".
   - settle_speed: The settle speed of the quote, **MUST** source from **`underlying_settlement_type`** with mapping **1->0, 2->1**.
   - level: The level of the quote, **MUST** be "L2".
   - status: The status of the quote, **MUST** be "Normal".
   - event_time: The transact_time from the source quote, **MUST** source from **`transact_time`**.
   - receive_time: The receive_time from the source quote, means what time we received this quote. **MUST** source from **`transact_time`**.
Below fields **MUST** be converted. The logic will be described shortly.
   - bid_0_price: The best bid price of the quote crossing global market, but may not be tradable.
   - bid_0_yield: The best bid yield of the quote crossing global market, but may not be tradable.
   - bid_0_yield_type: The yield type of the best bid, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - bid_0_volume: The volume of the best bid, because it may not be tradable, we put the volume on source quote here.
   - offer_0_price: The best offer price of the quote crossing global market, but may not be tradable.
   - offer_0_yield: The best offer yield of the quote crossing global market, but may not be tradable.
   - offer_0_yield_type: The yield type of the best offer, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - offer_0_volume: The volume of the best offer, because it may not be tradable, we put the volume on source quote here.
   - bid_1_price: The best bid price which is tradable for us.
   - bid_1_yield: The best bid yield which is tradable for us.
   - bid_1_yield_type: The yield type of the best tradable bid, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - bid_1_tradable_volume: The tradable volume of the best tradable bid, because it is tradable, we put the volume on source quote here.
   - offer_1_price: The best offer price which is tradable for us.
   - offer_1_yield: The best offer yield which is tradable for us.
   - offer_1_yield_type: The yield type of the best tradable offer, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - offer_1_tradable_volume: The tradable volume of the best tradable offer, because it is tradable, we put the volume on source quote here.
   - bid_2_price: The second best bid price which is tradable for us.
   - bid_2_yield: The second best bid yield which is tradable for us.
   - bid_2_yield_type: The yield type of the second best tradable bid, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - bid_2_tradable_volume: The tradable volume of the second best tradable bid, because it is tradable, we put the volume on source quote here.
   - offer_2_price: The second best offer price which is tradable for us.
   - offer_2_yield: The second best offer yield which is tradable for us.
   - offer_2_yield_type: The yield type of the second best tradable offer, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - offer_2_tradable_volume: The tradable volume of the second best tradable offer, because it is tradable, we put the volume on source quote here.
   - bid_3_price: The third best bid price which is tradable for us.
   - bid_3_yield: The third best bid yield which is tradable for us.
   - bid_3_yield_type: The yield type of the third best tradable bid, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - bid_3_tradable_volume: The tradable volume of the third best tradable bid, because it is tradable, we put the volume on source quote here.
   - offer_3_price: The third best offer price which is tradable for us.
   - offer_3_yield: The third best offer yield which is tradable for us.
   - offer_3_yield_type: The yield type of the third best tradable offer, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - offer_3_tradable_volume: The tradable volume of the third best tradable offer, because it is tradable, we put the volume on source quote here.
   - bid_4_price: The fourth best bid price which is tradable for us.
   - bid_4_yield: The fourth best bid yield which is tradable for us.
   - bid_4_yield_type: The yield type of the fourth best tradable bid, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - bid_4_tradable_volume: The tradable volume of the fourth best tradable bid, because it is tradable, we put the volume on source quote here.
   - offer_4_price: The fourth best offer price which is tradable for us.
   - offer_4_yield: The fourth best offer yield which is tradable for us.
   - offer_4_yield_type: The yield type of the fourth best tradable offer, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - offer_4_tradable_volume: The tradable volume of the fourth best tradable offer, because it is tradable, we put the volume on source quote here.
   - bid_5_price: The fifth best bid price which is tradable for us.
   - bid_5_yield: The fifth best bid yield which is tradable for us.
   - bid_5_yield_type: The yield type of the fifth best tradable bid, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - bid_5_tradable_volume: The tradable volume of the fifth best tradable bid, because it is tradable, we put the volume on source quote here.
   - offer_5_price: The fifth best offer price which is tradable for us.
   - offer_5_yield: The fifth best offer yield which is tradable for us.
   - offer_5_yield_type: The yield type of the fifth best tradable offer, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - offer_5_tradable_volume: The tradable volume of the fifth best tradable offer, because it is tradable, we put the volume on source quote here.

The fields not mentioned **MUST** be created but keep null.

7. Extractor needs to convert raw data as below steps:
   - Consolidate raw data from all CSV files together.
   - Group raw data by `mq_offset` firstly, each group **MUST** be converted to one `SourceDataModel` output
   - Then look each element in group and fill output according to below logic: 
     - If `underlying_md_entry_type` = 0 and `underlying_md_price_level` = 1, then `underlying_md_entry_px`->`bid_0_price`, `underlying_yield`->`bid_0_yield`, `underlying_yield_type`->`bid_0_yield_type` and `underlying_md_entry_size`->`bid_0_volume`
     - If `underlying_md_entry_type` = 1 and `underlying_md_price_level` = 1, then `underlying_md_entry_px`->`offer_0_price`, `underlying_yield`->`offer_0_yield`, `underlying_yield_type`->`offer_0_yield_type` and `underlying_md_entry_size`->`offer_0_volume`
     - **NOTE** below mapping are different to above 2 on `underlying_md_entry_size`!!!  
     - If `underlying_md_entry_type` = 0 and `underlying_md_price_level` = 2, then `underlying_md_entry_px`->`bid_1_price`, `underlying_yield`->`bid_1_yield`, `underlying_yield_type`->`bid_1_yield_type` and `underlying_md_entry_size`->`bid_1_tradable_volume`
     - If `underlying_md_entry_type` = 1 and `underlying_md_price_level` = 2, then `underlying_md_entry_px`->`offer_1_price`, `underlying_yield`->`offer_1_yield`, `underlying_yield_type`->`offer_1_yield_type` and `underlying_md_entry_size`->`offer_1_tradable_volume`
     - If `underlying_md_entry_type` = 0 and `underlying_md_price_level` = 3, then `underlying_md_entry_px`->`bid_2_price`, `underlying_yield`->`bid_2_yield`, `underlying_yield_type`->`bid_2_yield_type` and `underlying_md_entry_size`->`bid_2_tradable_volume`
     - If `underlying_md_entry_type` = 1 and `underlying_md_price_level` = 3, then `underlying_md_entry_px`->`offer_2_price`, `underlying_yield`->`offer_2_yield`, `underlying_yield_type`->`offer_2_yield_type` and `underlying_md_entry_size`->`offer_2_tradable_volume`
     - If `underlying_md_entry_type` = 0 and `underlying_md_price_level` = 4, then `underlying_md_entry_px`->`bid_3_price`, `underlying_yield`->`bid_3_yield`, `underlying_yield_type`->`bid_3_yield_type` and `underlying_md_entry_size`->`bid_3_tradable_volume`
     - If `underlying_md_entry_type` = 1 and `underlying_md_price_level` = 4, then `underlying_md_entry_px`->`offer_3_price`, `underlying_yield`->`offer_3_yield`, `underlying_yield_type`->`offer_3_yield_type` and `underlying_md_entry_size`->`offer_3_tradable_volume`
     - If `underlying_md_entry_type` = 0 and `underlying_md_price_level` = 5, then `underlying_md_entry_px`->`bid_4_price`, `underlying_yield`->`bid_4_yield`, `underlying_yield_type`->`bid_4_yield_type` and `underlying_md_entry_size`->`bid_4_tradable_volume`
     - If `underlying_md_entry_type` = 1 and `underlying_md_price_level` = 5, then `underlying_md_entry_px`->`offer_4_price`, `underlying_yield`->`offer_4_yield`, `underlying_yield_type`->`offer_4_yield_type` and `underlying_md_entry_size`->`offer_4_tradable_volume`
     - If `underlying_md_entry_type` = 0 and `underlying_md_price_level` = 6, then `underlying_md_entry_px`->`bid_5_price`, `underlying_yield`->`bid_5_yield`, `underlying_yield_type`->`bid_5_yield_type` and `underlying_md_entry_size`->`bid_5_tradable_volume`
     - If `underlying_md_entry_type` = 1 and `underlying_md_price_level` = 6, then `underlying_md_entry_px`->`offer_5_price`, `underlying_yield`->`offer_5_yield`, `underlying_yield_type`->`offer_5_yield_type` and `underlying_md_entry_size`->`offer_5_tradable_volume`
     - ......

## Reference Documents

Document regarding Tencent COS **FOR YOUR INFORMATION**
- https://doc.fincloud.tencent.cn/tcloud/Storage/COS/402191/876795/java_qstart
- https://doc.fincloud.tencent.cn/tcloud/Storage/COS/402191/876795/java_ifd
