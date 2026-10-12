package com.monitoralgo.core.model;

/**
 * Modèle "full" (avec objets liés) pour trade_strategy_live.
 */
public class TradeStrategyLiveFull {
    private Long idTrade;
    private Instrument instrument;
    private Timeframe timeframe;
    private RenkoSize renkoSize;
    private Pattern pattern;
    private Direction direction;
    private TakeProfit takeprofit;
    private StopLoss stoploss;
    private Long timestampEntry;
    private String dateTimeEntry;
    private Double entryPrice;
    private Long timestampExit;
    private String dateTimeExit;
    private Double exitPrice;
    private Double tpPrice;
    private Double slPrice;
    private String result;
    private String updatedAt;

    public TradeStrategyLiveFull() {
    }

    // --- Constructeur complet AVEC idTrade
    public TradeStrategyLiveFull(
            Long idTrade,
            Instrument instrument,
            Timeframe timeframe,
            RenkoSize renkoSize,
            Pattern pattern,
            Direction direction,
            TakeProfit takeprofit,
            StopLoss stoploss,
            Long timestampEntry,
            String dateTimeEntry,
            Double entryPrice,
            Long timestampExit,
            String dateTimeExit,
            Double exitPrice,
            Double tpPrice,
            Double slPrice,
            String result,
            String updatedAt) {
        this.idTrade = idTrade;
        this.instrument = instrument;
        this.timeframe = timeframe;
        this.renkoSize = renkoSize;
        this.pattern = pattern;
        this.direction = direction;
        this.takeprofit = takeprofit;
        this.stoploss = stoploss;
        this.timestampEntry = timestampEntry;
        this.dateTimeEntry = dateTimeEntry;
        this.entryPrice = entryPrice;
        this.timestampExit = timestampExit;
        this.dateTimeExit = dateTimeExit;
        this.exitPrice = exitPrice;
        this.tpPrice = tpPrice;
        this.slPrice = slPrice;
        this.result = result;
        this.updatedAt = updatedAt;
    }

    // --- Constructeur SANS idTrade (id généré par la BDD)
    public TradeStrategyLiveFull(
            Instrument instrument,
            Timeframe timeframe,
            RenkoSize renkoSize,
            Pattern pattern,
            Direction direction,
            TakeProfit takeprofit,
            StopLoss stoploss,
            Long timestampEntry,
            String dateTimeEntry,
            Double entryPrice,
            Long timestampExit,
            String dateTimeExit,
            Double exitPrice,
            Double tpPrice,
            Double slPrice,
            String result) {
        this.instrument = instrument;
        this.timeframe = timeframe;
        this.renkoSize = renkoSize;
        this.pattern = pattern;
        this.direction = direction;
        this.takeprofit = takeprofit;
        this.stoploss = stoploss;
        this.timestampEntry = timestampEntry;
        this.dateTimeEntry = dateTimeEntry;
        this.entryPrice = entryPrice;
        this.timestampExit = timestampExit;
        this.dateTimeExit = dateTimeExit;
        this.exitPrice = exitPrice;
        this.tpPrice = tpPrice;
        this.slPrice = slPrice;
        this.result = result;
    }

    // --- getters/setters ---

    public Long getIdTrade() {
        return idTrade;
    }

    public void setIdTrade(Long idTrade) {
        this.idTrade = idTrade;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public Timeframe getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(Timeframe timeframe) {
        this.timeframe = timeframe;
    }

    public RenkoSize getRenkoSize() {
        return renkoSize;
    }

    public void setRenkoSize(RenkoSize renkoSize) {
        this.renkoSize = renkoSize;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public TakeProfit getTakeprofit() {
        return takeprofit;
    }

    public void setTakeprofit(TakeProfit takeprofit) {
        this.takeprofit = takeprofit;
    }

    public StopLoss getStoploss() {
        return stoploss;
    }

    public void setStoploss(StopLoss stoploss) {
        this.stoploss = stoploss;
    }

    public Long getTimestampEntry() {
        return timestampEntry;
    }

    public void setTimestampEntry(Long timestampEntry) {
        this.timestampEntry = timestampEntry;
    }

    public String getDateTimeEntry() {
        return dateTimeEntry;
    }

    public void setDateTimeEntry(String dateTimeEntry) {
        this.dateTimeEntry = dateTimeEntry;
    }

    public Double getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(Double entryPrice) {
        this.entryPrice = entryPrice;
    }

    public Long getTimestampExit() {
        return timestampExit;
    }

    public void setTimestampExit(Long timestampExit) {
        this.timestampExit = timestampExit;
    }

    public String getDateTimeExit() {
        return dateTimeExit;
    }

    public void setDateTimeExit(String dateTimeExit) {
        this.dateTimeExit = dateTimeExit;
    }

    public Double getExitPrice() {
        return exitPrice;
    }

    public void setExitPrice(Double exitPrice) {
        this.exitPrice = exitPrice;
    }

    public Double getTpPrice() {
        return tpPrice;
    }

    public void setTpPrice(Double tpPrice) {
        this.tpPrice = tpPrice;
    }

    public Double getSlPrice() {
        return slPrice;
    }

    public void setSlPrice(Double slPrice) {
        this.slPrice = slPrice;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
