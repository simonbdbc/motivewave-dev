package sbdev.joy_ai_project.pattern_strategy_algo.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantLock;
import sbdev.joy_ai_project.pattern_strategy_algo.model.PatternRenkoStrategy;
import sbdev.joy_ai_project.pattern_strategy_algo.model.Bracket;

public class StrategyHistoryBufferManager {

    // --------- Buffer interne générique ----------
    public static class HistoryBuffer<T> {
        private final int MAX_SIZE;
        private final ConcurrentSkipListMap<Long, T> map = new ConcurrentSkipListMap<>();
        private final ReentrantLock lock = new ReentrantLock();

        public HistoryBuffer(int maxSize) {
            this.MAX_SIZE = maxSize;
        }

        public boolean addIfNewAndIncreasing(Long key, T value) {
            if (key == null || value == null) {
                // System.err.println("[HistoryBuffer] IGNORE null: key=" + key + " value=" +
                // value);
                return false;
            }
            lock.lock();
            try {
                if (map.containsKey(key))
                    return false;
                if (!map.isEmpty() && key <= map.lastKey())
                    return false;
                map.put(key, value);
                while (map.size() > MAX_SIZE) {
                    map.pollFirstEntry();
                }
                return true;
            } finally {
                lock.unlock();
            }
        }

        public T[] getLastN(int n, java.util.function.IntFunction<T[]> generator) {
            lock.lock();
            try {
                T[] all = map.values().toArray(generator.apply(0));
                int len = Math.min(all.length, n);
                T[] arr = generator.apply(len);
                System.arraycopy(all, all.length - len, arr, 0, len);
                return arr;
            } finally {
                lock.unlock();
            }
        }

        public int size() {
            lock.lock();
            try {
                return map.size();
            } finally {
                lock.unlock();
            }
        }

        public Long getLastKey() {
            lock.lock();
            try {
                return map.isEmpty() ? null : map.lastKey();
            } finally {
                lock.unlock();
            }
        }

        public T getLastValue() {
            lock.lock();
            try {
                return map.isEmpty() ? null : map.lastEntry().getValue();
            } finally {
                lock.unlock();
            }
        }

        public void clear() {
            lock.lock();
            try {
                map.clear();
            } finally {
                lock.unlock();
            }
        }

    }

    // --------- Buffers pour chaque historique ---------
    private static final int MAX_HISTORY = 8;

    private final HistoryBuffer<String> crossHistory = new HistoryBuffer<>(MAX_HISTORY);
    private final HistoryBuffer<String> renkoColorHistory = new HistoryBuffer<>(MAX_HISTORY);
    private final HistoryBuffer<Double> renkoPriceResHistory = new HistoryBuffer<>(MAX_HISTORY);
    private final HistoryBuffer<Double> renkoPriceSupHistory = new HistoryBuffer<>(MAX_HISTORY);
    private final HistoryBuffer<Double> hiLoPrice1History = new HistoryBuffer<>(MAX_HISTORY);
    private final HistoryBuffer<Double> hiLoPrice2History = new HistoryBuffer<>(MAX_HISTORY);
    private final HistoryBuffer<String> hiLoColor1History = new HistoryBuffer<>(MAX_HISTORY);
    private final HistoryBuffer<String> hiLoColor2History = new HistoryBuffer<>(MAX_HISTORY);
    private final HistoryBuffer<Double> dowLineUpPriceHistory = new HistoryBuffer<>(MAX_HISTORY);
    private final HistoryBuffer<Double> dowLineDownPriceHistory = new HistoryBuffer<>(MAX_HISTORY);

    // --------- Méthodes d’update pour chaque historique ---------
    public boolean updateCrossHistory(Long key, String value) {
        return crossHistory.addIfNewAndIncreasing(key, value);
    }

    public boolean updateRenkoColorHistory(Long key, String value) {
        return renkoColorHistory.addIfNewAndIncreasing(key, value);
    }

    public boolean updateRenkoPriceResHistory(Long key, Double value) {
        return renkoPriceResHistory.addIfNewAndIncreasing(key, value);
    }

    public boolean updateRenkoPriceSupHistory(Long key, Double value) {
        return renkoPriceSupHistory.addIfNewAndIncreasing(key, value);
    }

    public boolean updateHiLoPrice1History(Long key, Double value) {
        return hiLoPrice1History.addIfNewAndIncreasing(key, value);
    }

    public boolean updateHiLoPrice2History(Long key, Double value) {
        return hiLoPrice2History.addIfNewAndIncreasing(key, value);
    }

