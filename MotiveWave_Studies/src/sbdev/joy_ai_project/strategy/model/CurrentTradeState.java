package sbdev.joy_ai_project.strategy.model;

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
}
