package sbdev.joy_ai_project.strategy_v8o3.core_pattern;

import java.util.ArrayList;
import java.util.List;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID10;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID11;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID12;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID13;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID14;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID15;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID16;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID17;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID18;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID19;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID20;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID21;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID22;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns.PatternID23;
import sbdev.joy_ai_project.strategy_v8o3.util.InterfacePattern;
import sbdev.joy_ai_project.strategy_v8o3.util.InterfaceCorePattern;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.BracketResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternCheckResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternDirectionResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternHiloResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternRenkoResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternStrengthResult;

public class CorePattern_1_20_20 implements InterfaceCorePattern {

    private final InterfacePattern[] patterns;

    public CorePattern_1_20_20(String[] colorHistory, String[] hiLoColor1History, String[] hiLoColor2History,
            Double[] hiLoPrice1History, Double[] hiLoPrice2History, Double price, Double close) {

        this.patterns = new InterfacePattern[] {
                new PatternID10(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID11(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID12(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID13(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID14(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID15(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID16(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID17(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID18(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID19(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID20(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID21(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID22(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID23(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close)
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
