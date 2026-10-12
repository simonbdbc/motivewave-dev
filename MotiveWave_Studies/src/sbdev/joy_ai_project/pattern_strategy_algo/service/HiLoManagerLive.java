package sbdev.joy_ai_project.pattern_strategy_algo.service;

import sbdev.joy_ai_project.pattern_strategy_algo.model.DataSeriesBarAdapter;
import sbdev.joy_ai_project.pattern_strategy_algo.model.IBar;
import sbdev.joy_ai_project.pattern_strategy_algo.model.HiLoResult;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.DataSeries;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper live HiLo pour DataSeries MotiveWave.
 */
public class HiLoManagerLive {
    private final HiLoManager core = new HiLoManager();

    public HiLoResult computeHiLo(DataSeries ds, int index, Enums.MAMethod maMethod, int period) {
        // Oui: calcule le HiLo à l'index donné en convertissant la DataSeries en liste de IBar
        if (ds == null || index < period - 1)
            return null;
        List<IBar> bars = new ArrayList<>();
        for (int i = 0; i <= index; i++) {
            bars.add(new DataSeriesBarAdapter(ds, i));
        }
        // Oui: utilise HiLoManager pour calculer le HiLo map, puis renvoie le résultat à l'endTime de l'index actuel
        var map = core.computeHiLo(bars, maMethod, period);
        return map.get(ds.getEndTime(index));
    }
}
