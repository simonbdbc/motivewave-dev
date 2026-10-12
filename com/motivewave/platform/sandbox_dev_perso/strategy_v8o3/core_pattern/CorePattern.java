package sbdev.joy_ai_project.strategy_v8o3.core_pattern;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sbdev.joy_ai_project.strategy_v8o3.model.Bracket;
import sbdev.joy_ai_project.strategy_v8o3.model.PatternData;

public class CorePattern {

    // Matcher hybride (maps imbriquées + hashmap de patterns)
    public static class PatternMatcher {
        // instrument -> interval -> bracket -> Map<clé pattern, PatternData>
        private final Map<String, Map<Integer, Map<Bracket, Map<String, PatternData>>>> masterMap = new HashMap<>();

        public PatternMatcher(List<PatternData> patterns) {
            for (PatternData p : patterns) {
                String patternKey = buildPatternKey(p.direction, p.patternRenko, p.patternHilo1, p.patternHilo2);

                masterMap.computeIfAbsent(p.instrumentKey, _ -> new HashMap<>())
                        .computeIfAbsent(p.intervalValue, _ -> new HashMap<>())
                        .computeIfAbsent(p.bracket, _ -> new HashMap<>()).put(patternKey, p);
            }
        }

        private static String buildPatternKey(String direction, String renko, String hilo1, String hilo2) {
            return direction + "|" + renko + "|" + hilo1 + "|" + hilo2;
        }

        /**
         * Match exact pour un seul bracket.
         */
        public PatternData match(String instrumentKey, int intervalValue, Bracket bracket, String direction,
                String renko, String hilo1, String hilo2) {
            Map<Integer, Map<Bracket, Map<String, PatternData>>> byInterval = masterMap.get(instrumentKey);
            if (byInterval == null)
                return null;
            Map<Bracket, Map<String, PatternData>> byBracket = byInterval.get(intervalValue);
            if (byBracket == null)
                return null;
            Map<String, PatternData> patternMap = byBracket.get(bracket);
            if (patternMap == null)
                return null;
            String key = buildPatternKey(direction, renko, hilo1, hilo2);
            return patternMap.get(key);
        }

        /**
         * Match sur une collection de brackets ou tous si null/vide. Retourne le
         * premier match trouvé.
         */
        public PatternData matchFlexible(String instrumentKey, int intervalValue, Collection<Bracket> brackets,
                String direction, String renko, String hilo1, String hilo2) {
            Map<Integer, Map<Bracket, Map<String, PatternData>>> byInterval = masterMap.get(instrumentKey);
            if (byInterval == null)
                return null;
            Map<Bracket, Map<String, PatternData>> byBracket = byInterval.get(intervalValue);
            if (byBracket == null)
                return null;

            Collection<Bracket> searchBrackets = (brackets == null || brackets.isEmpty()) ? byBracket.keySet()
                    : brackets;

            for (Bracket bracket : searchBrackets) {
                Map<String, PatternData> patternMap = byBracket.get(bracket);
                if (patternMap == null)
                    continue;

                // Gestion "direction null" : on teste toutes les directions possibles
                if (direction == null || direction.isEmpty()) {
                    for (String dirKey : new String[] { "long", "short" }) { // adapte si d'autres directions possibles
                        String key = buildPatternKey(dirKey, renko, hilo1, hilo2);
                        PatternData found = patternMap.get(key);
                        // Debug pattern key
                        // System.out.println("Recherche pattern key: " + key);
                        // for (String k : patternMap.keySet()) {
                        // System.out.println("Base key: " + k);
                        // }
                        if (found != null)
                            return found;
                    }
                    // Si jamais tu as d'autres directions que "long"/"short", on peut faire :
                    /*
                     * for (String dirKey : extractAllDirections(patternMap)) { String key =
                     * buildPatternKey(dirKey, renko, hilo1, hilo2); PatternData found =
                     * patternMap.get(key); if (found != null) return found; }
                     */
                } else {
                    String key = buildPatternKey(direction, renko, hilo1, hilo2);
                    PatternData found = patternMap.get(key);
                    if (found != null)
                        return found;
                }
            }
            return null;
        }

    }

}
