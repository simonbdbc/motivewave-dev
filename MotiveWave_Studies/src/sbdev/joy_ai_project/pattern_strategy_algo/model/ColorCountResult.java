package sbdev.joy_ai_project.pattern_strategy_algo.model;

public class ColorCountResult {
    private final int greenCount;
    private final int redCount;

    public ColorCountResult(int greenCount, int redCount) {
        this.greenCount = greenCount;
        this.redCount = redCount;
    }

    public int getGreenCount() {
        return greenCount;
    }

    public int getRedCount() {
        return redCount;
    }
}