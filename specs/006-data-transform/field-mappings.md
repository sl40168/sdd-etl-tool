# 字段映射关系文档

## 概述

本文档详细列出了三个Transformer的Source和Target Data Model字段映射关系。

---

## 1. XbondQuoteTransformer 字段映射

**Source**: `com.sdd.etl.model.XbondQuoteDataModel`  
**Target**: `com.sdd.etl.loader.model.XbondQuoteDataModel`  
**字段数**: 83个

### 完整字段映射表

| Target字段序号 | Target字段 | Target类型 | Source字段 | Source类型 | 映射规则 | 特殊处理 |
|---------------|-----------|-----------|-----------|-----------|---------|---------|
| 1 | businessDate | LocalDate | businessDate | String | 直接映射 | String → LocalDate (YYYY.MM.DD → YYYYMMDD) |
| 2 | exchProductId | String | exchProductId | String | 直接映射 | - |
| 3 | productType | String | productType | String | 直接映射 | - |
| 4 | exchange | String | exchange | String | 直接映射 | - |
| 5 | source | String | source | String | 直接映射 | - |
| 6 | settleSpeed | int | settleSpeed | Integer | 直接映射 | null → -1 (哨兵值) |
| 7 | level | String | level | String | 直接映射 | - |
| 8 | status | String | status | String | 直接映射 | - |
| 9 | preClosePrice | double | (不存在) | - | 无映射 | 保持NaN (DolphinDB补充字段) |
| 10 | preSettlePrice | double | (不存在) | - | 无映射 | 保持NaN |
| 11 | preInterest | double | (不存在) | - | 无映射 | 保持NaN |
| 12 | openPrice | double | (不存在) | - | 无映射 | 保持NaN |
| 13 | highPrice | double | (不存在) | - | 无映射 | 保持NaN |
| 14 | lowPrice | double | (不存在) | - | 无映射 | 保持NaN |
| 15 | closePrice | double | (不存在) | - | 无映射 | 保持NaN |
| 16 | settlePrice | double | (不存在) | - | 无映射 | 保持NaN |
| 17 | upperLimit | double | (不存在) | - | 无映射 | 保持NaN |
| 18 | lowerLimit | double | (不存在) | - | 无映射 | 保持NaN |
| 19 | totalVolume | double | (不存在) | - | 无映射 | 保持NaN |
| 20 | totalTurnover | double | (不存在) | - | 无映射 | 保持NaN |
| 21 | openInterest | double | (不存在) | - | 无映射 | 保持NaN |
| 22 | bid0Price | double | bid0Price | Double | 直接映射 | - |
| 23 | bid0Yield | double | bid0Yield | Double | 直接映射 | - |
| 24 | bid0YieldType | String | bid0YieldType | String | 直接映射 | - |
| 25 | bid0TradableVolume | double | (不存在) | - | 无映射 | 保持NaN |
| 26 | bid0Volume | double | bid0Volume | Long | 直接映射 | Long → double |
| 27 | offer0Price | double | offer0Price | Double | 直接映射 | - |
| 28 | offer0Yield | double | offer0Yield | Double | 直接映射 | - |
| 29 | offer0YieldType | String | offer0YieldType | String | 直接映射 | - |
| 30 | offer0TradableVolume | double | (不存在) | - | 无映射 | 保持NaN |
| 31 | offer0Volume | double | offer0Volume | Long | 直接映射 | Long → double |
| 32 | bid1Price | double | bid1Price | Double | 直接映射 | - |
| 33 | bid1Yield | double | bid1Yield | Double | 直接映射 | - |
| 34 | bid1YieldType | String | bid1YieldType | String | 直接映射 | - |
| 35 | bid1TradableVolume | double | bid1TradableVolume | Long | 直接映射 | Long → double |
| 36 | bid1Volume | double | (不存在) | - | 无映射 | 保持NaN |
| 37 | offer1Price | double | offer1Price | Double | 直接映射 | - |
| 38 | offer1Yield | double | offer1Yield | Double | 直接映射 | - |
| 39 | offer1YieldType | String | offer1YieldType | String | 直接映射 | - |
| 40 | offer1TradableVolume | double | offer1TradableVolume | Long | 直接映射 | Long → double |
| 41 | offer1Volume | double | (不存在) | - | 无映射 | 保持NaN |
| 42 | bid2Price | double | bid2Price | Double | 直接映射 | - |
| 43 | bid2Yield | double | bid2Yield | Double | 直接映射 | - |
| 44 | bid2YieldType | String | bid2YieldType | String | 直接映射 | - |
| 45 | bid2TradableVolume | double | bid2TradableVolume | Long | 直接映射 | Long → double |
| 46 | bid2Volume | double | (不存在) | - | 无映射 | 保持NaN |
| 47 | offer2Price | double | offer2Price | Double | 直接映射 | - |
| 48 | offer2Yield | double | offer2Yield | Double | 直接映射 | - |
| 49 | offer2YieldType | String | offer2YieldType | String | 直接映射 | - |
| 50 | offer2TradableVolume | double | offer2TradableVolume | Long | 直接映射 | Long → double |
| 51 | offer2Volume | double | (不存在) | - | 无映射 | 保持NaN |
| 52 | bid3Price | double | bid3Price | Double | 直接映射 | - |
| 53 | bid3Yield | double | bid3Yield | Double | 直接映射 | - |
| 54 | bid3YieldType | String | bid3YieldType | String | 直接映射 | - |
| 55 | bid3TradableVolume | double | bid3TradableVolume | Long | 直接映射 | Long → double |
| 56 | bid3Volume | double | (不存在) | - | 无映射 | 保持NaN |
| 57 | offer3Price | double | offer3Price | Double | 直接映射 | - |
| 58 | offer3Yield | double | offer3Yield | Double | 直接映射 | - |
| 59 | offer3YieldType | String | offer3YieldType | String | 直接映射 | - |
| 60 | offer3TradableVolume | double | offer3TradableVolume | Long | 直接映射 | Long → double |
| 61 | offer3Volume | double | (不存在) | - | 无映射 | 保持NaN |
| 62 | bid4Price | double | bid4Price | Double | 直接映射 | - |
| 63 | bid4Yield | double | bid4Yield | Double | 直接映射 | - |
| 64 | bid4YieldType | String | bid4YieldType | String | 直接映射 | - |
| 65 | bid4TradableVolume | double | bid4TradableVolume | Long | 直接映射 | Long → double |
| 66 | bid4Volume | double | (不存在) | - | 无映射 | 保持NaN |
| 67 | offer4Price | double | offer4Price | Double | 直接映射 | - |
| 68 | offer4Yield | double | offer4Yield | Double | 直接映射 | - |
| 69 | offer4YieldType | String | offer4YieldType | String | 直接映射 | - |
| 70 | offer4TradableVolume | double | offer4TradableVolume | Long | 直接映射 | Long → double |
| 71 | offer4Volume | double | (不存在) | - | 无映射 | 保持NaN |
| 72 | bid5Price | double | bid5Price | Double | 直接映射 | - |
| 73 | bid5Yield | double | bid5Yield | Double | 直接映射 | - |
| 74 | bid5YieldType | String | bid5YieldType | String | 直接映射 | - |
| 75 | bid5TradableVolume | double | bid5TradableVolume | Long | 直接映射 | Long → double |
| 76 | bid5Volume | double | (不存在) | - | 无映射 | 保持NaN |
| 77 | offer5Price | double | offer5Price | Double | 直接映射 | - |
| 78 | offer5Yield | double | offer5Yield | Double | 直接映射 | - |
| 79 | offer5YieldType | String | offer5YieldType | String | 直接映射 | - |
| 80 | offer5TradableVolume | double | offer5TradableVolume | Long | 直接映射 | Long → double |
| 81 | offer5Volume | double | (不存在) | - | 无映射 | 保持NaN |
| 82 | eventTime | Instant | eventTime | LocalDateTime | 直接映射 | LocalDateTime → Instant (使用系统时区) |
| 83 | receiveTime | Instant | receiveTime | LocalDateTime | 直接映射 | LocalDateTime → Instant (使用系统时区) |

