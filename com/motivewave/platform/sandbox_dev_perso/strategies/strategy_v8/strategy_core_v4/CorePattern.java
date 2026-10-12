package custom_studies.strategies.strategy_v8o3.strategy_core_v4;

import java.util.ArrayList;
import java.util.List;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.patterns.PatternID1;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.patterns.PatternID2;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.patterns.PatternID3;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.patterns.PatternID4;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.patterns.PatternID5;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.patterns.PatternID6;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.patterns.PatternID7;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.patterns.PatternID8;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.patterns.PatternID9;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.patterns.PatternID10;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.patterns.PatternID11;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.patterns.PatternID12;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.patterns.PatternID13;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.patterns.PatternID14;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.patterns.PatternID15;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.utils.InterfacePattern;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.utils.FormatResult.BracketResult;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.utils.FormatResult.PatternCheckResult;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.utils.FormatResult.PatternDirectionResult;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.utils.FormatResult.PatternHiloResult;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.utils.FormatResult.PatternRenkoResult;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.utils.FormatResult.PatternStrengthResult;

public class CorePattern {

    private final InterfacePattern[] patterns;

    public CorePattern(String[] colorHistory, String[] hiLoColor1History, String[] hiLoColor2History,
            Double[] hiLoPrice1History, Double[] hiLoPrice2History, Double price, Double close) {

        this.patterns = new InterfacePattern[] {
                new PatternID1(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID2(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID3(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID4(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID5(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID6(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID7(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID8(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID9(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID10(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID11(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID12(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID13(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID14(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternID15(colorHistory, hiLoColor1History, hiLoColor2History, hiLoPrice1History, hiLoPrice2History, price, close)
        };
    }

    public List<PatternCheckResult> checkPattern() {
        List<PatternCheckResult> results = new ArrayList<>();

        for (InterfacePattern pattern : patterns) {
            Integer interval = pattern.getInterval();
            BracketResult bracket = pattern.getBracket();
            PatternDirectionResult dir = pattern.checkDirection();
            PatternStrengthResult strength = pattern.checkTrendStrength();
            PatternRenkoResult renko = pattern.checkRenkoPattern();
            PatternHiloResult hilo = pattern.checkHiloPattern();

            results.add(
                    new PatternCheckResult(pattern.getClass().getSimpleName(), interval, bracket, dir, strength, renko, hilo));
        }

        return results;
    }

}
