package sbdev.joy_ai_project.strategy.model;

public class HiLoResult {
    public Long timestamp;
    public Double hiLo;
    public Double close;
    public Boolean isHigh;
    public Boolean isLow;

    public HiLoResult(Long timestamp, Double hiLo, Double close, Boolean isHigh, Boolean isLow) {
        this.timestamp = timestamp;
        this.hiLo = hiLo;
        this.close = close;
        this.isHigh = isHigh;
        this.isLow = isLow;
    }
}