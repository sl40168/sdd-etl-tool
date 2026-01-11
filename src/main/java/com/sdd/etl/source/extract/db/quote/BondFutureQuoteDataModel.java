package com.sdd.etl.source.extract.db.quote;

import com.sdd.etl.model.SourceDataModel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Concrete implementation of {@link SourceDataModel} for Bond Future Quote
 * data.
 * 
 * <p>
 * Represents standardized bond future quote records extracted from database.
 * Contains price, volume, limit, and Level 1 market depth information.
 * </p>
 */
public class BondFutureQuoteDataModel extends SourceDataModel {

    // --- Common Fields ---
    private String businessDate;
    private String exchProductId;
    private String productType;
    private String exchange;
    private String source;
    private Integer settleSpeed;
    private String level;
    private String status;

    // --- Price Fields ---
    private Double lastTradePrice;
    private Double openPrice;
    private Double highPrice;
    private Double lowPrice;
    private Double closePrice;
    private Double settlePrice;

    // --- Limit Fields ---
    private Double upperLimit;
    private Double lowerLimit;

    // --- Volume Fields ---
    private Long totalVolume;
    private Double totalTurnover;
    private Long openInterest;

    // --- Level 1 Market Depth ---
    private Double bid1Price;
    private Long bid1Volume;
    private Double offer1Price;
    private Long offer1Volume;

    // --- Timestamps ---
    private LocalDateTime eventTime;
    private LocalDateTime receiveTime;

    /**
     * Constructs a new BondFutureQuoteDataModel with default values.
     */
    public BondFutureQuoteDataModel() {
        super();
        this.productType = "BOND_FUT";
        this.exchange = "CFFEX";
        this.source = "CFFEX";
        this.settleSpeed = 0;
        this.level = "L1";
        this.status = "Normal";

        // Initialize doubles to NaN
        this.lastTradePrice = Double.NaN;
        this.openPrice = Double.NaN;
        this.highPrice = Double.NaN;
        this.lowPrice = Double.NaN;
        this.closePrice = Double.NaN;
        this.settlePrice = Double.NaN;
        this.upperLimit = Double.NaN;
        this.lowerLimit = Double.NaN;
        this.totalTurnover = Double.NaN;

        this.bid1Price = Double.NaN;
        this.offer1Price = Double.NaN;
    }

    /**
     * Validates data integrity and completeness.
     * 
     * <p>
     * Validation rules:
     * <ol>
     * <li>businessDate must be non-null and match pattern YYYY.MM.DD</li>
     * <li>exchProductId must be non-null</li>
     * <li>eventTime must be non-null</li>
     * <li>lastTradePrice must be non-null (warn if NaN but record kept)</li>
     * </ol>
     * 
     * @return true if data is valid, false otherwise
     */
    @Override
    public boolean validate() {
        if (businessDate == null || !businessDate.matches("\\d{4}\\.\\d{2}\\.\\d{2}")) {
            return false;
        }

        if (exchProductId == null || exchProductId.trim().isEmpty()) {
            return false;
        }

        if (eventTime == null) {
            return false;
        }

        return true;
    }

    /**
     * Gets primary key value for this data model.
     * 
     * <p>
     * Primary key: businessDate:exchProductId:eventTime
     * </p>
     * 
     * @return composite primary key string
     */
    @Override
    public Object getPrimaryKey() {
        if (businessDate == null || exchProductId == null || eventTime == null) {
            return null;
        }
        return String.format("%s:%s:%s", businessDate, exchProductId,
                eventTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    /**
     * Gets source type identifier.
     * 
     * @return "bond_future_quote_db"
     */
    @Override
    public String getSourceType() {
        return "bond_future_quote_db";
    }

    // --- Getters and Setters ---

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

    public Integer getSettleSpeed() {
        return settleSpeed;
    }

    public void setSettleSpeed(Integer settleSpeed) {
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

    public Double getLastTradePrice() {
        return lastTradePrice;
    }

    public void setLastTradePrice(Double lastTradePrice) {
        this.lastTradePrice = lastTradePrice;
    }

    public Double getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(Double openPrice) {
        this.openPrice = openPrice;
    }

    public Double getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(Double highPrice) {
        this.highPrice = highPrice;
    }

    public Double getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(Double lowPrice) {
        this.lowPrice = lowPrice;
    }

    public Double getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(Double closePrice) {
        this.closePrice = closePrice;
    }

    public Double getSettlePrice() {
        return settlePrice;
    }

    public void setSettlePrice(Double settlePrice) {
        this.settlePrice = settlePrice;
    }

    public Double getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(Double upperLimit) {
        this.upperLimit = upperLimit;
    }

    public Double getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(Double lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public Long getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(Long totalVolume) {
        this.totalVolume = totalVolume;
    }

    public Double getTotalTurnover() {
        return totalTurnover;
    }

    public void setTotalTurnover(Double totalTurnover) {
        this.totalTurnover = totalTurnover;
    }

    public Long getOpenInterest() {
        return openInterest;
    }

    public void setOpenInterest(Long openInterest) {
        this.openInterest = openInterest;
    }

    public Double getBid1Price() {
        return bid1Price;
    }

    public void setBid1Price(Double bid1Price) {
        this.bid1Price = bid1Price;
    }

    public Long getBid1Volume() {
        return bid1Volume;
    }

    public void setBid1Volume(Long bid1Volume) {
        this.bid1Volume = bid1Volume;
    }

    public Double getOffer1Price() {
        return offer1Price;
    }

    public void setOffer1Price(Double offer1Price) {
        this.offer1Price = offer1Price;
    }

    public Long getOffer1Volume() {
        return offer1Volume;
    }

    public void setOffer1Volume(Long offer1Volume) {
        this.offer1Volume = offer1Volume;
    }

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
}
