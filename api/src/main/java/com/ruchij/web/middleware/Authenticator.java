package com.ruchij.web.middleware;

import com.ruchij.dao.user.models.User;
import com.ruchij.exception.AuthenticationException;
import com.ruchij.service.auth.AuthenticationService;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.http.Context;
import io.javalin.http.Header;
import io.javalin.websocket.WsConnectContext;

public class Authenticator {
    private static final String AUTH_TYPE = "Bearer";

    private final AuthenticationService authenticationService;

    public Authenticator(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public static String getToken(Context context) throws Exception {
        String authorizationHeader = context.header(Header.AUTHORIZATION);

        if (authorizationHeader == null) {
            throw new AuthenticationException("Missing %s header".formatted(Header.AUTHORIZATION));
        }

        if (!authorizationHeader.startsWith(AUTH_TYPE)) {
            throw new AuthenticationException("Unsupported auth type");
        }

        String token = authorizationHeader.substring(AUTH_TYPE.length()).trim();
        return token;
    }

    private User authenticate(Context context) throws Exception {
        String token = Authenticator.getToken(context);
        User user = this.authenticationService.authenticate(token);

        return user;
    }

    public void post(AuthUserHandler authUserHandler) {
        ApiBuilder.post(context -> {
            User user = this.authenticate(context);
            authUserHandler.handle(user, context);
        });
    }

    public void post(String path, AuthUserHandler authUserHandler) {
        ApiBuilder.post(path, context -> {
            User user = this.authenticate(context);
            authUserHandler.handle(user, context);
        });
    }

    public void get(AuthUserHandler authUserHandler) {
        ApiBuilder.get(context -> {
            User user = this.authenticate(context);
            authUserHandler.handle(user, context);
        });
    }

    public void get(String path, AuthUserHandler authUserHandler) {
        ApiBuilder.get(path, context -> {
            User user = this.authenticate(context);
            authUserHandler.handle(user, context);
        });
    }

    public User authenticate(WsConnectContext wsConnectContext) throws Exception {
        String authToken = wsConnectContext.cookie("auth_token");

        if (authToken == null) {
            throw new AuthenticationException("Missing authToken cookie");
        }

        User user = this.authenticationService.authenticate(authToken);

        return user;
    }
}
