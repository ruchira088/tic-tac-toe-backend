package com.ruchij.web.middleware;

import com.ruchij.dao.user.models.User;
import com.ruchij.exception.AuthenticationException;
import com.ruchij.service.auth.AuthenticationService;
import io.javalin.http.Context;
import io.javalin.http.Header;
import io.javalin.websocket.WsConnectContext;

public class Authenticator {
    private static final String AUTH_TYPE = "Bearer";

    private final AuthenticationService authenticationService;

    public Authenticator(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public User authenticate(Context context) throws Exception {
        String token = Authenticator.getToken(context);
        User user = this.authenticationService.authenticate(token);

        return user;
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

    public User authenticate(WsConnectContext wsConnectContext) throws Exception {
        String authToken = wsConnectContext.cookie("authToken");

        if (authToken == null) {
            throw new AuthenticationException("Missing authToken cookie");
        }

        User user = this.authenticationService.authenticate(authToken);

        return user;
    }
}