    public boolean updateHiLoColor1History(Long key, String value) {
        return hiLoColor1History.addIfNewAndIncreasing(key, value);
    }

    public boolean updateHiLoColor2History(Long key, String value) {
        return hiLoColor2History.addIfNewAndIncreasing(key, value);
    }

    public boolean updateDowLineUpPriceHistory(Long key, Double value) {
        return dowLineUpPriceHistory.addIfNewAndIncreasing(key, value);
    }

    public boolean updateDowLineDownPriceHistory(Long key, Double value) {
        return dowLineDownPriceHistory.addIfNewAndIncreasing(key, value);
    }

    // --------- Extraction des N derniers éléments (FIFO du plus ancien au plus
    // récent) ---------
    public String[] getLastNCross(int length) {
        return crossHistory.getLastN(length, String[]::new);
    }

    public String[] getLastNRenkoColor(int length) {
        return renkoColorHistory.getLastN(length, String[]::new);
    }

    public Double[] getLastNRenkoPriceRes(int length) {
        return renkoPriceResHistory.getLastN(length, Double[]::new);
    }

    public Double[] getLastNRenkoPriceSup(int length) {
        return renkoPriceSupHistory.getLastN(length, Double[]::new);
    }

    public Double[] getLastNHiLoPrice1(int length) {
        return hiLoPrice1History.getLastN(length, Double[]::new);
    }

    public Double[] getLastNHiLoPrice2(int length) {
        return hiLoPrice2History.getLastN(length, Double[]::new);
    }

    public String[] getLastNHiLoColor1(int length) {
        return hiLoColor1History.getLastN(length, String[]::new);
    }

    public String[] getLastNHiLoColor2(int length) {
        return hiLoColor2History.getLastN(length, String[]::new);
    }

    public Double[] getLastNDowLineUpPrice(int length) {
        return dowLineUpPriceHistory.getLastN(length, Double[]::new);
    }

    public Double[] getLastNDowLineDownPrice(int length) {
        return dowLineDownPriceHistory.getLastN(length, Double[]::new);
    }

    // --------- Getters de taille pour chaque buffer ---------
    public int getCrossHistorySize() {
        return crossHistory.size();
    }

    public int getRenkoColorHistorySize() {
        return renkoColorHistory.size();
    }

    public int getRenkoPriceResHistorySize() {
        return renkoPriceResHistory.size();
    }

    public int getRenkoPriceSupHistorySize() {
        return renkoPriceSupHistory.size();
    }

    public int getHiLoPrice1HistorySize() {
        return hiLoPrice1History.size();
    }

    public int getHiLoPrice2HistorySize() {
        return hiLoPrice2History.size();
    }

    public int getHiLoColor1HistorySize() {
        return hiLoColor1History.size();
    }

    public int getHiLoColor2HistorySize() {
        return hiLoColor2History.size();
    }

    public int getDowLineUpPriceHistorySize() {
        return dowLineUpPriceHistory.size();
    }

    public int getDowLineDownPriceHistorySize() {
        return dowLineDownPriceHistory.size();
    }

    // --------- Getters de taille MAX pour la liste de buffer ---------
    public boolean allBuffersFull() {
        return crossHistory.size() >= MAX_HISTORY && renkoColorHistory.size() >= MAX_HISTORY
                && renkoPriceResHistory.size() >= MAX_HISTORY && renkoPriceSupHistory.size() >= MAX_HISTORY
                && hiLoPrice1History.size() >= MAX_HISTORY && hiLoPrice2History.size() >= MAX_HISTORY
                && hiLoColor1History.size() >= MAX_HISTORY && hiLoColor2History.size() >= MAX_HISTORY
        // && dowLineUpPriceHistory.size() >= MAX_HISTORY &&
        // dowLineDownPriceHistory.size() >= MAX_HISTORY
        ;
    }

    // --------- Getters dernière clé pour chaque buffer ---------
    public Long getLastKeyCross() {
        return crossHistory.getLastKey();
    }

    public Long getLastKeyRenkoColor() {
        return renkoColorHistory.getLastKey();
    }

    public Long getLastKeyRenkoPriceRes() {
        return renkoPriceResHistory.getLastKey();
    }

    public Long getLastKeyRenkoPriceSup() {
        return renkoPriceSupHistory.getLastKey();
    }

    public Long getLastKeyHiLoPrice1() {
        return hiLoPrice1History.getLastKey();
    }

