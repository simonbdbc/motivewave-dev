package sbdev.joy_ai_project.pattern_strategy_algo.model;

public class RenkoResult {
    public final boolean crossAbove;
    public final boolean crossBelow;
    public final Double realRenko;
    public final Double previousClose;

    public RenkoResult(boolean crossAbove, boolean crossBelow, Double realRenko, Double previousClose) {
        this.crossAbove = crossAbove;
        this.crossBelow = crossBelow;
        this.realRenko = realRenko;
        this.previousClose = previousClose;
    }
}