package sbdev.joy_ai_project.strategy_v8o3.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import sbdev.joy_ai_project.strategy_v8o3.config.PatternConfig;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.ColorCountResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternGroupKey;

public class Helper {

    private static final String GREEN = "green";
    private static final String RED = "red";

    private Helper() {
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

    /**
     * Extrait le "key_group" depuis le nom de l'instrument. Exemple: "MNQM5" →
     * "MNQ", "ESM5" → "ES"
     */
    public static String extractInstrumentKeyGroup(String name) {
        if (name == null || name.length() < 2)
            return name;

        int len = name.length();
        char lastChar = name.charAt(len - 1);
        char secondLastChar = name.charAt(len - 2);

        if (Character.isDigit(lastChar) && Character.isLetter(secondLastChar)) {
            return name.substring(0, len - 2);
        }

        return name;
    }

    // Méthode statique utilitaire pour lire les groupes depuis le fichier
    // properties
    public static List<PatternGroupKey> loadPatternGroupsFromSingleton() {
        List<PatternGroupKey> groups = new ArrayList<>();
        Properties props = PatternConfig.getInstance().getAllProperties();
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith("patternGroup")) {
                String[] vals = props.getProperty(key).split("_");
                if (vals.length == 3) {
                    int interval = Integer.parseInt(vals[0].trim());
                    int tp = Integer.parseInt(vals[1].trim());
                    int sl = Integer.parseInt(vals[2].trim());
                    groups.add(new PatternGroupKey(interval, tp, sl));
                }
            }
        }
        return groups;
    }

    // public static List<PatternGroupKey> loadPatternGroupsFromSingleton() {
    // List<PatternGroupKey> groups = new ArrayList<>();
    // Properties props = PatternConfig.getInstance().getAllProperties();
    // // Collecter les keys patternGroup* dans une liste temporaire
    // List<String> patternKeys = new ArrayList<>();
    // for (String key : props.stringPropertyNames()) {
    // if (key.startsWith("patternGroup")) {
    // patternKeys.add(key);
    // }
    // }
    // // Trier les clés sur leur numéro
    // patternKeys.sort(Comparator.comparingInt(k -> {
    // String num = k.replaceAll("\\D+", ""); // retire tout sauf les chiffres
    // return num.isEmpty() ? 0 : Integer.parseInt(num);
    // }));

    // // Maintenant on parcourt dans l'ordre
    // for (String key : patternKeys) {
    // String[] vals = props.getProperty(key).split("_");
    // if (vals.length == 3) {
    // int interval = Integer.parseInt(vals[0].trim());
    // int tp = Integer.parseInt(vals[1].trim());
    // int sl = Integer.parseInt(vals[2].trim());
    // groups.add(new PatternGroupKey(interval, tp, sl));
    // }
    // }
    // return groups;
    // }

}
