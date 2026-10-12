package sbdev.joy_ai_project.pattern_strategy_algo.model;

import com.motivewave.platform.sdk.common.Enums;

public class CurrentTradeState {
    public double entryPrice;
    public double takeProfitPrice;
    public double stopLossPrice;
    public double takeProfitPoint;
    public double stopLossPoint;
    public long entryTimestamp;
    public boolean isLong;
    public Enums.EntryState entryState;
    public PatternRenkoMatched entryPattern;
    public String tradeId;
    public long tradeDbId; // id_trade (trade_strategy_live)

    public CurrentTradeState(double entryPrice, double takeProfitPrice, double stopLossPrice, double takeProfitPoint,
            double stopLossPoint, boolean isLong, PatternRenkoMatched pattern, String tradeId) {
        this.entryPrice = entryPrice;
        this.takeProfitPrice = takeProfitPrice;
        this.stopLossPrice = stopLossPrice;
        this.takeProfitPoint = takeProfitPoint;
        this.stopLossPoint = stopLossPoint;
        this.isLong = isLong;
        this.entryPattern = pattern;
        this.entryState = Enums.EntryState.WAITING_ENTRY;
        this.entryTimestamp = System.currentTimeMillis();
        this.tradeId = tradeId;
    }

    /** Représentation lisible pour les logs/debug. Inclut une date locale ISO. */
    @Override
    public String toString() {
        String tsIso;
        try {
            tsIso = java.time.ZonedDateTime
                    .ofInstant(java.time.Instant.ofEpochMilli(entryTimestamp), java.time.ZoneId.systemDefault())
                    .toString();
        } catch (Throwable t) {
            tsIso = String.valueOf(entryTimestamp);
        }

        return "CurrentTradeState{" + "entryPrice=" + entryPrice + ", takeProfitPrice=" + takeProfitPrice
                + ", stopLossPrice=" + stopLossPrice + ", takeProfitPoint=" + takeProfitPoint + ", stopLossPoint="
                + stopLossPoint + ", entryTimestamp=" + entryTimestamp + " (" + tsIso + ")" + ", isLong=" + isLong
                + ", entryState=" + entryState + ", tradeId='" + tradeId + '\'' + ", tradeDbId=" + tradeDbId
                + ", entryPattern=" + (entryPattern != null ? entryPattern : "null") + '}';
    }
}
