package com.ruchij.api.web.middleware;

import com.ruchij.api.dao.user.models.User;
import com.ruchij.api.exception.AuthenticationException;
import com.ruchij.api.service.auth.AuthenticationService;
import com.ruchij.api.web.responses.SseEvent;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.http.Context;
import io.javalin.http.Header;
import io.javalin.websocket.WsConnectContext;

public class Authenticator {
    public static final String COOKIE_NAME = "auth_token";
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

    private User authenticateWithCookie(Context context) throws AuthenticationException {
        String authToken = context.cookie(COOKIE_NAME);

        if (authToken == null) {
            throw new AuthenticationException("Missing %s cookie".formatted(COOKIE_NAME));
        }

        User user = this.authenticationService.authenticate(authToken);

        return user;
    }

    private User authenticate(Context context) throws Exception {
        String token = Authenticator.getToken(context);
        User user = this.authenticationService.authenticate(token);

        return user;
    }

    public void sse(String path, AuthSseHandler authSseHandler) {
        ApiBuilder.sse(path, sseClient -> {
            try {
                User user = this.authenticateWithCookie(sseClient.ctx());
                sseClient.keepAlive();
                authSseHandler.handle(user, sseClient);
            } catch (AuthenticationException authenticationException) {
                sseClient.sendEvent(SseEvent.AUTH_ERROR.name(), authenticationException.getMessage());
                sseClient.close();
            }
        });
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
