package com.sdd.etl.loader.model;

import com.sdd.etl.loader.annotation.ColumnOrder;
import com.sdd.etl.model.TargetDataModel;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Concrete implementation of TargetDataModel for Xbond Quote data.
 * Contains 83 fields for loading to DolphinDB xbond_quote_stream_temp table.
 * This is the target model used by Loader, after transformation from source data.
 */
public class XbondQuoteDataModel extends TargetDataModel {

    @ColumnOrder(1)
    private LocalDate businessDate;

    @ColumnOrder(2)
    private String exchProductId;

    @ColumnOrder(3)
    private String productType;

    @ColumnOrder(4)
    private String exchange;

    @ColumnOrder(5)
    private String source;

    @ColumnOrder(6)
    private int settleSpeed = -1;

    @ColumnOrder(7)
    private String level;

    @ColumnOrder(8)
    private String status;

    @ColumnOrder(9)
    private double preClosePrice = Double.NaN;

    @ColumnOrder(10)
    private double preSettlePrice = Double.NaN;

    @ColumnOrder(11)
    private double preInterest = Double.NaN;

    @ColumnOrder(12)
    private double openPrice = Double.NaN;

    @ColumnOrder(13)
    private double highPrice = Double.NaN;

    @ColumnOrder(14)
    private double lowPrice = Double.NaN;

    @ColumnOrder(15)
    private double closePrice = Double.NaN;

    @ColumnOrder(16)
    private double settlePrice = Double.NaN;

    @ColumnOrder(17)
    private double upperLimit = Double.NaN;

    @ColumnOrder(18)
    private double lowerLimit = Double.NaN;

    @ColumnOrder(19)
    private double totalVolume = Double.NaN;

    @ColumnOrder(20)
    private double totalTurnover = Double.NaN;

    @ColumnOrder(21)
    private double openInterest = Double.NaN;

    @ColumnOrder(22)
    private double bid0Price = Double.NaN;

    @ColumnOrder(23)
    private double bid0Yield = Double.NaN;

    @ColumnOrder(24)
    private String bid0YieldType;

    @ColumnOrder(25)
    private double bid0TradableVolume = Double.NaN;

    @ColumnOrder(26)
    private double bid0Volume = Double.NaN;

    @ColumnOrder(27)
    private double offer0Price = Double.NaN;

    @ColumnOrder(28)
    private double offer0Yield = Double.NaN;

    @ColumnOrder(29)
    private String offer0YieldType;

    @ColumnOrder(30)
    private double offer0TradableVolume = Double.NaN;

    @ColumnOrder(31)
    private double offer0Volume = Double.NaN;

    @ColumnOrder(32)
    private double bid1Price = Double.NaN;

    @ColumnOrder(33)
    private double bid1Yield = Double.NaN;

    @ColumnOrder(34)
    private String bid1YieldType;

    @ColumnOrder(35)
    private double bid1TradableVolume = Double.NaN;

    @ColumnOrder(36)
    private double bid1Volume = Double.NaN;

    @ColumnOrder(37)
    private double offer1Price = Double.NaN;

    @ColumnOrder(38)
    private double offer1Yield = Double.NaN;

    @ColumnOrder(39)
    private String offer1YieldType;

    @ColumnOrder(40)
    private double offer1TradableVolume = Double.NaN;

    @ColumnOrder(41)
    private double offer1Volume = Double.NaN;

    @ColumnOrder(42)
    private double bid2Price = Double.NaN;

    @ColumnOrder(43)
    private double bid2Yield = Double.NaN;

    @ColumnOrder(44)
    private String bid2YieldType;

    @ColumnOrder(45)
    private double bid2TradableVolume = Double.NaN;

    @ColumnOrder(46)
    private double bid2Volume = Double.NaN;

    @ColumnOrder(47)
    private double offer2Price = Double.NaN;

    @ColumnOrder(48)
    private double offer2Yield = Double.NaN;

    @ColumnOrder(49)
    private String offer2YieldType;

    @ColumnOrder(50)
    private double offer2TradableVolume = Double.NaN;

    @ColumnOrder(51)
    private double offer2Volume = Double.NaN;

    @ColumnOrder(52)
    private double bid3Price = Double.NaN;

    @ColumnOrder(53)
    private double bid3Yield = Double.NaN;

    @ColumnOrder(54)
    private String bid3YieldType;

    @ColumnOrder(55)
    private double bid3TradableVolume = Double.NaN;

    @ColumnOrder(56)
    private double bid3Volume = Double.NaN;

    @ColumnOrder(57)
    private double offer3Price = Double.NaN;

    @ColumnOrder(58)
    private double offer3Yield = Double.NaN;

    @ColumnOrder(59)
    private String offer3YieldType;