    public Long getLastKeyHiLoPrice2() {
        return hiLoPrice2History.getLastKey();
    }

    public Long getLastKeyHiLoColor1() {
        return hiLoColor1History.getLastKey();
    }

    public Long getLastKeyHiLoColor2() {
        return hiLoColor2History.getLastKey();
    }

    public Long getLastKeyDowLineUpPrice() {
        return dowLineUpPriceHistory.getLastKey();
    }

    public Long getLastKeyDowLineDownPrice() {
        return dowLineDownPriceHistory.getLastKey();
    }

    // --------- Getters dernière valeur pour chaque buffer ---------
    public String getLastValueCross() {
        return crossHistory.getLastValue();
    }

    public String getLastValueRenkoColor() {
        return renkoColorHistory.getLastValue();
    }

    public Double getLastValueRenkoPriceRes() {
        return renkoPriceResHistory.getLastValue();
    }

    public Double getLastValueRenkoPriceSup() {
        return renkoPriceSupHistory.getLastValue();
    }

    public Double getLastValueHiLoPrice1() {
        return hiLoPrice1History.getLastValue();
    }

    public Double getLastValueHiLoPrice2() {
        return hiLoPrice2History.getLastValue();
    }

    public String getLastValueHiLoColor1() {
        return hiLoColor1History.getLastValue();
    }

    public String getLastValueHiLoColor2() {
        return hiLoColor2History.getLastValue();
    }

    public Double getLastValueDowLineUpPrice() {
        return dowLineUpPriceHistory.getLastValue();
    }

    public Double getLastValueDowLineDownPrice() {
        return dowLineDownPriceHistory.getLastValue();
    }

    // --------- Getters pour récupérer la valeur à un timestamp donné ---------

    public String getCrossAt(Long timestamp) {
        return getValueAtTimestamp(crossHistory, timestamp, String.class);
    }

    public String getRenkoColorAt(Long timestamp) {
        return getValueAtTimestamp(renkoColorHistory, timestamp, String.class);
    }

    public Double getRenkoPriceResAt(Long timestamp) {
        return getValueAtTimestamp(renkoPriceResHistory, timestamp, Double.class);
    }

    public Double getRenkoPriceSupAt(Long timestamp) {
        return getValueAtTimestamp(renkoPriceSupHistory, timestamp, Double.class);
    }

    public Double getHiLoPrice1At(Long timestamp) {
        return getValueAtTimestamp(hiLoPrice1History, timestamp, Double.class);
    }

    public Double getHiLoPrice2At(Long timestamp) {
        return getValueAtTimestamp(hiLoPrice2History, timestamp, Double.class);
    }

    public String getHiLoColor1At(Long timestamp) {
        return getValueAtTimestamp(hiLoColor1History, timestamp, String.class);
    }

    public String getHiLoColor2At(Long timestamp) {
        return getValueAtTimestamp(hiLoColor2History, timestamp, String.class);
    }

    public Double getDowLineUpPriceAt(Long timestamp) {
        return getValueAtTimestamp(dowLineUpPriceHistory, timestamp, Double.class);
    }

    public Double getDowLineDownPriceAt(Long timestamp) {
        return getValueAtTimestamp(dowLineDownPriceHistory, timestamp, Double.class);
    }

    // Helper générique, typé, thread-safe
    private <T> T getValueAtTimestamp(HistoryBuffer<T> buf, Long timestamp, Class<T> clazz) {
        if (timestamp == null)
            return null;
        buf.lock.lock();
        try {
            T val = buf.map.get(timestamp);
            return (val != null && clazz.isInstance(val)) ? val : null;
        } finally {
            buf.lock.unlock();
        }
    }

    public Map<Long, Object> getAllEntriesForBuffer(String bufferName) {
        // Map nom → buffer
        Map<String, HistoryBuffer<?>> bufferMap = Map.of("crossHistory", crossHistory, "renkoColorHistory",
                renkoColorHistory, "renkoPriceResHistory", renkoPriceResHistory, "renkoPriceSupHistory",
                renkoPriceSupHistory, "hiLoPrice1History", hiLoPrice1History, "hiLoPrice2History", hiLoPrice2History,
                "hiLoColor1History", hiLoColor1History, "hiLoColor2History", hiLoColor2History, "dowLineUpPriceHistory",
                dowLineUpPriceHistory, "dowLineDownPriceHistory", dowLineDownPriceHistory);

        HistoryBuffer<?> buffer = bufferMap.get(bufferName);
        if (buffer == null)
            return Collections.emptyMap();

        buffer.lock.lock();
        try {
            // On fait une copie pour la thread safety et éviter modification concurrente
            return new HashMap<>(buffer.map);
        } finally {
            buffer.lock.unlock();
        }
    }

