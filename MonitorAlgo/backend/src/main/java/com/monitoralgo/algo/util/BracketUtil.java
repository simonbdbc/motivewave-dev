package com.monitoralgo.algo.util;

import com.monitoralgo.core.model.TakeProfit;
import com.monitoralgo.core.model.StopLoss;

import java.util.ArrayList;
import java.util.List;

public class BracketUtil {

    /**
     * Génère toutes les combinaisons (TakeProfit, StopLoss) sous forme de double[]
     * {tp, sl}
     * 
     * @param takeProfits Liste des TakeProfit (utilise getValuePoint())
     * @param stopLosses  Liste des StopLoss (utilise getValuePoint())
     * @return List<double[]> où chaque double[] = {tp, sl}
     */
    public static List<double[]> generateBracketPoints(List<TakeProfit> takeProfits, List<StopLoss> stopLosses) {
        List<double[]> list = new ArrayList<>();
        if (takeProfits == null || stopLosses == null)
            return list;
        for (TakeProfit tp : takeProfits) {
            for (StopLoss sl : stopLosses) {
                list.add(new double[] { tp.getValuePoint(), sl.getValuePoint() });
            }
        }
        return list;
    }
}