    @ColumnOrder(60)
    private double offer3TradableVolume = Double.NaN;

    @ColumnOrder(61)
    private double offer3Volume = Double.NaN;

    @ColumnOrder(62)
    private double bid4Price = Double.NaN;

    @ColumnOrder(63)
    private double bid4Yield = Double.NaN;

    @ColumnOrder(64)
    private String bid4YieldType;

    @ColumnOrder(65)
    private double bid4TradableVolume = Double.NaN;

    @ColumnOrder(66)
    private double bid4Volume = Double.NaN;

    @ColumnOrder(67)
    private double offer4Price = Double.NaN;

    @ColumnOrder(68)
    private double offer4Yield = Double.NaN;

    @ColumnOrder(69)
    private String offer4YieldType;

    @ColumnOrder(70)
    private double offer4TradableVolume = Double.NaN;

    @ColumnOrder(71)
    private double offer4Volume = Double.NaN;

    @ColumnOrder(72)
    private double bid5Price = Double.NaN;

    @ColumnOrder(73)
    private double bid5Yield = Double.NaN;

    @ColumnOrder(74)
    private String bid5YieldType;

    @ColumnOrder(75)
    private double bid5TradableVolume = Double.NaN;

    @ColumnOrder(76)
    private double bid5Volume = Double.NaN;

    @ColumnOrder(77)
    private double offer5Price = Double.NaN;

    @ColumnOrder(78)
    private double offer5Yield = Double.NaN;

    @ColumnOrder(79)
    private String offer5YieldType;

    @ColumnOrder(80)
    private double offer5TradableVolume = Double.NaN;

    @ColumnOrder(81)
    private double offer5Volume = Double.NaN;

    @ColumnOrder(82)
    private Instant eventTime;

    @ColumnOrder(83)
    private Instant receiveTime;

