package com.ruchij.api.dao.user.models;

import java.time.Instant;
import java.util.Optional;

public record User(String id, String username, Optional<String> email, Instant createdAt) {
}
