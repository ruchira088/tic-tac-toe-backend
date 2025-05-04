package com.ruchij.web;

import com.ruchij.service.auth.AuthenticationService;
import com.ruchij.service.game.GameService;
import com.ruchij.service.health.HealthService;
import com.ruchij.service.user.UserService;
import com.ruchij.web.routes.AuthRoute;
import com.ruchij.web.routes.GameRoute;
import com.ruchij.web.routes.ServiceRoute;
import com.ruchij.web.routes.UserRoute;
import io.javalin.apibuilder.EndpointGroup;

import java.time.Clock;
import java.util.concurrent.ScheduledExecutorService;

import static io.javalin.apibuilder.ApiBuilder.path;

public class Routes implements EndpointGroup {
    private final ServiceRoute serviceRoute;
    private final UserRoute userRoute;
    private final GameRoute gameRoute;
    private final AuthRoute authRoute;

    public Routes(
        UserService userService,
        GameService gameService,
        AuthenticationService authenticationService,
        HealthService healthService,
        ScheduledExecutorService scheduledExecutorService,
        Clock clock
    ) {
        this.userRoute = new UserRoute(userService, authenticationService);
        this.serviceRoute = new ServiceRoute(healthService);
        this.gameRoute = new GameRoute(gameService, authenticationService, scheduledExecutorService, clock);
        this.authRoute = new AuthRoute(authenticationService);
    }

    public Routes(UserRoute userRoute, GameRoute gameRoute, ServiceRoute serviceRoute, AuthRoute authRoute) {
        this.userRoute = userRoute;
        this.serviceRoute = serviceRoute;
        this.gameRoute = gameRoute;
        this.authRoute = authRoute;
    }

    @Override
    public void addEndpoints() {
        path("/service", this.serviceRoute);
        path("/user", this.userRoute);
        path("/game", this.gameRoute);
        path("/auth", this.authRoute);
    }

}
