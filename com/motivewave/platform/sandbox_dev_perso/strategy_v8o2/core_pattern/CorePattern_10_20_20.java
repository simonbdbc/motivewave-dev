package sbdev.joy_ai_project.strategy_v8o3.core_pattern;

import java.util.ArrayList;
import java.util.List;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID59;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID60;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID61;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID62;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID63;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID64;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID65;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID66;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID67;
import sbdev.joy_ai_project.strategy_v8o3.util.InterfacePattern;
import sbdev.joy_ai_project.strategy_v8o3.util.InterfaceCorePattern;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.BracketResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternCheckResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternDirectionResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternHiloResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternRenkoResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternStrengthResult;

public class CorePattern_10_20_20 implements InterfaceCorePattern {

    private final InterfacePattern[] patterns;

    public CorePattern_10_20_20(String[] colorHistory, String[] hiLoColor1History, String[] hiLoColor2History,
            Double[] hiLoPrice1History, Double[] hiLoPrice2History, Double price, Double close) {

        this.patterns = new InterfacePattern[] {
                new PatternID59(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID60(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID61(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID62(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID63(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID64(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID65(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID66(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID67(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close)
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
