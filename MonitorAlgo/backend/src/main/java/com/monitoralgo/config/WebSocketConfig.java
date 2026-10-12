package com.monitoralgo.config;

// import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

import com.monitoralgo.monitoring.websocket.LogFileWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    // @Value("${log.watcher.strategy-folder:logs/strategy}")
    // private String strategyRoot;

    // @Value("${log.watcher.motivewave-folder:logs/motivewave}")
    // private String motivewaveRoot;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(logFileWebSocketHandler(), "/ws-logfile")
                // Mettez bien ici le nom de domaine en https pour la prod
                .setAllowedOrigins("https://sbdev-backend.sbcenter.fr", "http://localhost:5173", "http://localhost:8080");
    }

    @Bean
    public LogFileWebSocketHandler logFileWebSocketHandler() {
        LogFileWebSocketHandler handler = new LogFileWebSocketHandler();
        // handler.setStrategyRoot(strategyRoot);
        // handler.setMotivewaveRoot(motivewaveRoot);
        return handler;
    }
}
