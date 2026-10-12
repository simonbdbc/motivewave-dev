package custom_studies.strategies.strategy_core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.motivewave.platform.sdk.common.DataSeries;

import custom_studies.strategies.strategy_core.HelperFormat.ColorCountResult;

public class HelperStrat {

    private static final String GREEN = "green";
    private static final String RED = "red";

    private HelperStrat() {
        throw new UnsupportedOperationException("Classe utilitaire - ne doit pas être instanciée");
    }

    public static boolean estSuperieurOuEgal(Double[] tableau) {
        for (int i = 0; i < tableau.length - 1; i++) {
            if (tableau[i] < tableau[i + 1]) {
                return false;
            }
        }
        return true;
    }

    public static boolean estInferieurOuEgal(Double[] tableau) {
        for (int i = 0; i < tableau.length - 1; i++) {
            if (tableau[i] > tableau[i + 1]) {
                return false;
            }
        }
        return true;
    }

    // public class ArrayCounter {
    //     private static final String GREEN = "green";
    //     private static final String RED = "red";

    //     public static Map<String, Integer> countColors(String[] colors) {

    //         int greenCount = 0;
    //         int redCount = 0;

    //         for (String color : colors) {
    //             if (color.equals(GREEN)) {
    //                 greenCount++;
    //             } else if (color.equals(RED)) {
    //                 redCount++;
    //             } else {
    //                 throw new IllegalArgumentException(
    //                         "Le tableau ne doit contenir que 'green' ou 'red'. Valeur invalide : " + color);
    //             }
    //         }

    //         Map<String, Integer> result = new HashMap<>();
    //         result.put(GREEN, greenCount);
    //         result.put(RED, redCount);
    //         return result;
    //     }
    // }

    // private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    // public static long getTimestampFromIndex(int index, DataSeries series) {
    //     if (series == null || index < 0 || index >= series.size()) {
    //         return null;
    //     }

    //     long timeMillis = series.getEndTime(index);
    //     // Date date = new Date(timeMillis);

    //     return timeMillis;

    // }

    public static ColorCountResult countColors(String[] colors) {
        int greenCount = 0;
        int redCount = 0;

        for (String color : colors) {
            if (GREEN.equals(color)) {
                greenCount++;
            } else if (RED.equals(color)) {
                redCount++;
            } else {
                throw new IllegalArgumentException("Valeur invalide : " + color);
            }
        }

        return new ColorCountResult(greenCount, redCount);
    }

    public static ColorCountResult countColorConsecutive(String[] colors) {
        if (colors == null || colors.length == 0) {
            return new ColorCountResult(0, 0);
        }

        String firstColor = colors[0];
        int count = 0;

        for (String color : colors) {
            if (!Objects.equals(color, firstColor)) {
                break;
            }
            count++;
        }

        if (GREEN.equals(firstColor)) {
            return new ColorCountResult(count, 0);
        } else if (RED.equals(firstColor)) {
            return new ColorCountResult(0, count);
        } else {
            throw new IllegalArgumentException("Valeur invalide : " + firstColor);
        }
    }

}