    public String toString(Long timestamp, String historyBufferName) {
        // Map nom → buffer
        java.util.Map<String, HistoryBuffer<?>> bufferMap = java.util.Map.of("crossHistory", crossHistory,
                "renkoColorHistory", renkoColorHistory, "renkoPriceResHistory", renkoPriceResHistory,
                "renkoPriceSupHistory", renkoPriceSupHistory, "hiLoPrice1History", hiLoPrice1History,
                "hiLoPrice2History", hiLoPrice2History, "hiLoColor1History", hiLoColor1History, "hiLoColor2History",
                hiLoColor2History
        // "dowLineUpPriceHistory", dowLineUpPriceHistory,
        // "dowLineDownPriceHistory", dowLineDownPriceHistory
        );

        int max = MAX_HISTORY; // Ou adapte si tu veux un param

        if (historyBufferName != null && !historyBufferName.isEmpty()) {
            HistoryBuffer<?> buf = bufferMap.get(historyBufferName);
            if (buf == null)
                return "[Error] Unknown buffer name: " + historyBufferName;
            Object val = getValueAtTimestamp(buf, timestamp);
            return historyBufferName + "[" + timestamp + "] = " + (val != null ? val : "N/A");
        } else {
            StringBuilder sb = new StringBuilder();
            for (var entry : bufferMap.entrySet()) {
                Object val = getValueAtTimestamp(entry.getValue(), timestamp);
                sb.append(entry.getKey()).append("[").append(timestamp).append("] = ").append(val != null ? val : "N/A")
                        .append(" \n");
            }
            // Ajout des patterns compacts
            sb.append("PatternRenko(").append(max).append(") = ").append(getLastRenkoPattern(max)).append("\n");
            sb.append("PatternHiLo1(").append(max).append(") = ").append(getLastHiLo1Pattern(max)).append("\n");
            sb.append("PatternHiLo2(").append(max).append(") = ").append(getLastHiLo2Pattern(max)).append("\n");
            return sb.toString();
        }
    }

    // Helper générique pour récupérer la valeur à un timestamp
    private Object getValueAtTimestamp(HistoryBuffer<?> buf, Long timestamp) {
        if (timestamp == null)
            return null;
        buf.lock.lock();
        try {
            return buf.map.get(timestamp);
        } finally {
            buf.lock.unlock();
        }
    }

    public void clearAllHistoryBuffers() {
        crossHistory.clear();
        renkoColorHistory.clear();
        renkoPriceResHistory.clear();
        renkoPriceSupHistory.clear();
        hiLoPrice1History.clear();
        hiLoPrice2History.clear();
        hiLoColor1History.clear();
        hiLoColor2History.clear();
        dowLineUpPriceHistory.clear();
        dowLineDownPriceHistory.clear();
    }

    // --------- Extraction patterns compacts pour PatternData ---------
    private static String normalizePattern(String[] history) {
        StringBuilder sb = new StringBuilder(history.length);
        for (String c : history) {
            if (c == null)
                sb.append('?');
            else if (c.startsWith("g") || c.startsWith("G"))
                sb.append('G');
            else if (c.startsWith("r") || c.startsWith("R"))
                sb.append('R');
            else
                sb.append('?');
        }
        return sb.toString();
    }

    public String getLastRenkoPattern(int length) {
        return normalizePattern(getLastNRenkoColor(length));
    }

    public String getLastHiLo1Pattern(int length) {
        return normalizePattern(getLastNHiLoColor1(length));
    }

    public String getLastHiLo2Pattern(int length) {
        return normalizePattern(getLastNHiLoColor2(length));
    }

    // --------- Construction PatternData ---------
    public PatternRenkoStrategy toPatternData(String instrumentKey, int intervalValue, Bracket bracket,
            String direction) {
        PatternRenkoStrategy pd = new PatternRenkoStrategy();
        pd.instrumentKey = instrumentKey;
        pd.intervalValue = intervalValue;
        pd.bracket = bracket;
        pd.direction = direction;
        pd.patternRenko = getLastRenkoPattern(MAX_HISTORY);
        pd.patternHilo1 = getLastHiLo1Pattern(MAX_HISTORY);
        pd.patternHilo2 = getLastHiLo2Pattern(MAX_HISTORY);
        return pd;
    }
}
