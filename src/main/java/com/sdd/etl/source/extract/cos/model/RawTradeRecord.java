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
    private Integer underlyingSettlementType;
    private Double tradePrice;
    private Double tradeYield;
    private String tradeYieldType;
    private Long tradeVolume;
    private String tradeSide;
    private String tradeId;
    private LocalDateTime transactTime;
    private Long mqOffset;
    private LocalDateTime recvTime;

    /**
     * Constructs an empty RawTradeRecord.
     */
    public RawTradeRecord() {
    }

    /**
     * Constructs a RawTradeRecord with all fields.
     */
    public RawTradeRecord(Long id, String underlyingSecurityId, Integer underlyingSettlementType,
            Double tradePrice, Double tradeYield, String tradeYieldType,
            Long tradeVolume, String tradeSide, String tradeId,
            LocalDateTime transactTime, Long mqOffset, LocalDateTime recvTime) {
        this.id = id;
        this.underlyingSecurityId = underlyingSecurityId;
        this.underlyingSettlementType = underlyingSettlementType;
        this.tradePrice = tradePrice;
        this.tradeYield = tradeYield;
        this.tradeYieldType = tradeYieldType;
        this.tradeVolume = tradeVolume;
        this.tradeSide = tradeSide;
        this.tradeId = tradeId;
        this.transactTime = transactTime;
        this.mqOffset = mqOffset;
        this.recvTime = recvTime;
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

    public Integer getUnderlyingSettlementType() {
        return underlyingSettlementType;
    }

    public void setUnderlyingSettlementType(Integer underlyingSettlementType) {
        this.underlyingSettlementType = underlyingSettlementType;
    }

    public Double getTradePrice() {
        return tradePrice;
    }

    public void setTradePrice(Double tradePrice) {
        this.tradePrice = tradePrice;
    }

    public Double getTradeYield() {
        return tradeYield;
    }

    public void setTradeYield(Double tradeYield) {
        this.tradeYield = tradeYield;
    }

    public String getTradeYieldType() {
        return tradeYieldType;
    }

    public void setTradeYieldType(String tradeYieldType) {
        this.tradeYieldType = tradeYieldType;
    }

    public Long getTradeVolume() {
        return tradeVolume;
    }

    public void setTradeVolume(Long tradeVolume) {
        this.tradeVolume = tradeVolume;
    }

    public String getTradeSide() {
        return tradeSide;
    }

    public void setTradeSide(String tradeSide) {
        this.tradeSide = tradeSide;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public LocalDateTime getTransactTime() {
        return transactTime;
    }

    public void setTransactTime(LocalDateTime transactTime) {
        this.transactTime = transactTime;
    }

    public Long getMqOffset() {
        return mqOffset;
    }

    public void setMqOffset(Long mqOffset) {
        this.mqOffset = mqOffset;
    }

    public LocalDateTime getRecvTime() {
        return recvTime;
    }

    public void setRecvTime(LocalDateTime recvTime) {
        this.recvTime = recvTime;
    }

    /**
     * Checks if this record is valid according to raw parsing rules.
     * Required: underlyingSecurityId (non-empty),
     * underlyingSettlementType (0 or 1),
     * transactTime (non-null).
     * 
     * @return true if valid
     */
    public boolean isValid() {
        if (underlyingSecurityId == null || underlyingSecurityId.trim().isEmpty()) {
            return false;
        }
        if (underlyingSettlementType == null || (underlyingSettlementType != 0 && underlyingSettlementType != 1)) {
            return false;
        }
        if (transactTime == null) {
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
                Objects.equals(underlyingSettlementType, that.underlyingSettlementType) &&
                Objects.equals(tradePrice, that.tradePrice) &&
                Objects.equals(tradeYield, that.tradeYield) &&
                Objects.equals(tradeYieldType, that.tradeYieldType) &&
                Objects.equals(tradeVolume, that.tradeVolume) &&
                Objects.equals(tradeSide, that.tradeSide) &&
                Objects.equals(tradeId, that.tradeId) &&
                Objects.equals(transactTime, that.transactTime) &&
                Objects.equals(mqOffset, that.mqOffset) &&
                Objects.equals(recvTime, that.recvTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, underlyingSecurityId, underlyingSettlementType,
                tradePrice, tradeYield, tradeYieldType, tradeVolume,
                tradeSide, tradeId, transactTime, mqOffset, recvTime);
    }

    @Override
    public String toString() {
        return "RawTradeRecord{" +
                "id=" + id +
                ", underlyingSecurityId='" + underlyingSecurityId + '\'' +
                ", underlyingSettlementType=" + underlyingSettlementType +
                ", tradePrice=" + tradePrice +
                ", tradeYield=" + tradeYield +
                ", tradeYieldType='" + tradeYieldType + '\'' +
                ", tradeVolume=" + tradeVolume +
                ", tradeSide='" + tradeSide + '\'' +
                ", tradeId='" + tradeId + '\'' +
                ", transactTime=" + transactTime +
                ", mqOffset=" + mqOffset +
                ", recvTime=" + recvTime +
                '}';
    }
}