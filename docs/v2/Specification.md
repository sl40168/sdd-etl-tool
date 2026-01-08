## Phase 2: Build Data Extractor from COS for ETL

In this phase, we will start to build data extractor from COS data source. COS(Cloud Object Storage) is provided by Tencent and data is stored as csv file format below directories.

What we need to do in this phase include:

## I. Define Extractor API

1. Data Extractor set up connection to source system based on `com.sdd.etl.context.ETLContext`.
2. Data Extractor decides which day of data will be extracted based on `com.sdd.etl.context.ETLContext`, which is `BUSINESS_DATE`.
3. Data Extractor may also need other information from the same context to filter out the source data to extract.
4. The output is a set of extracted data extends `com.sdd.etl.model.SourceDataModel`
5. Except COS, we also have other types of data source, such as MySql and so on. So the Extractor API **MUST NOT** have any detail of special type of data source.

## II. Provide an abstract base COS Extractor implementation

1. We will have multiple data source host in COS, but with different filter pattern and source data structure. But they all follow the same process to extract data out. So a common base Extractor is needed for COS.
2. The process to extract data from COS includes below steps:
    - Filter out all files match the condition, via the API provided by COS
    - Download all matched files to local sub director in working director.
    - Read all single files back memory as raw data
    - Convert all raw data together to a set of `SourceDataModel`, **NOTE** that raw data may not be in the same data structure as `SourceDataModel`
    - Return the set of converted `SourceDataModel` as output
3. To set up the connection to COS, we need below information, which should be provided in INI configure file as an extension of `SourceConfig`:
    - EndPoint
    - Bucket
    - Certification
4. Except `BUSINESS_DATE`, COS needs an additional information `CATEGORY`, which will be provided by concrete extractors, to filter source data.
5. The filter condition of COS is `/CATEGORY/BUSINESS_DATE`, but **NOTE** the format of `BUSINESS_DATE` may be different in concrete extractors.
6. The sub director for download files is `/BUSINESS_DATE/CATEGORY` for any concrete extractor, and the format of `BUSINESS_DATE` **MUST** be `YYYYMMDD`

## III. Provide a concrete implementation for Xbond Quote Data Source

1. Xbond Quote Data **MUST** be extracted by an extension of base COS Extractor
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
    - underlying_security_id: The underlying security id of the product, considering this quote from Cfets Xbond platform, usually we add ".IB" suffix to security Id. For example, "210210" should be "210210.IB".
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
    - recv_time: The receive time of the record, means the time when the record is received by our system.
5. Data Structure of output is as below, which should extend `SourceDataModel`
```csv
| business_date | exch_product_id | product_type | exchange | source | settle_speed | level | status | pre_close_price | pre_settle_price | pre_interest | open_price | high_price | low_price | close_price | settle_price | upper_limit | lower_limit | total_volume | total_turnover | open_interest | bid_0_price | bid_0_yield | bid_0_yield_type | bid_0_tradable_volume | bid_0_volume | offer_0_price | offer_0_yield | offer_0_yield_type | offer_0_tradable_volume | offer_0_volume | bid_1_price | bid_1_yield | bid_1_yield_type | bid_1_tradable_volume | bid_1_volume | offer_1_price | offer_1_yield | offer_1_yield_type | offer_1_tradable_volume | offer_1_volume | bid_2_price | bid_2_yield | bid_2_yield_type | bid_2_tradable_volume | bid_2_volume | offer_2_price | offer_2_yield | offer_2_yield_type | offer_2_tradable_volume | offer_2_volume | bid_3_price | bid_3_yield | bid_3_yield_type | bid_3_tradable_volume | bid_3_volume | offer_3_price | offer_3_yield | offer_3_yield_type | offer_3_tradable_volume | offer_3_volume | bid_4_price | bid_4_yield | bid_4_yield_type | bid_4_tradable_volume | bid_4_volume | offer_4_price | offer_4_yield | offer_4_yield_type | offer_4_tradable_volume | offer_4_volume | bid_5_price | bid_5_yield | bid_5_yield_type | bid_5_tradable_volume | bid_5_volume | offer_5_price | offer_5_yield | offer_5_yield_type | offer_5_tradable_volume | offer_5_volume | event_time | receive_time |
|----------------|-----------------|--------------|----------|--------|--------------|-------|--------|-----------------|-----------------|---------------|------------|------------|-----------|-------------|--------------|------------|------------|---------------|----------------|----------------|-------------|------------|-----------------|-------------------|----------------------|---------------|--------------|---------------|-----------------|---------------------|-----------------|-------------|------------|-----------------|----------------------|---------------|--------------|---------------|-----------------|---------------------|-----------------|-------------|------------|-----------------|----------------------|---------------|--------------|---------------|-----------------|---------------------|-----------------|-------------|------------|-----------------|----------------------|---------------|--------------|---------------|-----------------|---------------------|-----------------|-------------|------------|-----------------|----------------------|---------------|--------------|---------------|-----------------|---------------------|-----------------|-------------|------------|-----------------|----------------------|---------------|--------------|-----------------|-----------------|---------------------|-----------------|-------------|------------|-----------------|----------------------|---------------|--------------|---------------|-----------------|---------------------|-----------------|-----------|-------------|
| 2026.01.06 | 092018002.IB | BOND | CFETS | XBOND | 1 | L2 | Normal | | | | | | | | | | | | 102.1069 | 1.6645 | MATURITY | | 30000000 | 102.2136 | 1.6047 | MATURITY | | 30000000 | 102.1069 | 1.6645 | MATURITY | 30000000 | | 102.2136 | 1.6047 | MATURITY | 30000000 | | 102.097 | 1.6701 | MATURITY | 30000000 | | 102.2216 | 1.6002 | MATURITY | 30000000 | | 102.069 | 1.6858 | MATURITY | 30000000 | | 102.2236 | 1.5991 | MATURITY | 30000000 | | | | | | | | 102.2602 | 1.5786 | MATURITY | 30000000 | | | | | | | | | | | | | 2026.01.06T11:01:33.300 | 2026.01.06T11:01:33.507 |
| 2026.01.06 | 09240422.IB | BOND | CFETS | XBOND | 1 | L2 | Normal | | | | | | | | | | | | 100.1902 | 1.5444 | MATURITY | | 30000000 | 100.2182 | 1.5172 | MATURITY | | 30000000 | 100.1902 | 1.5444 | MATURITY | 30000000 | | 100.2182 | 1.5172 | MATURITY | 30000000 | | 100.1866 | 1.5479 | MATURITY | 50000000 | | 100.2205 | 1.515 | MATURITY | 30000000 | | 100.1844 | 1.55 | MATURITY | 30000000 | | 100.2218 | 1.5137 | MATURITY | 30000000 | | 100.1837 | 1.5507 | MATURITY | 30000000 | | 100.2226 | 1.5129 | MATURITY | 50000000 | | 100.1824 | 1.5519 | MATURITY | 30000000 | | 100.2247 | 1.5109 | MATURITY | 30000000 | | 2026.01.06T11:01:33.300 | 2026.01.06T11:01:33.507 |
```

