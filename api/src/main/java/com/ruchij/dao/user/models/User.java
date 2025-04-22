package com.ruchij.dao.user.models;

import java.time.Instant;

public record User(String id, String name, Instant createdAt) {
}
