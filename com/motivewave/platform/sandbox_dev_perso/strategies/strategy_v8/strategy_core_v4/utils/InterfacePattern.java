package custom_studies.strategies.strategy_v8o3.strategy_core_v4.utils;

import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.utils.FormatResult.BracketResult;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.utils.FormatResult.PatternDirectionResult;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.utils.FormatResult.PatternHiloResult;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.utils.FormatResult.PatternRenkoResult;
import sbdev.joy_ai_project.strategy_v4.strategy_core_v4.utils.FormatResult.PatternStrengthResult;

public interface InterfacePattern {
    String getInstrument();

    Integer getInterval();

    BracketResult getBracket();

    PatternDirectionResult checkDirection();

    PatternRenkoResult checkRenkoPattern();

    PatternHiloResult checkHiloPattern();

    PatternStrengthResult checkTrendStrength();
}