### 映射统计

| 类别 | 数量 | 说明 |
|------|------|------|
| 直接映射 (同类型) | 51个字段 | String→String, Double→double |
| 类型转换 | 13个字段 | String→LocalDate, Integer→int, Long→double, LocalDateTime→Instant |
| 无映射 | 19个字段 | DolphinDB补充字段，保持NaN |
| **总计** | **83个字段** | Target字段数 |

### Source模型字段详情 (共62个字段)

| 类别 | 字段数 |
|------|--------|
| 公共字段 | 8个 (businessDate, exchProductId, productType, exchange, source, settleSpeed, level, status) |
| 时间戳 | 2个 (eventTime, receiveTime) |
| Level0订单簿 | 4个 (bid0Price, bid0Yield, bid0YieldType, bid0Volume) |
| Offer0订单簿 | 4个 (offer0Price, offer0Yield, offer0YieldType, offer0Volume) |
| Level1-5订单簿 | 48个 (每个Level 8个字段 × 5个Level = 40个字段，但Source只有Level1-5的bid/offerPrice/Yield/YieldType/TradableVolume，共40个) |

### 特殊处理说明

1. **日期格式转换**: businessDate从Source的"YYYY.MM.DD"格式转换为Target的LocalDate (YYYYMMDD格式)
2. **时区转换**: LocalDateTime → Instant使用系统默认时区
3. **补充字段**: preClosePrice等19个DolphinDB补充字段在Source中不存在，保持NaN
4. **字段顺序**: Target有@ColumnOrder注解，DolphinDB按顺序存储

