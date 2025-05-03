package com.ruchij.web.routes;

import com.ruchij.dao.auth.models.AuthToken;
import com.ruchij.dao.user.models.User;
import com.ruchij.service.auth.AuthenticationService;
import com.ruchij.service.user.UserService;
import com.ruchij.web.middleware.Authenticator;
import com.ruchij.web.requests.UserRegistrationRequest;
import com.ruchij.web.responses.UserRegistrationResponse;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;

import static io.javalin.apibuilder.ApiBuilder.post;

public class UserRoute implements EndpointGroup {
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final Authenticator authenticator;

    public UserRoute(UserService userService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
        this.authenticator = new Authenticator(authenticationService);
    }

    @Override
    public void addEndpoints() {
        post(context -> {
            UserRegistrationRequest userRegistrationRequest = context.bodyAsClass(UserRegistrationRequest.class);

            User user = this.userService.registerUser(
                userRegistrationRequest.username(),
                userRegistrationRequest.password(),
                userRegistrationRequest.email()
            );

            AuthToken authToken = this.authenticationService.createAuthToken(user.id());

            context.status(HttpStatus.CREATED).json(new UserRegistrationResponse(authToken, user));
        });

        this.authenticator.get((user, context) -> {
            context.status(HttpStatus.OK).json(user);
        });

        post("/guest", context -> {
            User user = this.userService.registerUser();
            AuthToken authToken = this.authenticationService.createAuthToken(user.id());

            context.status(HttpStatus.CREATED).json(new UserRegistrationResponse(authToken, user));
        });
    }
}
