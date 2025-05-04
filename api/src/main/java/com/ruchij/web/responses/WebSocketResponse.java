package com.ruchij.web.responses;

import java.time.Instant;

public record WebSocketResponse<T>(Type type, T data) {
    public enum Type {
        WINNER,
        MOVE_UPDATE,
        PING
    }

    public record Ping(String userId, String username, Instant timestamp) {}
}
