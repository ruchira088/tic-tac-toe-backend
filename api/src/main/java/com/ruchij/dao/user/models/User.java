package com.ruchij.dao.user.models;

import org.bson.codecs.pojo.annotations.BsonId;

import java.time.Instant;
import java.util.Optional;

public record User(String id, String username, Optional<String> email, Instant createdAt) {
}
