package sbdev.joy_ai_project.strategy.util;

import sbdev.joy_ai_project.strategy.model.DataSeriesBarAdapter;
import sbdev.joy_ai_project.strategy.model.IBar;
import sbdev.joy_ai_project.strategy.model.HiLoResult;
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
        if (ds == null || index < period - 1)
            return null;
        List<IBar> bars = new ArrayList<>();
        for (int i = 0; i <= index; i++)
            bars.add(new DataSeriesBarAdapter(ds, i));
        var map = core.computeHiLo(bars, maMethod, period);
        return map.get(ds.getEndTime(index));
    }
}
