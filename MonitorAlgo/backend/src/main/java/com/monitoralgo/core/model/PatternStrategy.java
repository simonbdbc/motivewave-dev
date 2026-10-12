package com.monitoralgo.core.model;

/**
 * Modèle "léger" mappé 1–1 avec la table pattern_strategy (colonnes par IDs).
 *
 * Champs notables :
 * - averageBar ← average_bar INTEGER (moyenne des count_bar)
 * - statsUpdatedAt ← stats_updated_at TEXT (date/heure du dernier calcul de
 * stats)
 * - createdAt ← created_at TEXT (date/heure de création de la ligne)
 */
public class PatternStrategy {

    private Long idPatternStrategy; // BIGSERIAL → Long objet (nullable avant insert)
    private int instrumentId;
    private int timeframeId;
    private int renkoSizeId;
    private int patternId;
    private int directionId;
    private int takeprofitId;
    private int stoplossId;

    private Long lastTimestampEntry; // last_timestamp_entry BIGINT
    private String lastDateTimeEntry; // last_date_time_entry TEXT
    private Integer averageBar; // average_bar INTEGER
    private Integer count; // count INTEGER
    private Integer countTp; // count_tp INTEGER
    private Integer countSl; // count_sl INTEGER
    private Integer delta; // delta INTEGER
    private String winrate; // winrate TEXT (ex: "51%")
    private Double profitFactor; // profit_factor DOUBLE PRECISION
    private Double score; // score DOUBLE PRECISION
    private String statsUpdatedAt; // stats_updated_at TEXT
    private String createdAt; // created_at TEXT

    public PatternStrategy() {
    }

    // --- Constructeur COMPLET (lecture BDD)
    public PatternStrategy(
            Long idPatternStrategy,
            int instrumentId,
            int timeframeId,
            int renkoSizeId,
            int patternId,
            int directionId,
            int takeprofitId,
            int stoplossId,
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
        this.instrumentId = instrumentId;
        this.timeframeId = timeframeId;
        this.renkoSizeId = renkoSizeId;
        this.patternId = patternId;
        this.directionId = directionId;
        this.takeprofitId = takeprofitId;
        this.stoplossId = stoplossId;
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

    // --- Constructeur SANS id (pour INSERT)
    public PatternStrategy(
            int instrumentId,
            int timeframeId,
            int renkoSizeId,
            int patternId,
            int directionId,
            int takeprofitId,
            int stoplossId,
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
        this.instrumentId = instrumentId;
        this.timeframeId = timeframeId;
        this.renkoSizeId = renkoSizeId;
        this.patternId = patternId;
        this.directionId = directionId;
        this.takeprofitId = takeprofitId;
        this.stoplossId = stoplossId;
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

    // --- Getters & Setters ---

    public Long getIdPatternStrategy() {
        return idPatternStrategy;
    }

    public void setIdPatternStrategy(Long idPatternStrategy) {
        this.idPatternStrategy = idPatternStrategy;
    }

    public int getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(int instrumentId) {
        this.instrumentId = instrumentId;
    }

    public int getTimeframeId() {
        return timeframeId;
    }

    public void setTimeframeId(int timeframeId) {
        this.timeframeId = timeframeId;
    }

    public int getRenkoSizeId() {
        return renkoSizeId;
    }

    public void setRenkoSizeId(int renkoSizeId) {
        this.renkoSizeId = renkoSizeId;
    }

    public int getPatternId() {
        return patternId;
    }

    public void setPatternId(int patternId) {
        this.patternId = patternId;
    }

    public int getDirectionId() {
        return directionId;
    }

    public void setDirectionId(int directionId) {
        this.directionId = directionId;
    }

    public int getTakeprofitId() {
        return takeprofitId;
    }

    public void setTakeprofitId(int takeprofitId) {
        this.takeprofitId = takeprofitId;
    }

    public int getStoplossId() {
        return stoplossId;
    }

    public void setStoplossId(int stoplossId) {
        this.stoplossId = stoplossId;
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
