package sbdev.joy_ai_project.strategy_v8o3.util;

import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.HiLoResult;
import com.motivewave.platform.sdk.common.Enums;
import java.util.*;

public class HiLoManagerStat {
    private final HiLoManager core = new HiLoManager();

    public Map<Long, HiLoResult> computeHiLo(List<FormatResult.SimulatedBar> bars, Enums.MAMethod maMethod, int period,
            long intervalMillis) {
        List<IBar> adapted = new ArrayList<>(bars.size());
        for (FormatResult.SimulatedBar b : bars)
            adapted.add(new SimulatedBarAdapter(b));
        // Optionnel: pré-grouping par intervalMillis si besoin (pour bien émuler la
        // close groupée des 5min, etc.)
        return core.computeHiLo(adapted, maMethod, period);
    }
}
