package sbdev.joy_ai_project.strategy.model;

public class RenkoResult {
    public final boolean crossAbove;
    public final boolean crossBelow;
    public final double realRenko;
    public final double previousClose;

    public RenkoResult(boolean crossAbove, boolean crossBelow, double realRenko, double previousClose) {
        this.crossAbove = crossAbove;
        this.crossBelow = crossBelow;
        this.realRenko = realRenko;
        this.previousClose = previousClose;
    }
}