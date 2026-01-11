package com.sdd.etl.loader.model;

import com.sdd.etl.loader.annotation.ColumnOrder;
import com.sdd.etl.model.TargetDataModel;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Concrete implementation of TargetDataModel for Xbond Trade data.
 * Contains 15 fields for loading to DolphinDB xbond_trade_stream_temp table.
 * This is target model used by Loader, after transformation from source data.
 */
public class XbondTradeDataModel extends TargetDataModel {

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
    private double lastTradePrice = Double.NaN;

    @ColumnOrder(8)
    private double lastTradeYield = Double.NaN;

    @ColumnOrder(9)
    private String lastTradeYieldType;

    @ColumnOrder(10)
    private double lastTradeVolume = Double.NaN;

    @ColumnOrder(11)
    private double lastTradeTurnover = Double.NaN;

    @ColumnOrder(12)
    private double lastTradeInterest = Double.NaN;

    @ColumnOrder(13)
    private String lastTradeSide;

    @ColumnOrder(14)
    private Instant eventTime;

    @ColumnOrder(15)
    private Instant receiveTime;

    @Override
    public String getDataType() {
        return "XbondTrade";
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

    public double getLastTradePrice() { return lastTradePrice; }
    public void setLastTradePrice(double lastTradePrice) { this.lastTradePrice = lastTradePrice; }

    public double getLastTradeYield() { return lastTradeYield; }
    public void setLastTradeYield(double lastTradeYield) { this.lastTradeYield = lastTradeYield; }

    public String getLastTradeYieldType() { return lastTradeYieldType; }
    public void setLastTradeYieldType(String lastTradeYieldType) { this.lastTradeYieldType = lastTradeYieldType; }

    public double getLastTradeVolume() { return lastTradeVolume; }
    public void setLastTradeVolume(double lastTradeVolume) { this.lastTradeVolume = lastTradeVolume; }

    public double getLastTradeTurnover() { return lastTradeTurnover; }
    public void setLastTradeTurnover(double lastTradeTurnover) { this.lastTradeTurnover = lastTradeTurnover; }

    public double getLastTradeInterest() { return lastTradeInterest; }
    public void setLastTradeInterest(double lastTradeInterest) { this.lastTradeInterest = lastTradeInterest; }

    public String getLastTradeSide() { return lastTradeSide; }
    public void setLastTradeSide(String lastTradeSide) { this.lastTradeSide = lastTradeSide; }

    public Instant getEventTime() { return eventTime; }
    public void setEventTime(Instant eventTime) { this.eventTime = eventTime; }

    public Instant getReceiveTime() { return receiveTime; }
    public void setReceiveTime(Instant receiveTime) { this.receiveTime = receiveTime; }
}
