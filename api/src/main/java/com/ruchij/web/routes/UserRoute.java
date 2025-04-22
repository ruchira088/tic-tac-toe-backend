package com.ruchij.web.routes;

import com.ruchij.dao.user.models.User;
import com.ruchij.service.user.UserService;
import com.ruchij.web.middleware.Authenticator;
import com.ruchij.web.requests.UserRegistrationRequest;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class UserRoute implements EndpointGroup {
    private final UserService userService;
    private final Authenticator authenticator;

    public UserRoute(UserService userService, Authenticator authenticator) {
        this.userService = userService;
        this.authenticator = authenticator;
    }

    @Override
    public void addEndpoints() {
        post("/", context -> {
            UserRegistrationRequest userRegistrationRequest = context.bodyAsClass(UserRegistrationRequest.class);
            User user = this.userService.registerUser(userRegistrationRequest.name());
            context.status(HttpStatus.CREATED).json(user);
        });

        get("/", context -> {
            User user = this.authenticator.authenticate(context);
            context.status(HttpStatus.OK).json(user);
        });
    }
}
