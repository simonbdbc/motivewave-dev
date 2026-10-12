package com.monitoralgo.tradesession.controller;

import com.monitoralgo.core.model.*;
import com.monitoralgo.core.dao.*;
import org.springframework.web.bind.annotation.*;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/session/read")
public class TradeReadController {

    // DAOs (plus de repository Spring)
    private final TradeStrategyLiveDao tradeStrategyLiveDao = new TradeStrategyLiveDao();

    @GetMapping("/all-trades")
    public List<TradeStrategyLive> getAllTradeStrategyLive() throws SQLException {
        return tradeStrategyLiveDao.findAllByDate();
    }

}
