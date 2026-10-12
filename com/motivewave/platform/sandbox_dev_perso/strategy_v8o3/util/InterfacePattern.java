package sbdev.joy_ai_project.strategy_v8o3.util;

import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.BracketResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternDirectionResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternHiloResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternRenkoResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternStrengthResult;

public interface InterfacePattern {
    String getInstrument();

    Integer getInterval();

    BracketResult getBracket();

    PatternDirectionResult checkDirection();

    PatternRenkoResult checkRenkoPattern();

    PatternHiloResult checkHiloPattern();

    PatternStrengthResult checkTrendStrength();
}