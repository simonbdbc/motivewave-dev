package sbdev.joy_ai_project.strategy.model;

import com.motivewave.platform.sdk.common.DataSeries;

public class DataSeriesBarAdapter implements IBar {
    private final DataSeries ds;
    private final int idx;

    public DataSeriesBarAdapter(DataSeries ds, int idx) {
        this.ds = ds;
        this.idx = idx;
    }

    @Override
    public long getEndTime() {
        return ds.getEndTime(idx);
    }

    @Override
    public double getOpen() {
        return ds.getOpen(idx);
    }

    @Override
    public double getClose() {
        return ds.getClose(idx);
    }

    @Override
    public double getHigh() {
        return ds.getHigh(idx);
    }

    @Override
    public double getLow() {
        return ds.getLow(idx);
    }
}
