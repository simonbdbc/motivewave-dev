package sbdev.joy_ai_project.strategy_v8o1.strategy_core.utils;

import java.util.Objects;

import sbdev.joy_ai_project.strategy_v8o3.strategy_core.utils.FormatResult.ColorCountResult;

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
     * Extrait le "key_group" depuis le nom de l'instrument.
     * Exemple: "MNQM5" → "MNQ", "ESM5" → "ES"
     */
    public static String extractKeyGroup(String name) {
        if (name == null || name.length() < 2) return name;

        int len = name.length();
        char lastChar = name.charAt(len - 1);
        char secondLastChar = name.charAt(len - 2);

        if (Character.isDigit(lastChar) && Character.isLetter(secondLastChar)) {
            return name.substring(0, len - 2);
        }

        return name;
    }

}
