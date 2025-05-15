package com.ruchij.api.web.routes;

import com.ruchij.api.dao.auth.models.AuthToken;
import com.ruchij.api.dao.user.models.User;
import com.ruchij.api.service.auth.AuthenticationService;
import com.ruchij.api.web.middleware.Authenticator;
import com.ruchij.api.web.requests.UserLoginRequest;
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
            AuthToken authToken =
                this.authenticationService.createAuthToken(userLoginRequest.email(), userLoginRequest.password());

            context.status(201).json(authToken);
        });

        delete(context -> {
            String authToken = Authenticator.getToken(context);
            User user = this.authenticationService.removeAuthToken(authToken);

            context
                .status(200)
                .removeCookie(Authenticator.COOKIE_NAME)
                .json(user);
        });
    }
}
