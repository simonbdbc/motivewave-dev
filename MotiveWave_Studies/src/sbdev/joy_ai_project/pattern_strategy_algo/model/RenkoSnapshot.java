package sbdev.joy_ai_project.pattern_strategy_algo.model;

public class RenkoSnapshot {
    public final String instrumentKey, instrumentName, intervalScale, dateTime, renkoColor, hiloColor1, hiloColor2,
            updatedAt;
    public final int intervalValue;
    public final double renkoSize, priceRes, priceSup, hiloPrice1, hiloPrice2;
    public final long timestamp;

    public RenkoSnapshot(String instrumentKey, String instrumentName, String intervalScale, int intervalValue,
            double renkoSize, long timestamp, String dateTime, double priceRes, double priceSup, String renkoColor,
            String hiloColor1, String hiloColor2, double hiloPrice1, double hiloPrice2, String updatedAt) {
        this.instrumentKey = instrumentKey;
        this.instrumentName = instrumentName;
        this.intervalScale = intervalScale;
        this.intervalValue = intervalValue;
        this.renkoSize = renkoSize;
        this.timestamp = timestamp;
        this.dateTime = dateTime;
        this.priceRes = priceRes;
        this.priceSup = priceSup;
        this.renkoColor = renkoColor;
        this.hiloColor1 = hiloColor1;
        this.hiloColor2 = hiloColor2;
        this.hiloPrice1 = hiloPrice1;
        this.hiloPrice2 = hiloPrice2;
        this.updatedAt = updatedAt;
    }
}
