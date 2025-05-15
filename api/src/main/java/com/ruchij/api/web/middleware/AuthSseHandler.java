package com.ruchij.api.web.middleware;

import com.ruchij.api.dao.user.models.User;
import io.javalin.http.sse.SseClient;

public interface AuthSseHandler {
    void handle(User user, SseClient sseClient);
}
