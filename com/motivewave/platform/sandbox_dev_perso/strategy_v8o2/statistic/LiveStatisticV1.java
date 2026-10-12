package sbdev.joy_ai_project.strategy_v8o3.statistic;

import com.motivewave.platform.sdk.common.*;
import com.motivewave.platform.sdk.common.desc.*;
import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;
import sbdev.joy_ai_project.strategy_v8o3.database.StatisticDbManager;

import java.util.*;

@StudyHeader(id = "LIVE_STATISTIC_V1", namespace = "com.sbdev", name = "Live Statistic V1", desc = "Live Statistic V1", menu = "SB Dev", overlay = true, strategy = false)
public class LiveStatisticV1 extends Study {

    // --- Paramètres ---
    private static final String[] INTERVAL_KEYS = { "tickInterval1", "tickInterval2", "tickInterval3",
            "tickInterval4" };
    private static final int[] DEFAULT_INTERVALS = { 1, 5, 10, 25 };
    private static final String RENKO_POINT = "renkoPoint";

    // --- Statut Renko par intervalle ---
    private static class RenkoStatus {
        double brickSize;
        Double realRenko = null, previousClose = null;
        String lastColor = null;

        RenkoStatus(double brickSize) {
            this.brickSize = brickSize;
        }
    }

    private final Map<Integer, RenkoStatus> renkoMap = new HashMap<>();

    // --- DB Manager ---
    private final StatisticDbManager dbManager = new StatisticDbManager();

    // --- Initialisation (UI) ---
    @Override
    public void initialize(Defaults defaults) {
        var sd = createSD();
        var tab = sd.addTab("Analyse");

        // Section 1
        var section1 = tab.addGroup("Intervals");
        for (int i = 0; i < 4; i++) {
            section1.addRow(
                    new IntegerDescriptor(INTERVAL_KEYS[i], "Interval " + (i + 1), DEFAULT_INTERVALS[i], 1, 1000, 1));
        }

        // Section 2
        var section2 = tab.addGroup("General");
        section2.addRow(new IntegerDescriptor(RENKO_POINT, "Renko ticks", 20, 1, 1000, 1));
    }

    // --- Mise à jour automatique à chaque modification ou rechargement ---
    @Override
    public void onLoad(Defaults defaults) {
        reloadRenkoStatus();
    }

    @Override
    public void onSettingsUpdated(DataContext ctx) {
        reloadRenkoStatus();
    }

    private void reloadRenkoStatus() {
        renkoMap.clear();
        double brickSize = getSettings().getInteger(RENKO_POINT) / 4.0;
        for (int i = 0; i < 4; i++) {
            int interval = getSettings().getInteger(INTERVAL_KEYS[i]);
            if (interval > 0) { // par sécurité
                renkoMap.put(interval, new RenkoStatus(brickSize));
            }
        }
    }

    // --- Sur chaque tick : mise à jour Renko et snapshot si nouvelle brique ---
    @Override
    protected void calculate(int index, DataContext ctx) {
        DataSeries ds = ctx.getDataSeries();
        if (index == 0)
            return;

        double price = ds.getClose(index);
        long timestamp = ds.getEndTime(index);
        String instrument = ctx.getInstrument().getSymbol();
        // debug("instrument : " + instrument);

        for (Map.Entry<Integer, RenkoStatus> entry : renkoMap.entrySet()) {
            int interval = entry.getKey();
            RenkoStatus status = entry.getValue();
            boolean newBrick = updateRenko(status, price);
            debug("newBrick : " + newBrick);

            if (newBrick && status.lastColor != null) {
                // dbManager.saveRenkoSnapshot(
                // instrument,
                // "ticks",
                // interval,
                // timestamp,
                // status.realRenko,
                // status.lastColor
                // );
                debug("instrument : " + instrument);
                debug("interval : " + interval);
                debug("timestamp : " + timestamp);
                debug("status.realRenko : " + status.realRenko);
                debug("status.lastColor : " + status.lastColor);
            }
        }
    }

    // --- Logique Renko tick-based ---
    private boolean updateRenko(RenkoStatus s, double price) {
        boolean newBrick = false;
        if (s.realRenko == null) {
            double base = Math.floor(price / s.brickSize) * s.brickSize;
            s.realRenko = price >= base + s.brickSize ? base + s.brickSize : base;
            s.previousClose = price < s.realRenko ? s.realRenko + s.brickSize : s.realRenko - s.brickSize;
            s.lastColor = price >= s.realRenko ? "green" : "red";
        } else {
            double diff = price - s.realRenko;
            // Brick up
            if (s.lastColor.equals("green") && diff >= s.brickSize) {
                s.realRenko += s.brickSize;
                s.previousClose = s.realRenko - s.brickSize;
                s.lastColor = "green";
                newBrick = true;
            }
            // Brick down (reverse)
            else if (s.lastColor.equals("green") && diff <= -2 * s.brickSize) {
                s.realRenko -= s.brickSize;
                s.previousClose = s.realRenko + s.brickSize;
                s.lastColor = "red";
                newBrick = true;
            }
            // Brick down
            else if (s.lastColor.equals("red") && diff <= -s.brickSize) {
                s.realRenko -= s.brickSize;
                s.previousClose = s.realRenko + s.brickSize;
                s.lastColor = "red";
                newBrick = true;
            }
            // Brick up (reverse)
            else if (s.lastColor.equals("red") && diff >= 2 * s.brickSize) {
                s.realRenko += s.brickSize;
                s.previousClose = s.realRenko - s.brickSize;
                s.lastColor = "green";
                newBrick = true;
            }
        }
        return newBrick;
    }
}