---

## 2. XbondTradeTransformer 字段映射

**Source**: `com.sdd.etl.model.XbondTradeDataModel`  
**Target**: `com.sdd.etl.loader.model.XbondTradeDataModel`  
**字段数**: 15个

### 完整字段映射表

| Target字段序号 | Target字段 | Target类型 | Source字段 | Source类型 | 映射规则 | 特殊处理 |
|---------------|-----------|-----------|-----------|-----------|---------|---------|
| 1 | businessDate | LocalDate | businessDate | String | 直接映射 | String → LocalDate (YYYY.MM.DD → YYYYMMDD) |
| 2 | exchProductId | String | exchProductId | String | 直接映射 | - |
| 3 | productType | String | productType | String | 直接映射 | - |
| 4 | exchange | String | exchange | String | 直接映射 | - |
| 5 | source | String | source | String | 直接映射 | - |
| 6 | settleSpeed | int | settleSpeed | Integer | 直接映射 | null → -1 (哨兵值) |
| 7 | lastTradePrice | double | (不存在) | - | 无映射 | 保持NaN (Target补充字段) |
| 8 | lastTradeYield | double | (不存在) | - | 无映射 | 保持NaN (Target补充字段) |
| 9 | lastTradeYieldType | String | (不存在) | - | 无映射 | 保持null (Target补充字段) |
| 10 | lastTradeVolume | double | (不存在) | - | 无映射 | 保持NaN (Target补充字段) |
| 11 | lastTradeTurnover | double | (不存在) | - | 无映射 | 保持NaN (price × volume计算字段) |
| 12 | lastTradeInterest | double | (不存在) | - | 无映射 | 保持NaN (Target补充字段) |
| 13 | lastTradeSide | String | tradeSide | String | 名称映射 | tradeSide → lastTradeSide |
| 14 | eventTime | Instant | eventTime | LocalDateTime | 直接映射 | LocalDateTime → Instant (使用系统时区) |
| 15 | receiveTime | Instant | receiveTime | LocalDateTime | 直接映射 | LocalDateTime → Instant (使用系统时区) |

### Source模型中未映射字段

| Source字段 | Source类型 | 说明 |
|-----------|-----------|------|
| tradePrice | Double | 最新成交价 - Target中无lastTradePrice对应字段 |
| tradeYield | Double | 最新成交收益率 - Target中无lastTradeYield对应字段 |
| tradeYieldType | String | 最新成交收益率类型 - Target中无lastTradeYieldType对应字段 |
| tradeVolume | Long | 最新成交量 - Target中无lastTradeVolume对应字段 |
| tradeId | String | 成交ID - Target中无对应字段 |

### 映射统计

