package sbdev.joy_ai_project.strategy.model;

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