    @Override
    public String getDataType() {
        return "XbondQuote";
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public Object toTargetFormat() {
        return null;
    }

    @Override
    public String getTargetType() {
        return "DolphinDB";
    }

    // Getters and Setters
    public LocalDate getBusinessDate() { return businessDate; }
    public void setBusinessDate(LocalDate businessDate) { this.businessDate = businessDate; }

    public String getExchProductId() { return exchProductId; }
    public void setExchProductId(String exchProductId) { this.exchProductId = exchProductId; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public int getSettleSpeed() { return settleSpeed; }
    public void setSettleSpeed(int settleSpeed) { this.settleSpeed = settleSpeed; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getPreClosePrice() { return preClosePrice; }
    public void setPreClosePrice(double preClosePrice) { this.preClosePrice = preClosePrice; }

    public double getPreSettlePrice() { return preSettlePrice; }
    public void setPreSettlePrice(double preSettlePrice) { this.preSettlePrice = preSettlePrice; }

    public double getPreInterest() { return preInterest; }
    public void setPreInterest(double preInterest) { this.preInterest = preInterest; }

    public double getOpenPrice() { return openPrice; }
    public void setOpenPrice(double openPrice) { this.openPrice = openPrice; }

    public double getHighPrice() { return highPrice; }
    public void setHighPrice(double highPrice) { this.highPrice = highPrice; }

    public double getLowPrice() { return lowPrice; }
    public void setLowPrice(double lowPrice) { this.lowPrice = lowPrice; }

    public double getClosePrice() { return closePrice; }
    public void setClosePrice(double closePrice) { this.closePrice = closePrice; }

    public double getSettlePrice() { return settlePrice; }
    public void setSettlePrice(double settlePrice) { this.settlePrice = settlePrice; }

    public double getUpperLimit() { return upperLimit; }
    public void setUpperLimit(double upperLimit) { this.upperLimit = upperLimit; }

    public double getLowerLimit() { return lowerLimit; }
    public void setLowerLimit(double lowerLimit) { this.lowerLimit = lowerLimit; }

    public double getTotalVolume() { return totalVolume; }
    public void setTotalVolume(double totalVolume) { this.totalVolume = totalVolume; }

    public double getTotalTurnover() { return totalTurnover; }
    public void setTotalTurnover(double totalTurnover) { this.totalTurnover = totalTurnover; }

    public double getOpenInterest() { return openInterest; }
    public void setOpenInterest(double openInterest) { this.openInterest = openInterest; }

    public double getBid0Price() { return bid0Price; }
    public void setBid0Price(double bid0Price) { this.bid0Price = bid0Price; }

    public double getBid0Yield() { return bid0Yield; }
    public void setBid0Yield(double bid0Yield) { this.bid0Yield = bid0Yield; }

    public String getBid0YieldType() { return bid0YieldType; }
    public void setBid0YieldType(String bid0YieldType) { this.bid0YieldType = bid0YieldType; }

    public double getBid0TradableVolume() { return bid0TradableVolume; }
    public void setBid0TradableVolume(double bid0TradableVolume) { this.bid0TradableVolume = bid0TradableVolume; }

    public double getBid0Volume() { return bid0Volume; }
    public void setBid0Volume(double bid0Volume) { this.bid0Volume = bid0Volume; }

    public double getOffer0Price() { return offer0Price; }
    public void setOffer0Price(double offer0Price) { this.offer0Price = offer0Price; }

    public double getOffer0Yield() { return offer0Yield; }
    public void setOffer0Yield(double offer0Yield) { this.offer0Yield = offer0Yield; }

    public String getOffer0YieldType() { return offer0YieldType; }
    public void setOffer0YieldType(String offer0YieldType) { this.offer0YieldType = offer0YieldType; }

    public double getOffer0TradableVolume() { return offer0TradableVolume; }
    public void setOffer0TradableVolume(double offer0TradableVolume) { this.offer0TradableVolume = offer0TradableVolume; }

    public double getOffer0Volume() { return offer0Volume; }
    public void setOffer0Volume(double offer0Volume) { this.offer0Volume = offer0Volume; }

    public double getBid1Price() { return bid1Price; }
    public void setBid1Price(double bid1Price) { this.bid1Price = bid1Price; }

    public double getBid1Yield() { return bid1Yield; }
    public void setBid1Yield(double bid1Yield) { this.bid1Yield = bid1Yield; }

    public String getBid1YieldType() { return bid1YieldType; }
    public void setBid1YieldType(String bid1YieldType) { this.bid1YieldType = bid1YieldType; }

    public double getBid1TradableVolume() { return bid1TradableVolume; }
    public void setBid1TradableVolume(double bid1TradableVolume) { this.bid1TradableVolume = bid1TradableVolume; }

    public double getBid1Volume() { return bid1Volume; }
    public void setBid1Volume(double bid1Volume) { this.bid1Volume = bid1Volume; }

    public double getOffer1Price() { return offer1Price; }
    public void setOffer1Price(double offer1Price) { this.offer1Price = offer1Price; }

    public double getOffer1Yield() { return offer1Yield; }
    public void setOffer1Yield(double offer1Yield) { this.offer1Yield = offer1Yield; }

    public String getOffer1YieldType() { return offer1YieldType; }
    public void setOffer1YieldType(String offer1YieldType) { this.offer1YieldType = offer1YieldType; }

    public double getOffer1TradableVolume() { return offer1TradableVolume; }
    public void setOffer1TradableVolume(double offer1TradableVolume) { this.offer1TradableVolume = offer1TradableVolume; }

    public double getOffer1Volume() { return offer1Volume; }
    public void setOffer1Volume(double offer1Volume) { this.offer1Volume = offer1Volume; }

    public double getBid2Price() { return bid2Price; }
    public void setBid2Price(double bid2Price) { this.bid2Price = bid2Price; }

    public double getBid2Yield() { return bid2Yield; }
    public void setBid2Yield(double bid2Yield) { this.bid2Yield = bid2Yield; }

    public String getBid2YieldType() { return bid2YieldType; }
    public void setBid2YieldType(String bid2YieldType) { this.bid2YieldType = bid2YieldType; }

    public double getBid2TradableVolume() { return bid2TradableVolume; }
    public void setBid2TradableVolume(double bid2TradableVolume) { this.bid2TradableVolume = bid2TradableVolume; }

    public double getBid2Volume() { return bid2Volume; }
    public void setBid2Volume(double bid2Volume) { this.bid2Volume = bid2Volume; }

    public double getOffer2Price() { return offer2Price; }
    public void setOffer2Price(double offer2Price) { this.offer2Price = offer2Price; }

    public double getOffer2Yield() { return offer2Yield; }
    public void setOffer2Yield(double offer2Yield) { this.offer2Yield = offer2Yield; }

    public String getOffer2YieldType() { return offer2YieldType; }
    public void setOffer2YieldType(String offer2YieldType) { this.offer2YieldType = offer2YieldType; }

    public double getOffer2TradableVolume() { return offer2TradableVolume; }
    public void setOffer2TradableVolume(double offer2TradableVolume) { this.offer2TradableVolume = offer2TradableVolume; }

    public double getOffer2Volume() { return offer2Volume; }
    public void setOffer2Volume(double offer2Volume) { this.offer2Volume = offer2Volume; }

    public double getBid3Price() { return bid3Price; }
    public void setBid3Price(double bid3Price) { this.bid3Price = bid3Price; }

    public double getBid3Yield() { return bid3Yield; }
    public void setBid3Yield(double bid3Yield) { this.bid3Yield = bid3Yield; }

    public String getBid3YieldType() { return bid3YieldType; }
    public void setBid3YieldType(String bid3YieldType) { this.bid3YieldType = bid3YieldType; }

    public double getBid3TradableVolume() { return bid3TradableVolume; }
    public void setBid3TradableVolume(double bid3TradableVolume) { this.bid3TradableVolume = bid3TradableVolume; }

    public double getBid3Volume() { return bid3Volume; }
    public void setBid3Volume(double bid3Volume) { this.bid3Volume = bid3Volume; }

    public double getOffer3Price() { return offer3Price; }
    public void setOffer3Price(double offer3Price) { this.offer3Price = offer3Price; }

    public double getOffer3Yield() { return offer3Yield; }
    public void setOffer3Yield(double offer3Yield) { this.offer3Yield = offer3Yield; }

    public String getOffer3YieldType() { return offer3YieldType; }
    public void setOffer3YieldType(String offer3YieldType) { this.offer3YieldType = offer3YieldType; }

    public double getOffer3TradableVolume() { return offer3TradableVolume; }
    public void setOffer3TradableVolume(double offer3TradableVolume) { this.offer3TradableVolume = offer3TradableVolume; }

    public double getOffer3Volume() { return offer3Volume; }
    public void setOffer3Volume(double offer3Volume) { this.offer3Volume = offer3Volume; }

    public double getBid4Price() { return bid4Price; }
    public void setBid4Price(double bid4Price) { this.bid4Price = bid4Price; }

    public double getBid4Yield() { return bid4Yield; }
    public void setBid4Yield(double bid4Yield) { this.bid4Yield = bid4Yield; }

    public String getBid4YieldType() { return bid4YieldType; }
    public void setBid4YieldType(String bid4YieldType) { this.bid4YieldType = bid4YieldType; }

    public double getBid4TradableVolume() { return bid4TradableVolume; }
    public void setBid4TradableVolume(double bid4TradableVolume) { this.bid4TradableVolume = bid4TradableVolume; }

    public double getBid4Volume() { return bid4Volume; }
    public void setBid4Volume(double bid4Volume) { this.bid4Volume = bid4Volume; }

    public double getOffer4Price() { return offer4Price; }
    public void setOffer4Price(double offer4Price) { this.offer4Price = offer4Price; }

    public double getOffer4Yield() { return offer4Yield; }
    public void setOffer4Yield(double offer4Yield) { this.offer4Yield = offer4Yield; }

    public String getOffer4YieldType() { return offer4YieldType; }
    public void setOffer4YieldType(String offer4YieldType) { this.offer4YieldType = offer4YieldType; }

    public double getOffer4TradableVolume() { return offer4TradableVolume; }
    public void setOffer4TradableVolume(double offer4TradableVolume) { this.offer4TradableVolume = offer4TradableVolume; }

    public double getOffer4Volume() { return offer4Volume; }
    public void setOffer4Volume(double offer4Volume) { this.offer4Volume = offer4Volume; }

    public double getBid5Price() { return bid5Price; }
    public void setBid5Price(double bid5Price) { this.bid5Price = bid5Price; }

    public double getBid5Yield() { return bid5Yield; }
    public void setBid5Yield(double bid5Yield) { this.bid5Yield = bid5Yield; }

    public String getBid5YieldType() { return bid5YieldType; }
    public void setBid5YieldType(String bid5YieldType) { this.bid5YieldType = bid5YieldType; }

    public double getBid5TradableVolume() { return bid5TradableVolume; }
    public void setBid5TradableVolume(double bid5TradableVolume) { this.bid5TradableVolume = bid5TradableVolume; }

    public double getBid5Volume() { return bid5Volume; }
    public void setBid5Volume(double bid5Volume) { this.bid5Volume = bid5Volume; }

    public double getOffer5Price() { return offer5Price; }
    public void setOffer5Price(double offer5Price) { this.offer5Price = offer5Price; }

    public double getOffer5Yield() { return offer5Yield; }
    public void setOffer5Yield(double offer5Yield) { this.offer5Yield = offer5Yield; }

    public String getOffer5YieldType() { return offer5YieldType; }
    public void setOffer5YieldType(String offer5YieldType) { this.offer5YieldType = offer5YieldType; }

    public double getOffer5TradableVolume() { return offer5TradableVolume; }
    public void setOffer5TradableVolume(double offer5TradableVolume) { this.offer5TradableVolume = offer5TradableVolume; }

    public double getOffer5Volume() { return offer5Volume; }
    public void setOffer5Volume(double offer5Volume) { this.offer5Volume = offer5Volume; }

    public Instant getEventTime() { return eventTime; }
    public void setEventTime(Instant eventTime) { this.eventTime = eventTime; }

    public Instant getReceiveTime() { return receiveTime; }
    public void setReceiveTime(Instant receiveTime) { this.receiveTime = receiveTime; }
}
