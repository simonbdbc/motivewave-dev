package sbdev.joy_ai_project.strategy_v8o1.strategy_core.patterns;

import sbdev.joy_ai_project.strategy_v8o3.strategy_core.utils.Helper;
import sbdev.joy_ai_project.strategy_v8o3.strategy_core.utils.InterfacePattern;
import sbdev.joy_ai_project.strategy_v8o3.strategy_core.utils.FormatResult.BracketResult;
import sbdev.joy_ai_project.strategy_v8o3.strategy_core.utils.FormatResult.ColorCountResult;
import sbdev.joy_ai_project.strategy_v8o3.strategy_core.utils.FormatResult.PatternDirectionResult;
import sbdev.joy_ai_project.strategy_v8o3.strategy_core.utils.FormatResult.PatternHiloResult;
import sbdev.joy_ai_project.strategy_v8o3.strategy_core.utils.FormatResult.PatternRenkoResult;
import sbdev.joy_ai_project.strategy_v8o3.strategy_core.utils.FormatResult.PatternStrengthResult;

// Auto-generated pattern based on Renko backtest
public class PatternID91 implements InterfacePattern {

    private static final String GREEN = "green";
    private static final String RED = "red";

    private String[] colorHistory;
    private String[] hiLoColor1History;
    private String[] hiLoColor2History;
    private Double[] hiLoPrice1History;
    private Double[] hiLoPrice2History;
    private Double price;
    private Double close;

    public PatternID91(String[] colorHistory, String[] hiLoColor1History, String[] hiLoColor2History,
            Double[] hiLoPrice1History, Double[] hiLoPrice2History, Double price, Double close) {
        this.colorHistory = colorHistory;
        this.hiLoColor1History = hiLoColor1History;
        this.hiLoColor2History = hiLoColor2History;
        this.hiLoPrice1History = hiLoPrice1History;
        this.hiLoPrice2History = hiLoPrice2History;
        this.price = price;
        this.close = close;
    }

    public String getInstrument() {
        String instrument = "MNQ";
        return instrument;
    }

    public Integer getInterval() {
        Integer interval = 10;
        return interval;
    }

    public BracketResult getBracket() {
        Double tpTarget = 60.00 / 4;
        Double slTarget = 60.00 / 4;
        return new BracketResult(tpTarget, slTarget);
    }

    public PatternDirectionResult checkDirection() {
        Boolean dirHigh = false;
        Boolean dirShort = true;
        return new PatternDirectionResult(dirHigh, dirShort);
    }

    public PatternRenkoResult checkRenkoPattern() {
        Boolean renkoPatternHigh = false;
        Boolean renkoPatternLow = colorHistory[0].equals(RED) && colorHistory[1].equals(GREEN) && colorHistory[2].equals(GREEN) && colorHistory[3].equals(RED) && colorHistory[4].equals(RED) && colorHistory[5].equals(GREEN) && colorHistory[6].equals(RED) && colorHistory[7].equals(GREEN);
        return new PatternRenkoResult(renkoPatternHigh, renkoPatternLow);
    }

    public PatternHiloResult checkHiloPattern() {
        Boolean hiloPatternHigh = false;
        Boolean hiloPatternLow = hiLoColor1History[0].equals(RED) && hiLoColor1History[1].equals(RED) && hiLoColor1History[2].equals(RED) && hiLoColor1History[3].equals(RED) && hiLoColor1History[4].equals(RED) && hiLoColor1History[5].equals(RED) && hiLoColor1History[6].equals(RED) && hiLoColor1History[7].equals(RED) && hiLoColor2History[0].equals(RED) && hiLoColor2History[1].equals(RED) && hiLoColor2History[2].equals(RED) && hiLoColor2History[3].equals(RED) && hiLoColor2History[4].equals(RED) && hiLoColor2History[5].equals(RED) && hiLoColor2History[6].equals(RED) && hiLoColor2History[7].equals(RED);
        return new PatternHiloResult(hiloPatternHigh, hiloPatternLow);
    }

    public PatternStrengthResult checkTrendStrength() {
        Boolean trendStrengthHigh = true;
        Boolean trendStrengthLow = true;
        return new PatternStrengthResult(trendStrengthHigh, trendStrengthLow);
    }
}
