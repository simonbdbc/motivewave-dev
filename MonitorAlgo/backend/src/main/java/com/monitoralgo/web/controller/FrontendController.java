package com.monitoralgo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class FrontendController {

    // Catch-all qui exclut API, WebSocket, assets, et fichiers statiques
    @RequestMapping(value = {
            "/{path:^(?!api|ws-logfile|static|favicon\\.ico|logo\\.svg|manifest\\.json|robots\\.txt|index\\.html|assets|images|css|js|fonts|png|jpg|jpeg|gif|webp|svg|ico|json|js|css|map).*$}/**",
            "/" // pour la racine
    })
    public String redirect(HttpServletRequest request) {
        // Tu peux loguer ici pour debug
        // System.out.println("FrontendController - catch-all: " +
        // request.getRequestURI());
        return "forward:/index.html";
    }
}
