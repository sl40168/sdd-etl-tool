package com.sdd.etl.source.extract.cos.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Parsed representation of a single CSV row from source files.
 * Contains all fields from the Xbond Quote CSV format.
 */
public class RawQuoteRecord {

    private Long id;
    private String underlyingSecurityId;
    private Integer underlyingSettlementType;
    private Integer underlyingMdEntryType;
    private Double underlyingMdEntryPx;
    private Integer underlyingMdPriceLevel;
    private Long underlyingMdEntrySize;
    private String underlyingYieldType;
    private Double underlyingYield;
    private LocalDateTime transactTime;
    private Long mqOffset;
    private LocalDateTime recvTime;

    /**
     * Constructs an empty RawQuoteRecord.
     */
    public RawQuoteRecord() {
    }

    /**
     * Constructs a RawQuoteRecord with all fields.
     */
    public RawQuoteRecord(Long id, String underlyingSecurityId, Integer underlyingSettlementType,
                          Integer underlyingMdEntryType, Double underlyingMdEntryPx,
                          Integer underlyingMdPriceLevel, Long underlyingMdEntrySize,
                          String underlyingYieldType, Double underlyingYield,
                          LocalDateTime transactTime, Long mqOffset, LocalDateTime recvTime) {
        this.id = id;
        this.underlyingSecurityId = underlyingSecurityId;
        this.underlyingSettlementType = underlyingSettlementType;
        this.underlyingMdEntryType = underlyingMdEntryType;
        this.underlyingMdEntryPx = underlyingMdEntryPx;
        this.underlyingMdPriceLevel = underlyingMdPriceLevel;
        this.underlyingMdEntrySize = underlyingMdEntrySize;
        this.underlyingYieldType = underlyingYieldType;
        this.underlyingYield = underlyingYield;
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

    public Integer getUnderlyingMdEntryType() {
        return underlyingMdEntryType;
    }

    public void setUnderlyingMdEntryType(Integer underlyingMdEntryType) {
        this.underlyingMdEntryType = underlyingMdEntryType;
    }

    public Double getUnderlyingMdEntryPx() {
        return underlyingMdEntryPx;
    }

    public void setUnderlyingMdEntryPx(Double underlyingMdEntryPx) {
        this.underlyingMdEntryPx = underlyingMdEntryPx;
    }

    public Integer getUnderlyingMdPriceLevel() {
        return underlyingMdPriceLevel;
    }

    public void setUnderlyingMdPriceLevel(Integer underlyingMdPriceLevel) {
        this.underlyingMdPriceLevel = underlyingMdPriceLevel;
    }

    public Long getUnderlyingMdEntrySize() {
        return underlyingMdEntrySize;
    }

    public void setUnderlyingMdEntrySize(Long underlyingMdEntrySize) {
        this.underlyingMdEntrySize = underlyingMdEntrySize;
    }

    public String getUnderlyingYieldType() {
        return underlyingYieldType;
    }

    public void setUnderlyingYieldType(String underlyingYieldType) {
        this.underlyingYieldType = underlyingYieldType;
    }

    public Double getUnderlyingYield() {
        return underlyingYield;
    }

    public void setUnderlyingYield(Double underlyingYield) {
        this.underlyingYield = underlyingYield;
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
     * Checks if this record is valid (required fields non-null).
     * Required: underlyingSecurityId, underlyingMdEntryType (0 or 1),
     * underlyingMdPriceLevel (1-6), transactTime.
     * @return true if valid
     */
    public boolean isValid() {
        if (underlyingSecurityId == null || underlyingSecurityId.trim().isEmpty()) {
            return false;
        }
        if (underlyingMdEntryType == null || (underlyingMdEntryType != 0 && underlyingMdEntryType != 1)) {
            return false;
        }
        if (underlyingMdPriceLevel == null || underlyingMdPriceLevel < 1 || underlyingMdPriceLevel > 6) {
            return false;
        }
        if (transactTime == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RawQuoteRecord that = (RawQuoteRecord) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(underlyingSecurityId, that.underlyingSecurityId) &&
                Objects.equals(underlyingSettlementType, that.underlyingSettlementType) &&
                Objects.equals(underlyingMdEntryType, that.underlyingMdEntryType) &&
                Objects.equals(underlyingMdEntryPx, that.underlyingMdEntryPx) &&
                Objects.equals(underlyingMdPriceLevel, that.underlyingMdPriceLevel) &&
                Objects.equals(underlyingMdEntrySize, that.underlyingMdEntrySize) &&
                Objects.equals(underlyingYieldType, that.underlyingYieldType) &&
                Objects.equals(underlyingYield, that.underlyingYield) &&
                Objects.equals(transactTime, that.transactTime) &&
                Objects.equals(mqOffset, that.mqOffset) &&
                Objects.equals(recvTime, that.recvTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, underlyingSecurityId, underlyingSettlementType,
                underlyingMdEntryType, underlyingMdEntryPx, underlyingMdPriceLevel,
                underlyingMdEntrySize, underlyingYieldType, underlyingYield,
                transactTime, mqOffset, recvTime);
    }

    @Override
    public String toString() {
        return "RawQuoteRecord{" +
                "id=" + id +
                ", underlyingSecurityId='" + underlyingSecurityId + '\'' +
                ", underlyingSettlementType=" + underlyingSettlementType +
                ", underlyingMdEntryType=" + underlyingMdEntryType +
                ", underlyingMdEntryPx=" + underlyingMdEntryPx +
                ", underlyingMdPriceLevel=" + underlyingMdPriceLevel +
                ", underlyingMdEntrySize=" + underlyingMdEntrySize +
                ", underlyingYieldType='" + underlyingYieldType + '\'' +
                ", underlyingYield=" + underlyingYield +
                ", transactTime=" + transactTime +
                ", mqOffset=" + mqOffset +
                ", recvTime=" + recvTime +
                '}';
    }
}
