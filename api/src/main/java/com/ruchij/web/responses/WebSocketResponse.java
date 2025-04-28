package com.ruchij.web.responses;

public record WebSocketResponse<T>(Type type, T data) {
    public enum Type {
        WINNER,
        MOVE_UPDATE
    }
}
