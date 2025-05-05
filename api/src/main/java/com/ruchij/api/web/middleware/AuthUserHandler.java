package com.ruchij.api.web.middleware;

import com.ruchij.api.dao.user.models.User;
import io.javalin.http.Context;

public interface AuthUserHandler {
    void handle(User user, Context context) throws Exception;
}
