package com.monitoralgo.algo.model;

/** Rapport d’insert batch trades (rowsAffected, batches/chunks, autoCommit) */
public class TradeDaoBatchReport {
    public final long totalRowsAffected;
    public final int batchCount;
    public final boolean autoCommit;

    public TradeDaoBatchReport(long totalRowsAffected, int batchCount, boolean autoCommit) {
        this.totalRowsAffected = totalRowsAffected;
        this.batchCount = batchCount;
        this.autoCommit = autoCommit;
    }
}