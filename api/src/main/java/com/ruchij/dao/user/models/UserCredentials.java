package com.ruchij.dao.user.models;

import org.bson.codecs.pojo.annotations.BsonId;

public record UserCredentials(@BsonId String userId, String hashedPassword) {
}
