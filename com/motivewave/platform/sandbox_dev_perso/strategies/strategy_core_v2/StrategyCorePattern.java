package custom_studies.strategies.strategy_core_v2;

import java.util.ArrayList;
import java.util.List;

import custom_studies.strategies.strategy_core_v2.strategies_patterns.PatternN1;
import custom_studies.strategies.strategy_core_v2.strategies_patterns.PatternN2;
import custom_studies.strategies.strategy_core_v2.strategies_patterns.PatternN21;
import custom_studies.strategies.strategy_core_v2.strategies_patterns.PatternN22;
import custom_studies.strategies.strategy_core_v2.strategies_patterns.PatternN23;
import custom_studies.strategies.strategy_core_v2.strategies_patterns.PatternN24;
import custom_studies.strategies.strategy_core_v2.strategies_patterns.PatternN25;
import custom_studies.strategies.strategy_core_v2.strategies_patterns.PatternN26;
import custom_studies.strategies.strategy_core_v2.strategies_patterns.PatternN3;
import custom_studies.strategies.strategy_core_v2.strategies_patterns.PatternN4;
import custom_studies.strategies.strategy_core_v2.utils.InterfacePattern;
import custom_studies.strategies.strategy_core_v2.utils.FormatResult.BracketResult;
import custom_studies.strategies.strategy_core_v2.utils.FormatResult.PatternCheckResult;
import custom_studies.strategies.strategy_core_v2.utils.FormatResult.PatternDirectionResult;
import custom_studies.strategies.strategy_core_v2.utils.FormatResult.PatternHiloResult;
import custom_studies.strategies.strategy_core_v2.utils.FormatResult.PatternRenkoResult;
import custom_studies.strategies.strategy_core_v2.utils.FormatResult.PatternStrengthResult;

public class StrategyCorePattern {

    private final InterfacePattern[] patterns;

    public StrategyCorePattern(String[] colorHistory, String[] hiLoColor1History, String[] hiLoColor2History,
            Double[] hiLoPrice1History, Double[] hiLoPrice2History, Double price, float close) {

        this.patterns = new InterfacePattern[] {
                // new PatternN1(colorHistory, hiLoColor1History, hiLoColor2History,
                //         hiLoPrice1History, hiLoPrice2History, price, close),
                // new PatternN2(colorHistory, hiLoColor1History, hiLoColor2History,
                //         hiLoPrice1History, hiLoPrice2History, price, close),
                // new PatternN3(colorHistory, hiLoColor1History, hiLoColor2History,
                //         hiLoPrice1History, hiLoPrice2History, price, close),
                // new PatternN4(colorHistory, hiLoColor1History, hiLoColor2History,
                // hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternN21(colorHistory, hiLoColor1History, hiLoColor2History,
                        hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternN22(colorHistory, hiLoColor1History, hiLoColor2History,
                        hiLoPrice1History, hiLoPrice2History, price, close),
                // new PatternN12(colorHistory, hiLoColor1History, hiLoColor2History,
                //         hiLoPrice1History, hiLoPrice2History, price, close),
                // new PatternN13(colorHistory, hiLoColor1History, hiLoColor2History,
                //         hiLoPrice1History, hiLoPrice2History, price, close),
                // new PatternN14(colorHistory, hiLoColor1History, hiLoColor2History,
                //         hiLoPrice1History, hiLoPrice2History, price, close),
                // new PatternN15(colorHistory, hiLoColor1History, hiLoColor2History,
                //         hiLoPrice1History, hiLoPrice2History, price, close),
                // new PatternN16(colorHistory, hiLoColor1History, hiLoColor2History,
                //         hiLoPrice1History, hiLoPrice2History, price, close),
        };
    }

    public List<PatternCheckResult> checkPattern() {
        List<PatternCheckResult> results = new ArrayList<>();

        for (InterfacePattern pattern : patterns) {
            BracketResult bracket = pattern.getBracket();
            PatternDirectionResult dir = pattern.checkTrendDirection();
            PatternStrengthResult strength = pattern.checkTrendStrength();
            PatternRenkoResult renko = pattern.checkRenkoPatterns();
            PatternHiloResult hilo = pattern.checkHiloPatterns();

            results.add(new PatternCheckResult(
                    pattern.getClass().getSimpleName(),
                    bracket, dir, strength, renko, hilo));
        }

        return results;
    }

}