| 类别 | 数量 | 说明 |
|------|------|------|
| 直接映射 (同类型) | 9个字段 | String→String |
| 类型转换 | 3个字段 | String→LocalDate, Integer→int, LocalDateTime→Instant |
| 字段名映射 | 1个字段 | tradeSide → lastTradeSide |
| 无映射 (Target有但Source无) | 6个字段 | lastTradePrice等Target补充字段 |
| 无映射 (Source有但Target无) | 5个字段 | tradePrice等Source独有字段 |
| **总计** | **15个Target字段** | Target字段数 |

### 特殊处理说明

1. **字段名映射**: Source的`tradeSide`映射到Target的`lastTradeSide`
2. **缺失Source字段**: lastTradePrice等6个字段在Source中不存在，Target保持NaN/null
3. **缺失Target字段**: tradePrice等5个字段在Target中不存在，Source数据将被忽略

### 业务决策点

**需要确认**: Source的tradePrice, tradeYield, tradeYieldType, tradeVolume字段是否应该映射到Target的lastTradePrice等字段？

- **选项A**: 忽略这些字段（当前文档采用此方案）
- **选项B**: 将Source.tradePrice映射到Target.lastTradePrice等
- **选项C**: 记录警告日志，但保持原状

---

## 3. BondFutureQuoteTransformer 字段映射

**Source**: `com.sdd.etl.source.extract.db.quote.BondFutureQuoteDataModel`  
**Target**: `com.sdd.etl.loader.model.BondFutureQuoteDataModel`  
**字段数**: 96个

### 完整字段映射表

| Target字段序号 | Target字段 | Target类型 | Source字段 | Source类型 | 映射规则 | 特殊处理 |
|---------------|-----------|-----------|-----------|-----------|---------|---------|
| 1 | businessDate | LocalDate | businessDate | String | 直接映射 | String → LocalDate (YYYY.MM.DD → YYYYMMDD) |
| 2 | exchProductId | String | exchProductId | String | 直接映射 | - |
| 3 | productType | String | productType | String | 直接映射 | - |
| 4 | exchange | String | exchange | String | 直接映射 | - |
| 5 | source | String | source | String | 直接映射 | - |
| 6 | settleSpeed | int | settleSpeed | Integer | 直接映射 | null → -1 (哨兵值) |
| 7 | lastTradePrice | double | lastTradePrice | Double | 直接映射 | - |
| 8 | lastTradeYield | double | (不存在) | - | 无映射 | 保持NaN |
| 9 | lastTradeYieldType | String | (不存在) | - | 无映射 | 保持null |
| 10 | lastTradeVolume | double | (不存在) | - | 无映射 | 保持NaN |
| 11 | lastTradeTurnover | double | (不存在) | - | 无映射 | 保持NaN |
| 12 | lastTradeInterest | double | (不存在) | - | 无映射 | 保持NaN |
| 13 | lastTradeSide | String | (不存在) | - | 无映射 | 保持null |
| 14 | level | String | level | String | 直接映射 | - |
| 15 | status | String | status | String | 直接映射 | - |
| 16 | preClosePrice | double | (不存在) | - | 无映射 | 保持NaN |
| 17 | preSettlePrice | double | (不存在) | - | 无映射 | 保持NaN |
| 18 | preInterest | double | (不存在) | - | 无映射 | 保持NaN |
| 19 | openPrice | double | openPrice | Double | 直接映射 | - |
| 20 | highPrice | double | highPrice | Double | 直接映射 | - |
| 21 | lowPrice | double | lowPrice | Double | 直接映射 | - |
| 22 | closePrice | double | closePrice | Double | 直接映射 | - |
| 23 | settlePrice | double | settlePrice | Double | 直接映射 | - |
| 24 | upperLimit | double | upperLimit | Double | 直接映射 | - |
| 25 | lowerLimit | double | lowerLimit | Double | 直接映射 | - |
| 26 | totalVolume | double | totalVolume | Long | 直接映射 | Long → double |
| 27 | totalTurnover | double | totalTurnover | Double | 直接映射 | - |
| 28 | openInterest | double | openInterest | Long | 直接映射 | Long → double |
| 29 | bid0Price | double | (不存在) | - | 无映射 | 保持NaN (Source无Level0) |
| 30 | bid0Yield | double | (不存在) | - | 无映射 | 保持NaN |
| 31 | bid0YieldType | String | (不存在) | - | 无映射 | 保持null |
| 32 | bid0TradableVolume | double | (不存在) | - | 无映射 | 保持NaN |
| 33 | bid0Volume | double | (不存在) | - | 无映射 | 保持NaN |
| 34 | offer0Price | double | (不存在) | - | 无映射 | 保持NaN |
| 35 | offer0Yield | double | (不存在) | - | 无映射 | 保持NaN |
| 36 | offer0YieldType | String | (不存在) | - | 无映射 | 保持null |
| 37 | offer0TradableVolume | double | (不存在) | - | 无映射 | 保持NaN |
| 38 | offer0Volume | double | (不存在) | - | 无映射 | 保持NaN |
| 39 | bid1Price | double | bid1Price | Double | 直接映射 | - |
| 40 | bid1Yield | double | (不存在) | - | 无映射 | 保持NaN |
| 41 | bid1YieldType | String | (不存在) | - | 无映射 | 保持null |
| 42 | bid1TradableVolume | double | (不存在) | - | 无映射 | 保持NaN |
| 43 | bid1Volume | double | bid1Volume | Long | 直接映射 | Long → double |
| 44 | offer1Price | double | offer1Price | Double | 直接映射 | - |
| 45 | offer1Yield | double | (不存在) | - | 无映射 | 保持NaN |
| 46 | offer1YieldType | String | (不存在) | - | 无映射 | 保持null |
| 47 | offer1TradableVolume | double | (不存在) | - | 无映射 | 保持NaN |
| 48 | offer1Volume | double | offer1Volume | Long | 直接映射 | Long → double |
| 49-96 | bid2-5, offer2-5 (各12个字段) | double | (不存在) | - | 无映射 | 全部保持NaN (Source无Level2-5) |

