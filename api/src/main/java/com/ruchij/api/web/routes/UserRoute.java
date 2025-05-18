package com.ruchij.api.web.routes;

import com.ruchij.api.dao.auth.models.AuthToken;
import com.ruchij.api.dao.user.models.User;
import com.ruchij.api.service.auth.AuthenticationService;
import com.ruchij.api.service.user.UserService;
import com.ruchij.api.web.middleware.Authenticator;
import com.ruchij.api.web.requests.UserRegistrationRequest;
import com.ruchij.api.web.responses.UserRegistrationResponse;
import com.ruchij.api.web.responses.UsernameResponse;
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

            context.status(HttpStatus.CREATED)
                .cookie(Authenticator.COOKIE_NAME, authToken.token())
                .json(new UserRegistrationResponse(authToken, user));
        });

        this.authenticator.get((user, context) -> {
            context.status(HttpStatus.OK).json(user);
        });

        this.authenticator.get("/id/{userId}/username", ((__, context) -> {
            String userId = context.pathParam("userId");
            User user = this.userService.getUserById(userId);

            context.status(HttpStatus.OK).json(new UsernameResponse(user.username()));
        }));

        post("/guest", context -> {
            User user = this.userService.registerUser();
            AuthToken authToken = this.authenticationService.createAuthToken(user.id());

            context.status(HttpStatus.CREATED)
                .cookie(Authenticator.COOKIE_NAME, authToken.token())
                .json(new UserRegistrationResponse(authToken, user));
        });
    }
}
