package com.ruchij.dao.game.models;

import org.bson.codecs.pojo.annotations.BsonId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record Game(
    @BsonId
    String id,
    String name,
    Instant createdAt,
    String createdBy,
    Instant startedAt,
    String playerOneId,
    String playerTwoId,
    List<Move> moves,
    Optional<Winner> winner
) {
    public enum WinningRule {
        Diagonal, Horizontal, Vertical
    }

    public record Winner(String playerId, WinningRule winningRule) {
    }

    public record Coordinate(int x, int y) {
    }

    public record Move(String playerId, Instant performedAt, Coordinate coordinate) {
    }
}