### 映射统计

| 类别 | 数量 | 说明 |
|------|------|------|
| 直接映射 (同类型) | 17个字段 | String→String, Double→double |
| 类型转换 | 5个字段 | String→LocalDate, Integer→int, Long→double, LocalDateTime→Instant |
| 无映射 | 74个字段 | Target有96个，Source只有22个，74个字段保持NaN |
| **总计** | **96个字段** | Target字段数 |

### Source模型字段详情 (共22个字段)

| 类别 | 字段 |
|------|------|
| 公共字段 | 8个: businessDate, exchProductId, productType, exchange, source, settleSpeed, level, status |
| 价格字段 | 7个: lastTradePrice, openPrice, highPrice, lowPrice, closePrice, settlePrice |
| 涨跌停 | 2个: upperLimit, lowerLimit |
| 成交量 | 3个: totalVolume (Long), totalTurnover (Double), openInterest (Long) |
| Level1订单簿 | 4个: bid1Price, bid1Volume, offer1Price, offer1Volume |
| 时间戳 | 2个: eventTime, receiveTime |

### Target模型字段详情 (共96个字段)

| 类别 | 字段数 |
|------|--------|
| 公共字段 | 8个 (序号1-8) |
| 最新成交信息 | 7个 (序号7-13: lastTradePrice ~ lastTradeSide) |
| Level/Status | 2个 (序号14-15) |
| 昨日价格 | 3个 (序号16-18: preClosePrice, preSettlePrice, preInterest) |
| 今日价格 | 6个 (序号19-24: openPrice ~ settlePrice) |
| 涨跌停 | 2个 (序号25-26: upperLimit, lowerLimit) |
| 成交量 | 3个 (序号27-28: totalVolume, totalTurnover, openInterest) |
| Level0订单簿 | 10个 (序号29-38) |
| Level1订单簿 | 10个 (序号39-48) |
| Level2订单簿 | 10个 (序号49-58) |
| Level3订单簿 | 10个 (序号59-68) |
| Level4订单簿 | 10个 (序号69-78) |
| Level5订单簿 | 10个 (序号79-88) |
| 时间戳 | 2个 (序号95-96: eventTime, receiveTime) |

