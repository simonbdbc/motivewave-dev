package custom_studies.strategies.strategy_core;

import custom_studies.strategies.strategy_core.HelperFormat.BracketResult;
import custom_studies.strategies.strategy_core.HelperFormat.DirectionResult;
import custom_studies.strategies.strategy_core.HelperFormat.HiloResult;
import custom_studies.strategies.strategy_core.HelperFormat.RenkoResult;
import custom_studies.strategies.strategy_core.HelperFormat.StrengthResult;

public interface InterfacePattern {
    BracketResult getBracket();
    DirectionResult checkTrendDirection();
    StrengthResult checkTrendStrength();
    RenkoResult checkRenkoPatterns();
    HiloResult checkHiloPatterns();
}