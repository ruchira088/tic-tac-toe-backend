package com.ruchij.api.dao.game.models;

import org.bson.codecs.pojo.annotations.BsonId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record Game(
    @BsonId
    String id,
    String title,
    Instant createdAt,
    String createdBy,
    Instant startedAt,
    String playerOneId,
    String playerTwoId,
    List<Move> moves,
    Optional<Winner> winner
) {
    public enum WinningRule {
        BackwardDiagonal, ForwardDiagonal, Horizontal, Vertical
    }

    public record Winner(String playerId, WinningRule winningRule, List<Coordinate> coordinates) {
    }

    public record Coordinate(int x, int y) {
    }

    public record Move(String id, String playerId, Instant performedAt, Coordinate coordinate) {
    }
}