### 特殊处理说明

1. **日期格式转换**: businessDate从Source的"YYYY.MM.DD"格式转换为Target的LocalDate (YYYYMMDD格式)
2. **时区转换**: LocalDateTime → Instant使用系统默认时区
3. **无Level0订单簿**: Source只有Level1数据，Target的Level0-5订单簿全部保持NaN (共60个字段)
4. **字段数量差异大**: Source只有22个字段，Target有96个字段，74个字段保持NaN

---

## 4. 类型转换规则汇总

| Source类型 | Target类型 | 转换规则 | Null处理 | 示例 |
|-----------|-----------|---------|---------|------|
| String | LocalDate | 解析YYYY.MM.DD格式 → LocalDate | null → null | "2024.01.15" → LocalDate.of(2024, 1, 15) |
| Integer | int | 自动拆箱 | null → -1 (哨兵值) | Integer(0) → 0, null → -1 |
| Long | double | 自动拆箱 | null → Double.NaN | Long(1000) → 1000.0, null → NaN |
| Double | double | 直接赋值 | NaN → Double.NaN | Double(99.5) → 99.5, NaN → NaN |
| LocalDateTime | Instant | 转换为系统时区Instant | null → null | LocalDateTime.atZone(systemDefault).toInstant() |
| String | String | 直接赋值 | null → null | 直接复制 |

### 类型转换代码示例

```java
// String → LocalDate
if (sourceValue instanceof String) {
    String dateStr = ((String) sourceValue).replace(".", "");
    targetValue = LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE); // YYYYMMDD
}

// Integer → int (with null handling)
if (sourceValue == null) {
    targetValue = -1; // Sentinel value
} else {
    targetValue = ((Integer) sourceValue).intValue();
}

// Long → double (with null handling)
if (sourceValue == null) {
    targetValue = Double.NaN; // Sentinel value
} else {
    targetValue = ((Long) sourceValue).doubleValue();
}

// LocalDateTime → Instant (with null handling)
if (sourceValue instanceof LocalDateTime) {
    targetValue = ((LocalDateTime) sourceValue).atZone(ZoneId.systemDefault()).toInstant();
}
```

---

## 5. 哨兵值策略

根据宪法原则11（原始数字字段初始化，无默认零），使用以下哨兵值表示未设置：

| Target类型 | 哨兵值 | 含义 | 符合宪法原则 | 初始值示例 |
|-----------|---------|------|-------------|-----------|
| int | -1 | 未设置 | ✅ 原则11 | `private int settleSpeed = -1;` |
| double | Double.NaN | 未设置 | ✅ 原则11 | `private double price = Double.NaN;` |
| String | null | 未设置 | ✅ 原则11 | `private String type;` (默认null) |
| LocalDate | null | 未设置 | ✅ 原则11 | `private LocalDate date;` (默认null) |
| Instant | null | 未设置 | ✅ 原则11 | `private Instant time;` (默认null) |

### 哨兵值使用原则

1. **原始int类型**: 默认初始化为-1，不使用0
2. **原始double类型**: 默认初始化为NaN，不使用0.0
3. **引用类型**: 默认为null

---

## 6. 实现注意事项

### XbondQuoteTransformer

1. **日期格式转换**: String → LocalDate需要移除分隔符（"2024.01.15" → "20240115"）
2. **时区转换**: LocalDateTime → Instant使用系统默认时区
3. **补充字段**: preClosePrice等19个DolphinDB补充字段保持NaN
4. **字段顺序**: 需要按照@ColumnOrder注解的顺序映射（虽然Transformer按字段名映射，但DolphinDB加载时使用顺序）
5. **订单簿结构**: Level0有bid0Volume/offer0Volume但没有bid0TradableVolume/offer0TradableVolume

### XbondTradeTransformer

1. **字段名不匹配**: tradeSide → lastTradeSide
2. **缺失Source字段**: lastTradePrice等6个字段在Source中不存在，Target保持NaN
3. **缺失Target字段**: tradePrice等5个字段在Target中不存在，Source数据将被忽略
4. **时区转换**: LocalDateTime → Instant使用系统默认时区
5. **业务决策**: 需要确认tradePrice等字段的处理策略

