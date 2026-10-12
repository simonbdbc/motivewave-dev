package custom_studies.strategies.strategy_core_v2.utils;

import custom_studies.strategies.strategy_core_v2.utils.FormatResult.BracketResult;
import custom_studies.strategies.strategy_core_v2.utils.FormatResult.PatternDirectionResult;
import custom_studies.strategies.strategy_core_v2.utils.FormatResult.PatternHiloResult;
import custom_studies.strategies.strategy_core_v2.utils.FormatResult.PatternRenkoResult;
import custom_studies.strategies.strategy_core_v2.utils.FormatResult.PatternStrengthResult;

public interface InterfacePattern {
    BracketResult getBracket();
    PatternDirectionResult checkTrendDirection();
    PatternStrengthResult checkTrendStrength();
    PatternRenkoResult checkRenkoPatterns();
    PatternHiloResult checkHiloPatterns();
}