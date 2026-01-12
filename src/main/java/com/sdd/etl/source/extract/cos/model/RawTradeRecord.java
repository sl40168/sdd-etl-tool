package com.sdd.etl.source.extract.cos.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Parsed representation of a single CSV row from Xbond Trade source files.
 * Contains all fields from the Xbond Trade CSV format.
 */
public class RawTradeRecord {

    private Long id;
    private String underlyingSecurityId;
    private String bondCode;
    private String symbol;
    private LocalDateTime dealTime;
    private String actDt;
    private String actTm;
    private Integer preMarket;
    private Integer tradeMethod;
    private String side;
    private Double netPrice;
    private String setDays;
    private Double yield;
    private String yieldType;
    private Long dealSize;
    private LocalDateTime recvTime;
    private String hlid;

    /**
     * Constructs an empty RawTradeRecord.
     */
    public RawTradeRecord() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUnderlyingSecurityId() {
        return underlyingSecurityId;
    }

    public void setUnderlyingSecurityId(String underlyingSecurityId) {
        this.underlyingSecurityId = underlyingSecurityId;
    }

    public String getBondCode() {
        return bondCode;
    }

    public void setBondCode(String bondCode) {
        this.bondCode = bondCode;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public LocalDateTime getDealTime() {
        return dealTime;
    }

    public void setDealTime(LocalDateTime dealTime) {
        this.dealTime = dealTime;
    }

    public String getActDt() {
        return actDt;
    }

    public void setActDt(String actDt) {
        this.actDt = actDt;
    }

    public String getActTm() {
        return actTm;
    }

    public void setActTm(String actTm) {
        this.actTm = actTm;
    }

    public Integer getPreMarket() {
        return preMarket;
    }

    public void setPreMarket(Integer preMarket) {
        this.preMarket = preMarket;
    }

    public Integer getTradeMethod() {
        return tradeMethod;
    }

    public void setTradeMethod(Integer tradeMethod) {
        this.tradeMethod = tradeMethod;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public Double getNetPrice() {
        return netPrice;
    }

    public void setNetPrice(Double netPrice) {
        this.netPrice = netPrice;
    }

    public String getSetDays() {
        return setDays;
    }

    public void setSetDays(String setDays) {
        this.setDays = setDays;
    }

    public Double getYield() {
        return yield;
    }

    public void setYield(Double yield) {
        this.yield = yield;
    }

    public String getYieldType() {
        return yieldType;
    }

    public void setYieldType(String yieldType) {
        this.yieldType = yieldType;
    }

    public Long getDealSize() {
        return dealSize;
    }

    public void setDealSize(Long dealSize) {
        this.dealSize = dealSize;
    }

    public LocalDateTime getRecvTime() {
        return recvTime;
    }

    public void setRecvTime(LocalDateTime recvTime) {
        this.recvTime = recvTime;
    }

    public String getHlid() {
        return hlid;
    }

    public void setHlid(String hlid) {
        this.hlid = hlid;
    }

    // Backward compatibility getters/setters for old field names
    @Deprecated
    public Double getTradePrice() {
        return netPrice;
    }

    @Deprecated
    public void setTradePrice(Double tradePrice) {
        this.netPrice = tradePrice;
    }

    @Deprecated
    public Double getTradeYield() {
        return yield;
    }

    @Deprecated
    public void setTradeYield(Double tradeYield) {
        this.yield = tradeYield;
    }

    @Deprecated
    public String getTradeYieldType() {
        return yieldType;
    }

    @Deprecated
    public void setTradeYieldType(String tradeYieldType) {
        this.yieldType = tradeYieldType;
    }

    @Deprecated
    public Long getTradeVolume() {
        return dealSize;
    }

    @Deprecated
    public void setTradeVolume(Long tradeVolume) {
        this.dealSize = tradeVolume;
    }

    @Deprecated
    public String getTradeSide() {
        return side;
    }

    @Deprecated
    public void setTradeSide(String tradeSide) {
        this.side = tradeSide;
    }

    @Deprecated
    public LocalDateTime getTransactTime() {
        return dealTime;
    }

    @Deprecated
    public void setTransactTime(LocalDateTime transactTime) {
        this.dealTime = transactTime;
    }

    /**
     * Checks if this record is valid according to raw parsing rules.
     * Required: underlyingSecurityId (non-empty),
     * dealTime (non-null).
     * 
     * @return true if valid
     */
    public boolean isValid() {
        if (underlyingSecurityId == null || underlyingSecurityId.trim().isEmpty()) {
            return false;
        }
        if (dealTime == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RawTradeRecord that = (RawTradeRecord) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(underlyingSecurityId, that.underlyingSecurityId) &&
                Objects.equals(bondCode, that.bondCode) &&
                Objects.equals(symbol, that.symbol) &&
                Objects.equals(dealTime, that.dealTime) &&
                Objects.equals(actDt, that.actDt) &&
                Objects.equals(actTm, that.actTm) &&
                Objects.equals(preMarket, that.preMarket) &&
                Objects.equals(tradeMethod, that.tradeMethod) &&
                Objects.equals(side, that.side) &&
                Objects.equals(netPrice, that.netPrice) &&
                Objects.equals(setDays, that.setDays) &&
                Objects.equals(yield, that.yield) &&
                Objects.equals(yieldType, that.yieldType) &&
                Objects.equals(dealSize, that.dealSize) &&
                Objects.equals(recvTime, that.recvTime) &&
                Objects.equals(hlid, that.hlid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, underlyingSecurityId, bondCode, symbol, dealTime, actDt, actTm,
                preMarket, tradeMethod, side, netPrice, setDays, yield, yieldType, dealSize,
                recvTime, hlid);
    }

    @Override
    public String toString() {
        return "RawTradeRecord{" +
                "id=" + id +
                ", underlyingSecurityId='" + underlyingSecurityId + '\'' +
                ", bondCode='" + bondCode + '\'' +
                ", symbol='" + symbol + '\'' +
                ", dealTime=" + dealTime +
                ", actDt='" + actDt + '\'' +
                ", actTm='" + actTm + '\'' +
                ", preMarket=" + preMarket +
                ", tradeMethod=" + tradeMethod +
                ", side='" + side + '\'' +
                ", netPrice=" + netPrice +
                ", setDays='" + setDays + '\'' +
                ", yield=" + yield +
                ", yieldType='" + yieldType + '\'' +
                ", dealSize=" + dealSize +
                ", recvTime=" + recvTime +
                ", hlid='" + hlid + '\'' +
                '}';
    }
}