package com.ruchij.api.web.responses;

import java.time.Instant;

public record PingResponse(String userId, String username, Instant timestamp) {
}