package backtest.model;

public class RenkoRecord {
    public long id;
    public String instrumentKey;
    public String intervalScale;
    public int intervalValue;
    public double renkoSize;
    public long timestamp;
    public String dateTime;
    public String renkoColor;
    public String hiloColor1;
    public String hiloColor2;
    public double priceRes;

    public RenkoRecord(long id, String instrumentKey, String intervalScale, int intervalValue, double renkoSize, long timestamp,
                       String dateTime, String renkoColor, String hiloColor1, String hiloColor2, double priceRes) {
        this.id = id;
        this.instrumentKey = instrumentKey;
        this.intervalScale = intervalScale;
        this.intervalValue = intervalValue;
        this.renkoSize = renkoSize;
        this.timestamp = timestamp;
        this.dateTime = dateTime;
        this.renkoColor = renkoColor;
        this.hiloColor1 = hiloColor1;
        this.hiloColor2 = hiloColor2;
        this.priceRes = priceRes;
    }
}
