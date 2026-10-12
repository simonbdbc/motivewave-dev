package com.monitoralgo.core.model;

/**
 * Modèle "full" (avec objets liés) pour pattern_strategy.
 *
 * Champs notables :
 * - averageBar ← average_bar INTEGER
 * - statsUpdatedAt ← stats_updated_at TEXT
 * - createdAt ← created_at TEXT
 */
public class PatternStrategyFull {

    private Long idPatternStrategy;
    private Instrument instrument;
    private Timeframe timeframe;
    private RenkoSize renkoSize;
    private Pattern pattern;
    private Direction direction;
    private TakeProfit takeprofit;
    private StopLoss stoploss;

    private Long lastTimestampEntry; // last_timestamp_entry BIGINT
    private String lastDateTimeEntry; // last_date_time_entry TEXT
    private Integer averageBar; // average_bar INTEGER
    private Integer count; // count INTEGER
    private Integer countTp; // count_tp INTEGER
    private Integer countSl; // count_sl INTEGER
    private Integer delta; // delta INTEGER
    private String winrate; // winrate TEXT
    private Double profitFactor; // profit_factor DOUBLE
    private Double score; // score DOUBLE
    private String statsUpdatedAt; // stats_updated_at TEXT
    private String createdAt; // created_at TEXT

    public PatternStrategyFull() {
    }

    // --- Constructeur complet AVEC id (lecture BDD)
    public PatternStrategyFull(
            Long idPatternStrategy,
            Instrument instrument,
            Timeframe timeframe,
            RenkoSize renkoSize,
            Pattern pattern,
            Direction direction,
            TakeProfit takeprofit,
            StopLoss stoploss,
            Long lastTimestampEntry,
            String lastDateTimeEntry,
            Integer averageBar,
            Integer count,
            Integer countTp,
            Integer countSl,
            Integer delta,
            String winrate,
            Double profitFactor,
            Double score,
            String statsUpdatedAt,
            String createdAt) {
        this.idPatternStrategy = idPatternStrategy;
        this.instrument = instrument;
        this.timeframe = timeframe;
        this.renkoSize = renkoSize;
        this.pattern = pattern;
        this.direction = direction;
        this.takeprofit = takeprofit;
        this.stoploss = stoploss;
        this.lastTimestampEntry = lastTimestampEntry;
        this.lastDateTimeEntry = lastDateTimeEntry;
        this.averageBar = averageBar;
        this.count = count;
        this.countTp = countTp;
        this.countSl = countSl;
        this.delta = delta;
        this.winrate = winrate;
        this.profitFactor = profitFactor;
        this.score = score;
        this.statsUpdatedAt = statsUpdatedAt;
        this.createdAt = createdAt;
    }

    // --- Constructeur SANS id (pour insert BDD)
    public PatternStrategyFull(
            Instrument instrument,
            Timeframe timeframe,
            RenkoSize renkoSize,
            Pattern pattern,
            Direction direction,
            TakeProfit takeprofit,
            StopLoss stoploss,
            Long lastTimestampEntry,
            String lastDateTimeEntry,
            Integer averageBar,
            Integer count,
            Integer countTp,
            Integer countSl,
            Integer delta,
            String winrate,
            Double profitFactor,
            Double score,
            String statsUpdatedAt,
            String createdAt) {
        this.instrument = instrument;
        this.timeframe = timeframe;
        this.renkoSize = renkoSize;
        this.pattern = pattern;
        this.direction = direction;
        this.takeprofit = takeprofit;
        this.stoploss = stoploss;
        this.lastTimestampEntry = lastTimestampEntry;
        this.lastDateTimeEntry = lastDateTimeEntry;
        this.averageBar = averageBar;
        this.count = count;
        this.countTp = countTp;
        this.countSl = countSl;
        this.delta = delta;
        this.winrate = winrate;
        this.profitFactor = profitFactor;
        this.score = score;
        this.statsUpdatedAt = statsUpdatedAt;
        this.createdAt = createdAt;
    }

    // --- Constructeur partiel (clé logique / recherche uniquement)
    public PatternStrategyFull(
            Instrument instrument,
            Timeframe timeframe,
            RenkoSize renkoSize,
            Pattern pattern,
            Direction direction,
            TakeProfit takeprofit,
            StopLoss stoploss) {
        this.instrument = instrument;
        this.timeframe = timeframe;
        this.renkoSize = renkoSize;
        this.pattern = pattern;
        this.direction = direction;
        this.takeprofit = takeprofit;
        this.stoploss = stoploss;
    }

    // --- Getters & Setters ---

    public Long getId() {
        return idPatternStrategy;
    }

    public void setId(Long idPatternStrategy) {
        this.idPatternStrategy = idPatternStrategy;
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

    public Long getLastTimestampEntry() {
        return lastTimestampEntry;
    }

    public void setLastTimestampEntry(Long lastTimestampEntry) {
        this.lastTimestampEntry = lastTimestampEntry;
    }

    public String getLastDateTimeEntry() {
        return lastDateTimeEntry;
    }

    public void setLastDateTimeEntry(String lastDateTimeEntry) {
        this.lastDateTimeEntry = lastDateTimeEntry;
    }

    public Integer getAverageBar() {
        return averageBar;
    }

    public void setAverageBar(Integer averageBar) {
        this.averageBar = averageBar;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getCountTp() {
        return countTp;
    }

    public void setCountTp(Integer countTp) {
        this.countTp = countTp;
    }

    public Integer getCountSl() {
        return countSl;
    }

    public void setCountSl(Integer countSl) {
        this.countSl = countSl;
    }

    public Integer getDelta() {
        return delta;
    }

    public void setDelta(Integer delta) {
        this.delta = delta;
    }

    public String getWinrate() {
        return winrate;
    }

    public void setWinrate(String winrate) {
        this.winrate = winrate;
    }

    public Double getProfitFactor() {
        return profitFactor;
    }

    public void setProfitFactor(Double profitFactor) {
        this.profitFactor = profitFactor;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getStatsUpdatedAt() {
        return statsUpdatedAt;
    }

    public void setStatsUpdatedAt(String statsUpdatedAt) {
        this.statsUpdatedAt = statsUpdatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
