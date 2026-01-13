package com.sdd.etl.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Concrete implementation of {@link SourceDataModel} for Xbond Trade data.
 * 
 * <p>
 * Represents standardized bond trade records extracted from COS CSV files.
 * Contains trade price, yield, volume, trade side, and timestamps.
 * </p>
 */
public class XbondTradeDataModel extends SourceDataModel {

    // --- Common Fields ---
    private String businessDate;
    private String exchProductId;
    private String productType;
    private String exchange;
    private String source;
    private int settleSpeed;
    private String level;
    private String status;

    // --- Timestamps ---
    private LocalDateTime eventTime;
    private LocalDateTime receiveTime;

    // --- Trade-Specific Fields ---
    private double tradePrice;
    private double tradeYield;
    private String tradeYieldType;
    private double tradeVolume;
    private String tradeSide;
    private String tradeId;

    /**
     * Constructs an empty XbondTradeDataModel.
     */
    public XbondTradeDataModel() {
        super();
        this.productType = "BOND";
        this.exchange = "CFETS";
        this.source = "XBOND";
        this.level = "TRADE";
        this.status = "Normal";

        // Initialize Double fields to NaN to indicate unassigned values
        this.tradePrice = Double.NaN;
        this.tradeYield = Double.NaN;
    }

    // --- Abstract Method Implementations ---

    /**
     * Validates data integrity and completeness.
     * 
     * <p>
     * Validation rules:
     * <ol>
     * <li>businessDate must be non-null and match pattern YYYY.MM.DD</li>
     * <li>exchProductId must be non-null and end with ".IB"</li>
     * <li>settleSpeed must be 0 or 1</li>
     * <li>eventTime and receiveTime must be non-null</li>
     * <li>tradeId must be non-null and non-empty</li>
     * <li>At least one of tradePrice, tradeYield, or tradeVolume must be
     * populated</li>
     * <li>If tradePrice is provided and not NaN, must be > 0.0</li>
     * <li>If tradeVolume is provided, must be > 0</li>
     * </ol>
     * 
     * @return true if data is valid, false otherwise
     */
    @Override
    public boolean validate() {
        // Validate required common fields
        if (businessDate == null || !businessDate.matches("\\d{4}\\.\\d{2}\\.\\d{2}")) {
            return false;
        }

        if (exchProductId == null || !exchProductId.endsWith(".IB")) {
            return false;
        }

        if (settleSpeed != 0 && settleSpeed != 1) {
            return false;
        }

        if (eventTime == null || receiveTime == null) {
            return false;
        }

        if (tradeId == null || tradeId.trim().isEmpty()) {
            return false;
        }

        // Validate at least one trade field is populated (non-null and not NaN for
        // Double fields)
        boolean hasTradePrice = !Double.isNaN(tradePrice);
        boolean hasTradeYield = !Double.isNaN(tradeYield);
        boolean hasTradeVolume = !Double.isNaN(tradeVolume);

        if (!hasTradePrice && !hasTradeYield && !hasTradeVolume) {
            return false;
        }

        // Validate business rules
        if (hasTradePrice && tradePrice <= 0.0) {
            return false;
        }

        if (hasTradeVolume && tradeVolume <= 0) {
            return false;
        }

        // Optional consistency validation: eventTime ≤ receiveTime
        if (eventTime.isAfter(receiveTime)) {
            // Could be a warning, but for strict validation we may reject
            // Uncomment below to enforce chronological consistency
            // return false;
        }

        return true;
    }

    /**
     * Gets primary key value for this data model.
     * 
     * <p>
     * Primary key consists of: businessDate + ":" + exchProductId + ":" + tradeId
     * </p>
     * 
     * @return composite primary key string, or null if required fields missing
     */
    @Override
    public Object getPrimaryKey() {
        if (businessDate == null || exchProductId == null || tradeId == null) {
            return null;
        }
        return String.format("%s:%s:%s", businessDate, exchProductId, tradeId);
    }

    /**
     * Gets source type identifier.
     * 
     * @return "xbond_trade_cos"
     */
    @Override
    public String getSourceType() {
        return "xbond_trade_cos";
    }

    // --- Getters and Setters ---

    // Common Fields
    public String getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(String businessDate) {
        this.businessDate = businessDate;
    }

    public String getExchProductId() {
        return exchProductId;
    }

    public void setExchProductId(String exchProductId) {
        this.exchProductId = exchProductId;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getSettleSpeed() {
        return settleSpeed;
    }

    public void setSettleSpeed(int settleSpeed) {
        this.settleSpeed = settleSpeed;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Timestamps
    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public LocalDateTime getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(LocalDateTime receiveTime) {
        this.receiveTime = receiveTime;
    }

    // Trade-Specific Fields
    public double getTradePrice() {
        return tradePrice;
    }

    public void setTradePrice(double tradePrice) {
        this.tradePrice = tradePrice;
    }

    public double getTradeYield() {
        return tradeYield;
    }

    public void setTradeYield(double tradeYield) {
        this.tradeYield = tradeYield;
    }

    public String getTradeYieldType() {
        return tradeYieldType;
    }

    public void setTradeYieldType(String tradeYieldType) {
        this.tradeYieldType = tradeYieldType;
    }

    public double getTradeVolume() {
        return tradeVolume;
    }

    public void setTradeVolume(double tradeVolume) {
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

    // --- Utility Methods ---

    @Override
    public String toString() {
        return "XbondTradeDataModel{" +
                "businessDate='" + businessDate + '\'' +
                ", exchProductId='" + exchProductId + '\'' +
                ", tradeId='" + tradeId + '\'' +
                ", tradePrice=" + tradePrice +
                ", tradeVolume=" + tradeVolume +
                ", eventTime=" + eventTime +
                ", receiveTime=" + receiveTime +
                '}';
    }
}