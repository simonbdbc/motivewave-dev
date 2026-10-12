package dev;

import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.Instrument;
import com.motivewave.platform.sdk.common.desc.*;
import com.motivewave.platform.sdk.order_mgmt.OrderContext;

@StudyHeader(namespace = "com.mycompany.strategies", id = "CUSTOM_STRATEGY", name = "Custom Strategy", menu = "Custom Strategies", desc = "Stratégie de trading personnalisée utilisant le générateur de signaux.", strategy = true, overlay = true, signals = true)
public class CustomStrategy extends Study {

    enum Signals {
        ACHAT, VENTE
    }

    @Override
    public void initialize(Defaults defaults) {
        var sd = createSD();
        var tab = sd.addTab("Paramètres de la Stratégie");

        var inputs = tab.addGroup("Entrées");
        // inputs.addInput("Taille de la Position", Enums.InputType.DOUBLE, 1.0);
        inputs.addRow(new IntegerDescriptor("taillePosition", "Taille de la Position", 1, 1, 9999, 1));

        // var signals = createSignalDescriptor();
        // signals.addSignal("Achat", "Signal d'Achat");
        // signals.addSignal("Vente", "Signal de Vente");
        // Signals
        var desc = createRD();
        desc.exportValue(new ValueDescriptor(Signals.ACHAT, Enums.ValueType.BOOLEAN, "achat", null));
        desc.exportValue(new ValueDescriptor(Signals.VENTE, Enums.ValueType.BOOLEAN, "vente", null));

        desc.declareSignal(Signals.ACHAT, "achat");
        desc.declareSignal(Signals.VENTE, "vente");
    }

    @Override
    public void calculate(int index, DataContext ctx) {
        DataSeries series = ctx.getDataSeries();
        // int index = series.size() - 1;
        if (index < 1)
            return;

        double close = series.getClose(index);
        double prevClose = series.getClose(index - 1);

        if (close > prevClose) {
            // series.setBoolean(index, Signals.ACHAT, true);
            ctx.signal(index, Signals.ACHAT, "Prix en hausse", close);
        } else if (close < prevClose) {
            // series.setBoolean(index, Signals.VENTE, true);
            ctx.signal(index, Signals.VENTE, "Prix en baisse", close);
        }
    }

    // @Override
    // public void onActivate(OrderContext ctx) {
    //     // System.err.println("position: " + ctx.getAccountPosition());
    //     if (getSettings().isEnterOnActivate()) {
    //         DataSeries series = ctx.getDataContext().getDataSeries();
    //         int index = series.isLastBarComplete() ? series.size() - 1 : series.size() - 2;
    //         series.setBoolean(index, Signals.ACHAT, true);
    //         series.setBoolean(index, Signals.VENTE, true);

    //     }
    // }

    @Override
    public void onSignal(OrderContext ctx, Object signal) {
        Instrument instr = ctx.getInstrument();
        float position = ctx.getPositionAsFloat();
        float qty = (getSettings().getTradeLots() * instr.getDefaultQuantityAsFloat());

        // switch (getSettings().getPositionType()) {
        // case LONG: // Only Long Positions are allowed.
        if (position == 0 && signal == Signals.ACHAT) {
            // ctx.buy(qty); // Open Long Position
            debug("achat signal : " + signal);
        }
        // if (position > 0 && signal == Signals.CROSS_BELOW) {
        // ctx.sell(qty); // Close Long Position
        // }
        // break;
        // case SHORT: // Only Short Positions are allowed.
        if (position == 0 && signal == Signals.VENTE) {
            // ctx.sell(qty); // Open Short Position
            debug("vente signal : " + signal);
        }
        // if (position < 0 && signal == Signals.CROSS_ABOVE) {
        // ctx.buy(qty); // Close Short Position
        // }
        // break;
        // default: // Both Long and Short Positions Allowed
        // qty += Math.abs(position); // Stop and Reverse if there is an open position
        // if (position <= 0 && signal == Signals.CROSS_ABOVE) {
        // ctx.buy(qty); // Open Long Position
        // }
        // if (position >= 0 && signal == Signals.CROSS_BELOW) {
        // ctx.sell(qty); // Open Short Position
        // }
        debug("default signal : " + signal);
        // }
    }
}