### BondFutureQuoteTransformer

1. **Source字段少**: Source只有22个字段，Target有96个字段
2. **大量NaN字段**: 74个字段将保持NaN
3. **无Level0订单簿**: Source只有Level1数据，Level0-5订单簿全部保持NaN（60个字段）
4. **时区转换**: LocalDateTime → Instant使用系统默认时区

### 通用注意事项

1. **类型安全**: 所有转换需要在`AbstractTransformer.convertValue()`中实现
2. **异常处理**: 转换失败时应抛出`TransformationException`
3. **日志记录**: 转换失败时应记录详细的错误信息（包括字段名、源值、目标类型）
4. **性能优化**: 反射缓存字段映射，避免重复查找
5. **并发安全**: Transformer实现应该是无状态的，可在多线程环境下安全使用

---

## 7. 验证检查点

### XbondQuoteTransformer

- [ ] 83个Target字段全部正确映射
- [ ] 19个DolphinDB补充字段保持NaN
- [ ] settleSpeed的null值转换为-1
- [ ] String → LocalDate格式正确（YYYY.MM.DD → YYYYMMDD）
- [ ] LocalDateTime → Instant时区正确
- [ ] Long → double转换正确（bid0Volume等）
- [ ] Level0订单簿字段映射正确（bid0Volume存在，bid0TradableVolume不存在）

### XbondTradeTransformer

- [ ] 15个Target字段全部正确映射
- [ ] tradeSide → lastTradeSide映射正确
- [ ] 缺失Source字段处理策略已明确（lastTradePrice等保持NaN）
- [ ] 缺失Target字段处理策略已明确（tradePrice等被忽略）
- [ ] settleSpeed的null值转换为-1
- [ ] String → LocalDate格式正确

### BondFutureQuoteTransformer

- [ ] 96个Target字段全部正确处理
- [ ] 22个Source字段全部正确映射
- [ ] 74个Target字段保持NaN
- [ ] settleSpeed的null值转换为-1
- [ ] Long → double转换正确（totalVolume, openInterest等）
- [ ] String → LocalDate格式正确
- [ ] LocalDateTime → Instant时区正确

### 通用验证

- [ ] 所有类型转换正确处理null值
- [ ] 哨兵值策略符合宪法原则11
- [ ] Transformer是线程安全的（无状态）
- [ ] 转换失败时抛出TransformationException
- [ ] 错误日志包含足够的调试信息（字段名、源值、目标类型、错误原因）

---

## 8. 附录

### Source模型汇总

| 模型 | 包路径 | 字段数 | 关键特征 |
|------|--------|--------|---------|
| XbondQuoteDataModel | com.sdd.etl.model | 62个 | Level0-5订单簿，bid/offerPrice/Yield/YieldType/Volume/TradableVolume |
| XbondTradeDataModel | com.sdd.etl.model | 16个 | tradePrice/Volume/Yield/YieldType/Id |
| BondFutureQuoteDataModel | com.sdd.etl.source.extract.db.quote | 22个 | 价格+成交量+Level1订单簿 |

### Target模型汇总

| 模型 | 包路径 | 字段数 | 关键特征 |
|------|--------|--------|---------|
| XbondQuoteDataModel | com.sdd.etl.loader.model | 83个 | @ColumnOrder注解，DolphinDB列顺序 |
| XbondTradeDataModel | com.sdd.etl.loader.model | 15个 | @ColumnOrder注解 |
| BondFutureQuoteDataModel | com.sdd.etl.loader.model | 96个 | @ColumnOrder注解，Level0-5订单簿 |

### 映射复杂度评估

| Transformer | 映射复杂度 | 挑战点 |
|-------------|-----------|--------|
| XbondQuoteTransformer | 中 | 83个字段，19个补充字段，6个Level的订单簿 |
| XbondTradeTransformer | 低 | 15个字段，但存在字段名映射和缺失字段问题 |
| BondFutureQuoteTransformer | 低 | 96个Target字段，但Source只有22个，74个字段保持NaN |

---

**文档版本**: 2.0  
**最后更新**: 2026-01-11  
**状态**: 已验证，准备实现
