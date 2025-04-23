package com.ruchij.web.routes;

import com.ruchij.service.game.GameService;
import com.ruchij.web.middleware.Authenticator;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.post;

public class GameRoute implements EndpointGroup {
    private final GameService gameService;
    private final Authenticator authenticator;

    public GameRoute(GameService gameService, Authenticator authenticator) {
        this.gameService = gameService;
        this.authenticator = authenticator;
    }

    @Override
    public void addEndpoints() {
        post("/", context -> {

        });
    }
}
