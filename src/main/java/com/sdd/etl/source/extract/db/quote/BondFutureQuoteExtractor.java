package com.sdd.etl.source.extract.db.quote;

import com.sdd.etl.model.SourceDataModel;
import com.sdd.etl.source.extract.db.DatabaseExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Concrete extractor for Bond Future Quote data from MySQL.
 * 
 * <p>
 * SQL: select * from bond.fut_tick where trading_date = {BUSINESS_DATE}
 * </p>
 */
public class BondFutureQuoteExtractor extends DatabaseExtractor {

    private static final Logger logger = LoggerFactory.getLogger(BondFutureQuoteExtractor.class);
    // Spec format: "yyyyMMdd HHmmss.SSS" e.g. "20231201 093000.500"
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss.SSS");
    private static final DateTimeFormatter SHORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Override
    public String getCategory() {
        return "BondFutureQuote";
    }

    @Override
    public String getName() {
        return "BondFutureQuoteExtractor";
    }

    @Override
    protected String getSqlTemplateConfigKey() {
        return "sql.template";
    }

    @Override
    protected SourceDataModel mapRow(ResultSet rs) throws SQLException {
        BondFutureQuoteDataModel model = new BondFutureQuoteDataModel();

        // Common Fields
        // business_date from SQL is INTEGER YYYYMMDD, convert to YYYY.MM.DD
        int tradingDate = rs.getInt("trading_date");
        model.setBusinessDate(formatDate(tradingDate));

        // exch_product_id <- code
        model.setExchProductId(rs.getString("code"));

        // Prices
        model.setLastTradePrice(getDouble(rs, "price"));
        model.setOpenPrice(getDouble(rs, "open"));
        model.setHighPrice(getDouble(rs, "high"));
        model.setLowPrice(getDouble(rs, "low"));
        model.setClosePrice(getDouble(rs, "pre_close"));
        model.setSettlePrice(getDouble(rs, "settle_price"));

        // Limits
        model.setUpperLimit(getDouble(rs, "upper_limit"));
        model.setLowerLimit(getDouble(rs, "lower_limit"));

        // Volumes
        model.setTotalVolume(rs.getLong("total_volume"));
        model.setTotalTurnover(getDouble(rs, "total_turnover"));
        model.setOpenInterest(rs.getLong("open_interest"));

        // L1 Depth (Parsing vector string "[val, 0, 0, 0, 0]")
        String bidPrices = rs.getString("bid_prices");
        String askPrices = rs.getString("ask_prices");
        String bidQtys = rs.getString("bid_qty");
        String askQtys = rs.getString("ask_qty");

        model.setBid1Price(parseVectorFirstValue(bidPrices));
        model.setOffer1Price(parseVectorFirstValue(askPrices));
        model.setBid1Volume(parseVectorFirstLong(bidQtys));
        model.setOffer1Volume(parseVectorFirstLong(askQtys));

        // Timestamps
        int actionDate = rs.getInt("action_date");
        int actionTime = rs.getInt("action_time"); // HHMHmmSSS

        LocalDateTime eventTime = parseEventTime(actionDate, actionTime);
        model.setEventTime(eventTime);

        // receive_time logic: Use receive_time col, if empty use eventTime
        String receiveTimeStr = rs.getString("receive_time");
        if (receiveTimeStr != null && !receiveTimeStr.trim().isEmpty()) {
            try {
                // Spec says "yyyyMMdd HHmmss.SSS"
                model.setReceiveTime(LocalDateTime.parse(receiveTimeStr, TIME_FORMATTER));
            } catch (Exception e) {
                // Fallback to eventTime if parsing fails
                logger.warn("Failed to parse receive_time '{}', falling back to event_time.", receiveTimeStr);
                model.setReceiveTime(eventTime);
            }
        } else {
            model.setReceiveTime(eventTime);
        }

        return model;
    }

    // Helper to format YYYYMMDD int to YYYY.MM.DD string
    private String formatDate(int dateInt) {
        String s = String.valueOf(dateInt);
        if (s.length() != 8)
            return null;
        return s.substring(0, 4) + "." + s.substring(4, 6) + "." + s.substring(6, 8);
    }

    // Helper to get Double, handling 0 as 0.0, but keeping field semantics.
    // Spec says 'price' is Double.
    private Double getDouble(ResultSet rs, String col) throws SQLException {
        double v = rs.getDouble(col);
        if (rs.wasNull())
            return Double.NaN;
        return v;
    }

    /**
     * Parses vector string like "[109.025, 0, 0, 0, 0]" and returns first element.
     */
    private Double parseVectorFirstValue(String vector) {
        if (vector == null || !vector.startsWith("[") || !vector.endsWith("]")) {
            return Double.NaN;
        }
        try {
            String content = vector.substring(1, vector.length() - 1); // remove [ ]
            String[] parts = content.split(",");
            if (parts.length > 0) {
                return Double.parseDouble(parts[0].trim());
            }
        } catch (NumberFormatException e) {
            // log?
        }
        return Double.NaN;
    }

    private Long parseVectorFirstLong(String vector) {
        Double d = parseVectorFirstValue(vector);
        if (Double.isNaN(d))
            return 0L;
        return d.longValue();
    }

    /**
     * Parses action_date (YYYYMMDD) and action_time (HHMMSSmmm) into LocalDateTime.
     */
    private LocalDateTime parseEventTime(int date, int time) {
        // time is HHMHmmSSS (9 digits) or HMMSSmmm (8 digits) or less?
        // Spec: HOUR * 10000000 + MINUTE * 100000 + SECOND * 1000 + MILLISECOND
        // Example: 93000400 -> 09:30:00.400 -> 93000400
        // If hour is 9, it's 930... (8 digits). If 15, 1530... (9 digits).

        String dStr = String.valueOf(date);
        String tStr = String.valueOf(time);

        // Pad time to 9 digits with leading zeros if needed?
        // 93000400 -> 093000400
        while (tStr.length() < 9) {
            tStr = "0" + tStr;
        }

        String dtStr = dStr + tStr;
        try {
            return LocalDateTime.parse(dtStr, SHORT_TIME_FORMATTER);
            // format: yyyyMMddHHmmssSSS -> 8 + 9 = 17 chars
        } catch (Exception e) {
            logger.error("Failed to parse date {} time {}", date, time);
            return null;
        }
    }
}
