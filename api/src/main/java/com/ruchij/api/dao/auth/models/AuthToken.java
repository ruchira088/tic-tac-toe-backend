package com.ruchij.api.dao.auth.models;

import org.bson.codecs.pojo.annotations.BsonId;

import java.time.Instant;

public record AuthToken(@BsonId String token, String userId, Instant issuedAt) {
}
