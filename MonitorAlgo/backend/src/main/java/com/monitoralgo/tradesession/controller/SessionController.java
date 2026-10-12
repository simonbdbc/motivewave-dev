package com.monitoralgo.tradesession.controller;

import com.monitoralgo.core.model.*;
import com.monitoralgo.core.dao.*;
import org.springframework.web.bind.annotation.*;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    // DAOs (plus de repository Spring)
    private final TradingSessionDao tradingSessionDao = new TradingSessionDao();

    @GetMapping("/all")
    public List<TradingSession> getAllTradingSessions() throws SQLException {
        return tradingSessionDao.findAll();
    }
    
    @GetMapping("/last-three")
    public List<TradingSession> getLastThreeTradingSessions() throws SQLException {
        return tradingSessionDao.findLastThree();
    }

}
