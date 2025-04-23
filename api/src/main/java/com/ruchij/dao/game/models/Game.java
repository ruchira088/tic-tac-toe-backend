package com.ruchij.dao.game.models;

import org.bson.codecs.pojo.annotations.BsonId;

import java.time.Instant;
import java.util.List;

public record Game(
        @BsonId
        String id,
        String name,
        Instant createdAt,
        String createdBy,
        Instant startedAt,
        String playerOneId,
        String playerTwoId,
        List<Move> moves
) {
    public enum Player {
        PlayerOne, PlayerTwo
    }

    public record Coordinate(int x, int y) {
    }

    public record Move(Player player, Instant performedAt, Coordinate coordinate) {
    }
}
