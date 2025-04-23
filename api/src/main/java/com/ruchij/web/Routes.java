package com.ruchij.web;

import com.ruchij.service.game.GameService;
import com.ruchij.service.health.HealthService;
import com.ruchij.service.user.UserService;
import com.ruchij.web.middleware.Authenticator;
import com.ruchij.web.routes.GameRoute;
import com.ruchij.web.routes.ServiceRoute;
import com.ruchij.web.routes.UserRoute;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.path;

public class Routes implements EndpointGroup {
    private final ServiceRoute serviceRoute;
    private final UserRoute userRoute;
    private final GameRoute gameRoute;

    public Routes(UserService userService, GameService gameService, HealthService healthService) {
        Authenticator authenticator = new Authenticator(userService);

        this.userRoute = new UserRoute(userService, authenticator);
        this.serviceRoute = new ServiceRoute(healthService);
        this.gameRoute = new GameRoute(gameService, authenticator);
    }

    public Routes(UserRoute userRoute, GameRoute gameRoute, ServiceRoute serviceRoute) {
        this.userRoute = userRoute;
        this.serviceRoute = serviceRoute;
        this.gameRoute = gameRoute;
    }

    @Override
    public void addEndpoints() {
        path("service", this.serviceRoute);
        path("user", this.userRoute);
        path("game", this.gameRoute);
    }

}
