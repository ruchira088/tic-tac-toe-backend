package com.ruchij.web.routes;

import com.ruchij.dao.auth.models.AuthToken;
import com.ruchij.service.auth.AuthenticationService;
import com.ruchij.web.requests.UserLoginRequest;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.post;

public class AuthRoute implements EndpointGroup {
    private final AuthenticationService authenticationService;

    public AuthRoute(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public void addEndpoints() {
        post(context -> {
            UserLoginRequest userLoginRequest = context.bodyAsClass(UserLoginRequest.class);
            AuthToken authToken = this.authenticationService.createAuthToken(userLoginRequest.username(), userLoginRequest.password());

            context.status(201).json(authToken);
        });

        delete("/token/{token}", context -> {
            String token = context.pathParam("token");
        });
    }
}
