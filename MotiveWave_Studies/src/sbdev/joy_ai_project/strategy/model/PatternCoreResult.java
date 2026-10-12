package sbdev.joy_ai_project.strategy.model;

public class PatternCoreResult {
    public final String name;
    public final boolean high;
    public final boolean low;

    public PatternCoreResult(String name, boolean high, boolean low) {
        this.name = name;
        this.high = high;
        this.low = low;
    }
}