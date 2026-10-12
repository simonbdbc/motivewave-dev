package custom_studies.strategies.strategy_core;

import java.util.HashMap;
import java.util.Map;

public class HelperFormat {

    public static class PatternCheckResult {
        public final String patternName;
        public final BracketResult bracket;
        public final DirectionResult direction;
        public final StrengthResult strength;
        public final RenkoResult renko;
        public final HiloResult hilo;

        public PatternCheckResult(String patternName,
                BracketResult bracket,
                DirectionResult direction,
                StrengthResult strength,
                RenkoResult renko,
                HiloResult hilo) {
            this.patternName = patternName;
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

    public static class PatternCoreResult {
        public final String name;
        public final boolean high;
        public final boolean low;

        public PatternCoreResult(String name, boolean high, boolean low) {
            this.name = name;
            this.high = high;
            this.low = low;
        }
    }

    public static class DirectionResult {
        public final boolean high;
        public final boolean low;

        public DirectionResult(boolean high, boolean low) {
            this.high = high;
            this.low = low;
        }
    }

    public static class StrengthResult {
        public final boolean high;
        public final boolean low;

        public StrengthResult(boolean high, boolean low) {
            this.high = high;
            this.low = low;
        }
    }

    public static class RenkoResult {
        public final boolean high;
        public final boolean low;

        public RenkoResult(boolean high, boolean low) {
            this.high = high;
            this.low = low;
        }
    }

    public static class HiloResult {
        public final boolean high;
        public final boolean low;

        public HiloResult(boolean high, boolean low) {
            this.high = high;
            this.low = low;
        }
    }

    public static class BracketResult {
        public final double tp;
        public final double sl;

        public BracketResult(double tp, double sl) {
            this.tp = tp;
            this.sl = sl;
        }
    }

    public static class ColorCountResult {
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
    

}
