package sbdev.joy_ai_project.strategy_v8o3.core_pattern;

import java.util.ArrayList;
import java.util.List;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID68;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID69;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID70;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID71;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID72;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID73;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID74;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID75;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID76;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID77;
import sbdev.joy_ai_project.strategy_v8o3.util.InterfacePattern;
import sbdev.joy_ai_project.strategy_v8o3.util.InterfaceCorePattern;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.BracketResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternCheckResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternDirectionResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternHiloResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternRenkoResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternStrengthResult;

public class CorePattern_25_30_30 implements InterfaceCorePattern {

    private final InterfacePattern[] patterns;

    public CorePattern_25_30_30(String[] colorHistory, String[] hiLoColor1History, String[] hiLoColor2History,
            Double[] hiLoPrice1History, Double[] hiLoPrice2History, Double price, Double close) {

        this.patterns = new InterfacePattern[] {
                new PatternID68(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID69(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID70(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID71(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID72(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID73(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID74(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID75(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID76(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID77(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close)
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
