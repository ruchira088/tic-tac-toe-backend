package com.ruchij.web.middleware;

import com.ruchij.dao.user.models.User;
import io.javalin.http.Context;

public interface AuthUserHandler {
    void handle(User user, Context context) throws Exception;
}
