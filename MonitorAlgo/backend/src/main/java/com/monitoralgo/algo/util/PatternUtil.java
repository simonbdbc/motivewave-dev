package com.monitoralgo.algo.util;

import java.util.List;

import com.monitoralgo.stats.model.SnapshotAioByRenko;

public class PatternUtil {

    public static class TripleSuiteKey {
        public final String suiteRenko;
        public final String suiteHilo1;
        public final String suiteHilo2;

        public TripleSuiteKey(String renko, String hilo1, String hilo2) {
            this.suiteRenko = renko;
            this.suiteHilo1 = hilo1;
            this.suiteHilo2 = hilo2;
        }
    }

    public static TripleSuiteKey getSuiteKeysFromSnapshots(List<SnapshotAioByRenko> snapshots) {
        StringBuilder renko = new StringBuilder();
        StringBuilder hilo1 = new StringBuilder();
        StringBuilder hilo2 = new StringBuilder();

        for (SnapshotAioByRenko snap : snapshots) {
            renko.append(colorToLetter(snap.getRenkoColor()));
            hilo1.append(colorToLetter(snap.getHiloColor1()));
            hilo2.append(colorToLetter(snap.getHiloColor2()));
        }

        return new TripleSuiteKey(
                renko.toString(),
                hilo1.toString(),
                hilo2.toString());
    }

    private static char colorToLetter(String color) {
        return "GREEN".equalsIgnoreCase(color) ? 'G' : 'R';
    }
}
