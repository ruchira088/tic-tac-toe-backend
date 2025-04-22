package com.ruchij.dao.user.models;

import org.bson.codecs.pojo.annotations.BsonId;

import java.time.Instant;

public record User(@BsonId String id, String name, Instant createdAt) {
}
