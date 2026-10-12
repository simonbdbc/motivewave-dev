package sbdev.joy_ai_project.strategy.service.history;

import java.awt.Color;

import com.motivewave.platform.sdk.common.*;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

@StudyHeader(namespace = "sbdev", id = "RENKO_LIVE_STUDY", name = "Renko Live Example", desc = "Affiche RenkoResult en live via TickOperation", menu = "SB Dev", overlay = true)
public class RenkoLiveStudy extends Study {

    @Override
    public void initialize(Defaults defaults) {
        var sd = createSD();
        var tab = sd.addTab("Display");
        var grp = tab.addGroup("Colors");
        grp.addRow(new PathDescriptor("RENKO_PATH", "Renko Path", defaults.getRed(), 1.0f, null, true, true, false));
        grp.addRow(new PathDescriptor("RENKO_PREV_PATH", "Renko Prev Close", defaults.getBlue(), 1.0f, null, true, true,
                false));

        var desc = createRD();
        desc.declarePath(RenkoValues.REAL_RENKO, "RENKO_PATH");
        desc.declarePath(RenkoValues.PREV_CLOSE, "RENKO_PREV_PATH");
        desc.declareIndicator(RenkoValues.REAL_RENKO, "Renko Path");
        desc.declareIndicator(RenkoValues.PREV_CLOSE, "Renko Prev Close");

        desc.setRangeKeys(RenkoValues.REAL_RENKO, RenkoValues.PREV_CLOSE);

    }

    public void calculateValues(DataContext ctx) {
        var series = ctx.getDataSeries();
        for (int i = 0; i < series.size(); i++) {
            series.setDouble(i, RenkoValues.REAL_RENKO, (double) 10000 + 2 * i);
            series.setDouble(i, RenkoValues.PREV_CLOSE, (double) 9990 + 2 * i);
        }
    }

    @Override
    public void onTick(DataContext ctx, Tick tick) {
        var series = ctx.getDataSeries();
        int idx = series.size() - 1;

        // Pour le test, remplis toute la série :
        for (int i = 0; i < series.size(); i++) {
            series.setDouble(i, RenkoValues.REAL_RENKO, (double) tick.getPrice() + (i - idx));
            series.setDouble(i, RenkoValues.PREV_CLOSE, (double) tick.getPrice() - 10 + (i - idx));
        }
    }
    // private RenkoTickOperation renkoOp;

    // @Override
    // public void initialize(Defaults defaults) {
    // var sd = createSD();
    // var desc = createRD();
    // // Déclaration des paths (OBLIGATOIRE pour affichage !)
    // desc.declarePath(RenkoValues.REAL_RENKO, "Renko Path");
    // // desc.declareIndicator(RenkoValues.REAL_RENKO, "Renko Path",
    // // defaults.getRed());
    // desc.declarePath(RenkoValues.PREV_CLOSE, "Renko Prev Close");
    // // desc.declareIndicator(RenkoValues.PREV_CLOSE, "Renko Prev Close",
    // // defaults.getBlue());
    // desc.declareSignal(RenkoValues.CROSS_ABOVE, "Renko Cross Above");
    // desc.declareSignal(RenkoValues.CROSS_BELOW, "Renko Cross Below");
    // }

    // @Override
    // public void calculateValues(DataContext ctx) {
    // if (renkoOp == null) {
    // DataSeries series = ctx.getDataSeries();
    // int groupSize = 10; // Nombre de ticks par SimulatedBar
    // double brickSize = 4.0; // À adapter selon ton instrument
    // renkoOp = new RenkoTickOperation(series, groupSize, brickSize);
    // }
    // }

    // @Override
    // public void onTick(DataContext ctx, Tick tick) {
    // if (renkoOp == null) {
    // // Sécurité si pas encore initialisé (optionnel)
    // renkoOp = new RenkoTickOperation(ctx.getDataSeries(), 10, 4.0);
    // return;
    // }
    // renkoOp.onTick(tick);
    // }
}
