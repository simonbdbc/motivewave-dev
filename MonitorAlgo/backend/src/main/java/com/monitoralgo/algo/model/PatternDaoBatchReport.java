package com.monitoralgo.algo.model;

/**
 * Rapport d’insert batch pattern_strategy (rowsAffected, batches, autoCommit)
 */
public class PatternDaoBatchReport {
    public final long totalRowsAffected;
    public final int batchCount;
    public final boolean autoCommit;

    public PatternDaoBatchReport(long totalRowsAffected, int batchCount, boolean autoCommit) {
        this.totalRowsAffected = totalRowsAffected;
        this.batchCount = batchCount;
        this.autoCommit = autoCommit;
    }
}