package com.ruchij.dao.game.models;

import org.bson.codecs.pojo.annotations.BsonId;

import java.time.Instant;
import java.util.Optional;

public record PendingGame(
        @BsonId String id,
        String name,
        Instant createdAt,
        String createdBy,
        Optional<Instant> gameStartedAt
) {
}
