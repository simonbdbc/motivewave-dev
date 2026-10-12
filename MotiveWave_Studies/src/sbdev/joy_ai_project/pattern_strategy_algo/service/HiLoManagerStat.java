package sbdev.joy_ai_project.pattern_strategy_algo.service;

import java.util.*;
import com.motivewave.platform.sdk.common.Enums;
import sbdev.joy_ai_project.pattern_strategy_algo.model.HiLoResult;
import sbdev.joy_ai_project.pattern_strategy_algo.model.IBar;
import sbdev.joy_ai_project.pattern_strategy_algo.model.SimulatedBar;
import sbdev.joy_ai_project.pattern_strategy_algo.model.SimulatedBarAdapter;

/**
 * Utilitaire côté "stat" pour calculer HiLo sur des SimulatedBar via l'adapter
 * IBar.
 */
public class HiLoManagerStat {
    private final HiLoManager core = new HiLoManager();

    public Map<Long, HiLoResult> computeHiLoOnSimBars(List<SimulatedBar> bars, Enums.MAMethod method, int period,
            Double initialPrevHiLo) {
        List<IBar> adapted = new ArrayList<>(bars.size());
        for (SimulatedBar b : bars)
            adapted.add(new SimulatedBarAdapter(b));
        return core.computeHiLo(adapted, method, period, initialPrevHiLo);
    }
}
