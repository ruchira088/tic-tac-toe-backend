package com.ruchij.web.middleware;

import com.ruchij.dao.user.models.User;
import com.ruchij.exception.AuthenticationException;
import com.ruchij.service.user.UserService;
import io.javalin.http.Context;
import io.javalin.http.Header;

import java.util.Map;

public class Authenticator {
    private static final String AUTH_TYPE = "Bearer";

    private final UserService userService;

    public Authenticator(UserService userService) {
        this.userService = userService;
    }

    public User authenticate(Map<String, String> headerMap) throws Exception {
        String authorizationHeader = headerMap.get(Header.AUTHORIZATION);

        if (authorizationHeader == null) {
            throw new AuthenticationException("Missing %s header".formatted(Header.AUTHORIZATION));
        }

        if (!authorizationHeader.startsWith(AUTH_TYPE)) {
            throw new AuthenticationException("Unsupported auth type");
        }

        String userId = authorizationHeader.substring(AUTH_TYPE.length()).trim();

        User user = this.userService.getUserById(userId);

        return user;
    }

    public User authenticate(Context context) throws Exception {
        return this.authenticate(context.headerMap());
    }
}
