package custom_studies.strategies.strategy_core_v4;

import java.util.ArrayList;
import java.util.List;

import custom_studies.strategies.strategy_core_v4.strategy_patterns_old.PatternN1;
import custom_studies.strategies.strategy_core_v4.strategy_patterns_old.PatternN1V1;
import custom_studies.strategies.strategy_core_v4.strategy_patterns_old.PatternN2;
import custom_studies.strategies.strategy_core_v4.strategy_patterns_old.PatternN21;
import custom_studies.strategies.strategy_core_v4.strategy_patterns_old.PatternN22;
import custom_studies.strategies.strategy_core_v4.strategy_patterns_old.PatternN222;
import custom_studies.strategies.strategy_core_v4.strategy_patterns_old.PatternN23;
import custom_studies.strategies.strategy_core_v4.strategy_patterns_old.PatternN24;
import custom_studies.strategies.strategy_core_v4.strategy_patterns_old.PatternN25;
import custom_studies.strategies.strategy_core_v4.strategy_patterns_old.PatternN26;
import custom_studies.strategies.strategy_core_v4.strategy_patterns_old.PatternN3;
import custom_studies.strategies.strategy_core_v4.strategy_patterns_old.PatternN4;
import custom_studies.strategies.strategy_core_v4.utils.InterfacePattern;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.BracketResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.PatternCheckResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.PatternDirectionResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.PatternHiloResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.PatternRenkoResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.PatternStrengthResult;

public class StrategyCorePattern {

    private final InterfacePattern[] patterns;

    public StrategyCorePattern(String[] colorHistory, String[] hiLoColor1History, String[] hiLoColor2History,
            Double[] hiLoPrice1History, Double[] hiLoPrice2History, Double price, Double close) {

        this.patterns = new InterfacePattern[] {
                // new PatternN1V1(colorHistory, hiLoColor1History, hiLoColor2History,
                //         hiLoPrice1History, hiLoPrice2History, price, close),
                new PatternN21(colorHistory, hiLoColor1History, hiLoColor2History,
                        hiLoPrice1History, hiLoPrice2History, price, close),
                // new PatternN22(colorHistory, hiLoColor1History, hiLoColor2History,
                //         hiLoPrice1History, hiLoPrice2History, price, close),
                // new PatternN222(colorHistory, hiLoColor1History, hiLoColor2History,
                //         hiLoPrice1History, hiLoPrice2History, price, close),
                // new PatternN23(colorHistory, hiLoColor1History, hiLoColor2History,
                // hiLoPrice1History, hiLoPrice2History, price, close),
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
