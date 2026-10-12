package sbdev.joy_ai_project.strategy_v8o3.core_pattern.patterns;

import sbdev.joy_ai_project.strategy_v8o3.util.Helper;
import sbdev.joy_ai_project.strategy_v8o3.util.InterfacePattern;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.BracketResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.ColorCountResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternDirectionResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternHiloResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternRenkoResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternStrengthResult;

// Auto-generated pattern based on Renko backtest
public class PatternID65 implements InterfacePattern {

    private static final String GREEN = "green";
    private static final String RED = "red";

    private String[] colorHistory;
    private String[] hiLoColor1History;
    private String[] hiLoColor2History;
    private Double[] hiLoPrice1History;
    private Double[] hiLoPrice2History;
    private Double price;
    private Double close;

    public PatternID65(String[] colorHistory, String[] hiLoColor1History, String[] hiLoColor2History,
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
        Double tpTarget = 80.00;
        Double slTarget = 80.00;
        return new BracketResult(tpTarget, slTarget);
    }

    public PatternDirectionResult checkDirection() {
        Boolean dirHigh = false;
        Boolean dirShort = true;
        return new PatternDirectionResult(dirHigh, dirShort);
    }

    public PatternRenkoResult checkRenkoPattern() {
        Boolean renkoPatternHigh = false;
        Boolean renkoPatternLow = colorHistory[0].equals(GREEN) && colorHistory[1].equals(GREEN) && colorHistory[2].equals(RED) && colorHistory[3].equals(GREEN) && colorHistory[4].equals(GREEN) && colorHistory[5].equals(RED) && colorHistory[6].equals(GREEN) && colorHistory[7].equals(GREEN);
        return new PatternRenkoResult(renkoPatternHigh, renkoPatternLow);
    }

    public PatternHiloResult checkHiloPattern() {
        Boolean hiloPatternHigh = false;
        Boolean hiloPatternLow = hiLoColor1History[0].equals(GREEN) && hiLoColor1History[1].equals(GREEN) && hiLoColor1History[2].equals(GREEN) && hiLoColor1History[3].equals(GREEN) && hiLoColor1History[4].equals(GREEN) && hiLoColor1History[5].equals(GREEN) && hiLoColor1History[6].equals(GREEN) && hiLoColor1History[7].equals(GREEN) && hiLoColor2History[0].equals(GREEN) && hiLoColor2History[1].equals(GREEN) && hiLoColor2History[2].equals(GREEN) && hiLoColor2History[3].equals(GREEN) && hiLoColor2History[4].equals(GREEN) && hiLoColor2History[5].equals(GREEN) && hiLoColor2History[6].equals(GREEN) && hiLoColor2History[7].equals(GREEN);
        return new PatternHiloResult(hiloPatternHigh, hiloPatternLow);
    }

    public PatternStrengthResult checkTrendStrength() {
        Boolean trendStrengthHigh = true;
        Boolean trendStrengthLow = true;
        return new PatternStrengthResult(trendStrengthHigh, trendStrengthLow);
    }
}
