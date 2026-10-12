package sbdev.joy_ai_project.strategy_v8o1.strategy_core.utils;

import sbdev.joy_ai_project.strategy_v8o3.strategy_core.utils.FormatResult.BracketResult;
import sbdev.joy_ai_project.strategy_v8o3.strategy_core.utils.FormatResult.PatternDirectionResult;
import sbdev.joy_ai_project.strategy_v8o3.strategy_core.utils.FormatResult.PatternHiloResult;
import sbdev.joy_ai_project.strategy_v8o3.strategy_core.utils.FormatResult.PatternRenkoResult;
import sbdev.joy_ai_project.strategy_v8o3.strategy_core.utils.FormatResult.PatternStrengthResult;

public interface InterfacePattern {
    String getInstrument();

    Integer getInterval();

    BracketResult getBracket();

    PatternDirectionResult checkDirection();

    PatternRenkoResult checkRenkoPattern();

    PatternHiloResult checkHiloPattern();

    PatternStrengthResult checkTrendStrength();
}