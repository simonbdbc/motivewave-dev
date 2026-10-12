package sbdev.joy_ai_project.strategy_v8o3.core_pattern;

import java.util.ArrayList;
import java.util.List;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID115;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID116;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID117;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID118;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID119;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID120;
import sbdev.joy_ai_project.strategy_v8o3.util.InterfacePattern;
import sbdev.joy_ai_project.strategy_v8o3.util.InterfaceCorePattern;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.BracketResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternCheckResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternDirectionResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternHiloResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternRenkoResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternStrengthResult;

public class CorePattern_10_15_15 implements InterfaceCorePattern {

    private final InterfacePattern[] patterns;

    public CorePattern_10_15_15(String[] colorHistory, String[] hiLoColor1History, String[] hiLoColor2History,
            Double[] hiLoPrice1History, Double[] hiLoPrice2History, Double price, Double close) {

        this.patterns = new InterfacePattern[] {
                new PatternID115(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID116(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID117(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID118(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID119(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID120(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close)
        };
    }

    public List<PatternCheckResult> checkPattern() {
        List<PatternCheckResult> results = new ArrayList<>();

        for (InterfacePattern pattern : patterns) {
            Integer intervalValue = pattern.getInterval();
            BracketResult bracket = pattern.getBracket();
            PatternDirectionResult dir = pattern.checkDirection();
            PatternStrengthResult strength = pattern.checkTrendStrength();
            PatternRenkoResult renko = pattern.checkRenkoPattern();
            PatternHiloResult hilo = pattern.checkHiloPattern();

            results.add(
                    new PatternCheckResult(pattern.getClass().getSimpleName(), intervalValue, bracket, dir, strength, renko, hilo));
        }

        return results;
    }

}
