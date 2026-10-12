package com.monitoralgo.algo.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtil {
    /**
     * Arrondit un double à n décimales, arrondi commercial (HALF_UP)
     */
    public static double round(double value, int decimals) {
        if (decimals < 0)
            throw new IllegalArgumentException("decimals must be >= 0");
        if (Double.isNaN(value) || Double.isInfinite(value))
            return Double.NaN;
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(decimals, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Calcule le score brut d'une stratégie avec filtres selon le volume.
     * Retourne 0.0 si les conditions de fiabilité ne sont pas respectées.
     *
     * @param countTotal   Nombre total de trades
     * @param countTP      Nombre de trades gagnants
     * @param countSL      Nombre de trades perdants
     * @param profitFactor Profit factor (gains / pertes), sera plafonné à 5.0
     * @return Score brut (>= 0), ou 0.0 si non conforme
     */
    public static double computeScoreBrut(int countTotal, int countTP, int countSL, double profitFactor) {
        // Validation des entrées
        if (countTotal <= 0 || countTP < 0 || countSL < 0 || countTP > countTotal || countSL > countTotal) {
            return 0.0;
        }

        if (profitFactor < 0) {
            return 0.0;
        }

        double winrate = (double) countTP / countTotal;

        // Filtres winrate selon la taille de l'échantillon
        if ((countTotal <= 10 && winrate < 1.0) ||
                (countTotal <= 100 && winrate < 0.90) ||
                (countTotal <= 1000 && winrate < 0.80) ||
                (countTotal <= 10000 && winrate < 0.70) ||
                (countTotal > 10000 && winrate < 0.60)) {
            return 0.0;
        }

        // Calcul du score
        double cappedPF = Math.min(profitFactor, 5.0);
        double logVolume = Math.log(1.0 + countTotal);
        double winrateTerm = (2.0 * winrate - 1.0);
        double logPF = Math.log(1.0 + cappedPF);

        double score = logVolume * winrateTerm * logPF;

        return Double.isFinite(score) && score > 0 ? score : 0.0;
    }
}
