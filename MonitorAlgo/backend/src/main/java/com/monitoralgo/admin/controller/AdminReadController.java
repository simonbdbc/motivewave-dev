package com.monitoralgo.admin.controller;

import com.monitoralgo.core.model.*;
import com.monitoralgo.core.dao.*;
import org.springframework.web.bind.annotation.*;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/read")
public class AdminReadController {

    // DAOs (plus de repository Spring)
    private final InstrumentDao instrumentDao = new InstrumentDao();
    private final DirectionDao directionDao = new DirectionDao();
    private final RenkoSizeDao renkoSizeDao = new RenkoSizeDao();
    private final StopLossDao stopLossDao = new StopLossDao();
    private final TakeProfitDao takeProfitDao = new TakeProfitDao();
    private final TimeframeDao timeframeDao = new TimeframeDao();
    private final SuiteDao suiteDao = new SuiteDao();

    @GetMapping("/instruments")
    public List<Instrument> getInstruments() throws SQLException {
        return instrumentDao.findAll();
    }

    @GetMapping("/directions")
    public List<Direction> getDirections() throws SQLException {
        return directionDao.findAll();
    }

    @GetMapping("/renko-sizes")
    public List<RenkoSize> getRenkoSizes() throws SQLException {
        return renkoSizeDao.findAll();
    }

    @GetMapping("/stop-losses")
    public List<StopLoss> getStopLosses() throws SQLException {
        return stopLossDao.findAll();
    }

    @GetMapping("/take-profits")
    public List<TakeProfit> getTakeProfits() throws SQLException {
        return takeProfitDao.findAll();
    }

    @GetMapping("/timeframes")
    public List<Timeframe> getTimeframes() throws SQLException {
        return timeframeDao.findAll();
    }

    @GetMapping("/suites")
    public List<Suite> getSuites() throws SQLException {
        return suiteDao.findAll();
    }
}
