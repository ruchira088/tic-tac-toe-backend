package com.ruchij.api.web.requests;

public record UserRegistrationRequest(
    String username,
    String password,
    String email
) {
}
