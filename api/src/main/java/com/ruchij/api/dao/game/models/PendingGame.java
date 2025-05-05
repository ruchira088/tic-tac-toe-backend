package com.ruchij.api.dao.game.models;

import org.bson.codecs.pojo.annotations.BsonId;

import java.time.Instant;
import java.util.Optional;

public record PendingGame(
    @BsonId String id,
    String title,
    Instant createdAt,
    String createdBy,
    Optional<Instant> gameStartedAt
) {
}