6. **THE MEANING** of each field in output is as below:
   - business_date: The trading date of the quote happened. It should come from the `{BUSINESS_DATE}` parameter while running the ETL job. Please **NOTE** its data type in DolphinDB is DATE, and you can use `date(temporalParse({BUSINESS_DATE}, 'yyyyMMdd'))` to convert it.
   - exch_product_id: The exchange product id of the quote, which is the security id end with ".IB". For AllPriceDepth, it comes from 'underlying_security_id'
   - product_type: The product type of the quote, which is always "BOND" for AllPriceDepth.
   - exchange: The exchange of the quote, which is always "CFETS" for AllPriceDepth.
   - source: The source of the quote, which is always "XBOND" for AllPriceDepth.
   - settle_speed: The settle speed of the quote, the possible value for AllPriceDepth is 0(day) or 1(day), based on the actual 'underlying_settlement_type' value of quote.
   - level: The level of the quote, which is always "L2" for AllPriceDepth.
   - status: The status of the quote, which is always "Normal" for AllPriceDepth.
   - pre_close_price: Always empty for AllPriceDepth.
   - pre_settle_price: Always empty for AllPriceDepth.
   - pre_interest: Always empty for AllPriceDepth.
   - open_price: Always empty for AllPriceDepth.
   - high_price: Always empty for AllPriceDepth.
   - low_price: Always empty for AllPriceDepth.
   - close_price: Always empty for AllPriceDepth.
   - settle_price: Always empty for AllPriceDepth.
   - upper_limit: Always empty for AllPriceDepth.
   - lower_limit: Always empty for AllPriceDepth.
   - total_volume: Always empty for AllPriceDepth.
   - total_turnover: Always empty for AllPriceDepth.
   - open_interest: Always empty for AllPriceDepth.
   - bid_0_price: The best bid price of the quote crossing global market, but may not be tradable.
   - bid_0_yield: The best bid yield of the quote crossing global market, but may not be tradable.
   - bid_0_yield_type: The yield type of the best bid, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - bid_0_tradable_volume: The tradable volume of the best bid, which is always 0 for AllPriceDepth.
   - bid_0_volume: The volume of the best bid, because it may not be tradable, we put the volume on source quote here.
   - ask_0_price: The best ask price of the quote crossing global market, but may not be tradable.
   - ask_0_yield: The best ask yield of the quote crossing global market, but may not be tradable.
   - ask_0_yield_type: The yield type of the best ask, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - ask_0_tradable_volume: The tradable volume of the best ask, which is always 0 for AllPriceDepth.
   - ask_0_volume: The volume of the best ask, because it may not be tradable, we put the volume on source quote here.
   - bid_1_price: The best bid price which is tradable for us.
   - bid_1_yield: The best bid yield which is tradable for us.
   - bid_1_yield_type: The yield type of the best tradable bid, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - bid_1_tradable_volume: The tradable volume of the best tradable bid, because it is tradable, we put the volume on source quote here.
   - bid_1_volume: The volume of the best tradable bid, keep it empty for AllPriceDepth as duplicated with bid_1_tradable_volume.
   - ask_1_price: The best ask price which is tradable for us.
   - ask_1_yield: The best ask yield which is tradable for us.
   - ask_1_yield_type: The yield type of the best tradable ask, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - ask_1_tradable_volume: The tradable volume of the best tradable ask, because it is tradable, we put the volume on source quote here.
   - ask_1_volume: The volume of the best tradable ask, keep it empty for AllPriceDepth as duplicated with ask_1_tradable_volume.
   - bid_2_price: The second best bid price which is tradable for us.
   - bid_2_yield: The second best bid yield which is tradable for us.
   - bid_2_yield_type: The yield type of the second best tradable bid, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - bid_2_tradable_volume: The tradable volume of the second best tradable bid, because it is tradable, we put the volume on source quote here.
   - bid_2_volume: The volume of the second best tradable bid, keep it empty for AllPriceDepth as duplicated with bid_2_tradable_volume.
   - ask_2_price: The second best ask price which is tradable for us.
   - ask_2_yield: The second best ask yield which is tradable for us.
   - ask_2_yield_type: The yield type of the second best tradable ask, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - ask_2_tradable_volume: The tradable volume of the second best tradable ask, because it is tradable, we put the volume on source quote here.
   - ask_2_volume: The volume of the second best tradable ask, keep it empty for AllPriceDepth as duplicated with ask_2_tradable_volume.
   - bid_3_price: The third best bid price which is tradable for us.
   - bid_3_yield: The third best bid yield which is tradable for us.
   - bid_3_yield_type: The yield type of the third best tradable bid, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - bid_3_tradable_volume: The tradable volume of the third best tradable bid, because it is tradable, we put the volume on source quote here.
   - bid_3_volume: The volume of the third best tradable bid, keep it empty for AllPriceDepth as duplicated with bid_3_tradable_volume.
   - ask_3_price: The third best ask price which is tradable for us.
   - ask_3_yield: The third best ask yield which is tradable for us.
   - ask_3_yield_type: The yield type of the third best tradable ask, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - ask_3_tradable_volume: The tradable volume of the third best tradable ask, because it is tradable, we put the volume on source quote here.
   - ask_3_volume: The volume of the third best tradable ask, keep it empty for AllPriceDepth as duplicated with ask_3_tradable_volume.
   - bid_4_price: The fourth best bid price which is tradable for us.
   - bid_4_yield: The fourth best bid yield which is tradable for us.
   - bid_4_yield_type: The yield type of the fourth best tradable bid, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - bid_4_tradable_volume: The tradable volume of the fourth best tradable bid, because it is tradable, we put the volume on source quote here.
   - bid_4_volume: The volume of the fourth best tradable bid, keep it empty for AllPriceDepth as duplicated with bid_4_tradable_volume.
   - ask_4_price: The fourth best ask price which is tradable for us.
   - ask_4_yield: The fourth best ask yield which is tradable for us.
   - ask_4_yield_type: The yield type of the fourth best tradable ask, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - ask_4_tradable_volume: The tradable volume of the fourth best tradable ask, because it is tradable, we put the volume on source quote here.
   - ask_4_volume: The volume of the fourth best tradable ask, keep it empty for AllPriceDepth as duplicated with ask_4_tradable_volume.
   - bid_5_price: The fifth best bid price which is tradable for us.
   - bid_5_yield: The fifth best bid yield which is tradable for us.
   - bid_5_yield_type: The yield type of the fifth best tradable bid, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - bid_5_tradable_volume: The tradable volume of the fifth best tradable bid, because it is tradable, we put the volume on source quote here.
   - bid_5_volume: The volume of the fifth best tradable bid, keep it empty for AllPriceDepth as duplicated with bid_5_tradable_volume.
   - ask_5_price: The fifth best ask price which is tradable for us.
   - ask_5_yield: The fifth best ask yield which is tradable for us.
   - ask_5_yield_type: The yield type of the fifth best tradable ask, the possible value is MATURITY or EXERCISE(only for the bonds embedding an option).
   - ask_5_tradable_volume: The tradable volume of the fifth best tradable ask, because it is tradable, we put the volume on source quote here.
   - ask_5_volume: The volume of the fifth best tradable ask, keep it empty for AllPriceDepth as duplicated with ask_5_tradable_volume.
   - event_time: The transact_time from the source quote, means what time this quote happened.
   - receive_time: The receive_time from the source quote, means what time we received this quote.
