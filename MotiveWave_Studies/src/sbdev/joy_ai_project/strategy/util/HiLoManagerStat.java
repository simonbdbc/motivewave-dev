package sbdev.joy_ai_project.strategy.util;

import sbdev.joy_ai_project.strategy.model.IBar;
import sbdev.joy_ai_project.strategy.model.SimulatedBar;
import sbdev.joy_ai_project.strategy.model.SimulatedBarAdapter;
import sbdev.joy_ai_project.strategy.model.HiLoResult;
import com.motivewave.platform.sdk.common.Enums;
import java.util.*;

public class HiLoManagerStat {
    private final HiLoManager core = new HiLoManager();

    public Map<Long, HiLoResult> computeHiLo(List<SimulatedBar> bars, Enums.MAMethod maMethod, int period,
            long intervalMillis) {
        List<IBar> adapted = new ArrayList<>(bars.size());
        for (SimulatedBar b : bars)
            adapted.add(new SimulatedBarAdapter(b));
        // Optionnel: pré-grouping par intervalMillis si besoin (pour bien émuler la
        // close groupée des 5min, etc.)
        return core.computeHiLo(adapted, maMethod, period);
    }
}
