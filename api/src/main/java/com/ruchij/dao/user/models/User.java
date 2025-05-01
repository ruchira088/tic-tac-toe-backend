package com.ruchij.dao.user.models;

import org.bson.codecs.pojo.annotations.BsonId;

import java.time.Instant;
import java.util.Optional;

public record User(@BsonId String id, String name, Optional<String> email, Instant createdAt) {
}
