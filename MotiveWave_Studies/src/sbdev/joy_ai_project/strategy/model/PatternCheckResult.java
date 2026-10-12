package sbdev.joy_ai_project.strategy.model;

public class PatternCheckResult {
    public final String patternName;
    public final Integer interval;
    public final BracketResult bracket;
    public final PatternDirectionResult direction;
    public final PatternStrengthResult strength;
    public final PatternRenkoResult renko;
    public final PatternHiloResult hilo;

    public PatternCheckResult(String patternName, Integer interval, BracketResult bracket,
            PatternDirectionResult direction, PatternStrengthResult strength, PatternRenkoResult renko,
            PatternHiloResult hilo) {
        this.patternName = patternName;
        this.interval = interval;
        this.bracket = bracket;
        this.direction = direction;
        this.strength = strength;
        this.renko = renko;
        this.hilo = hilo;
    }

    public boolean okLong() {
        return direction.high && strength.high && renko.high && hilo.high;
    }

    public boolean okShort() {
        return direction.low && strength.low && renko.low && hilo.low;
    }
}