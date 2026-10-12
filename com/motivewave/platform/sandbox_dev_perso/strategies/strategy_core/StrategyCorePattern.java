package custom_studies.strategies.strategy_core;

import java.util.ArrayList;
import java.util.List;

import custom_studies.strategies.strategy_core.HelperFormat.BracketResult;
import custom_studies.strategies.strategy_core.HelperFormat.DirectionResult;
import custom_studies.strategies.strategy_core.HelperFormat.HiloResult;
import custom_studies.strategies.strategy_core.HelperFormat.PatternCheckResult;
import custom_studies.strategies.strategy_core.HelperFormat.RenkoResult;
import custom_studies.strategies.strategy_core.HelperFormat.StrengthResult;
import custom_studies.strategies.strategy_core.strategies_patterns.PatternN1;
import custom_studies.strategies.strategy_core.strategies_patterns.PatternN2;
import custom_studies.strategies.strategy_core.strategies_patterns.PatternN21;
import custom_studies.strategies.strategy_core.strategies_patterns.PatternN212;
import custom_studies.strategies.strategy_core.strategies_patterns.PatternN22;
import custom_studies.strategies.strategy_core.strategies_patterns.PatternN23;
import custom_studies.strategies.strategy_core.strategies_patterns.PatternN24;
import custom_studies.strategies.strategy_core.strategies_patterns.PatternN25;
import custom_studies.strategies.strategy_core.strategies_patterns.PatternN26;
import custom_studies.strategies.strategy_core.strategies_patterns.PatternN3;
import custom_studies.strategies.strategy_core.strategies_patterns.PatternN4;

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
                new PatternN212(colorHistory, hiLoColor1History, hiLoColor2History,
                        hiLoPrice1History, hiLoPrice2History, price, close),
                // new PatternN21(colorHistory, hiLoColor1History, hiLoColor2History,
                //         hiLoPrice1History, hiLoPrice2History, price, close),
                // new PatternN22(colorHistory, hiLoColor1History, hiLoColor2History,
                //         hiLoPrice1History, hiLoPrice2History, price, close),
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
            DirectionResult dir = pattern.checkTrendDirection();
            StrengthResult strength = pattern.checkTrendStrength();
            RenkoResult renko = pattern.checkRenkoPatterns();
            HiloResult hilo = pattern.checkHiloPatterns();

            results.add(new PatternCheckResult(
                    pattern.getClass().getSimpleName(),
                    bracket, dir, strength, renko, hilo));
        }

        return results;
    }

}
