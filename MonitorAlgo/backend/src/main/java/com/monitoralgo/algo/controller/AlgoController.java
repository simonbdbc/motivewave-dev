// monitoralgo/algo/controller/AlgoController.java
package com.monitoralgo.algo.controller;

import com.monitoralgo.api.model.AlgoRunRequest;
import com.monitoralgo.api.model.AlgoRunResponse;
import com.monitoralgo.api.model.AlgoUpdateStatsResponse;
import com.monitoralgo.algo.service.AlgoService;
import com.monitoralgo.algo.service.AlgoUpdateStatsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/algo")
public class AlgoController {

    private final AlgoService algoService;
    private final AlgoUpdateStatsService algoUpdateStatsService;

    @Autowired
    public AlgoController(AlgoService algoService, AlgoUpdateStatsService algoUpdateStatsService) {
        this.algoService = algoService;
        this.algoUpdateStatsService = algoUpdateStatsService;
    }

    @PostMapping("/run")
    public AlgoRunResponse runAlgo(@RequestBody AlgoRunRequest request) {
        return algoService.runAlgo(request);
    }

    @PostMapping("/update-stats")
    public AlgoUpdateStatsResponse updateStats() {
        return algoUpdateStatsService.updatePatternStrategyStats();
    }
}
