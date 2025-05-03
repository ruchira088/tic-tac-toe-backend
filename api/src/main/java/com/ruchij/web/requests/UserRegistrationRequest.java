package com.ruchij.web.requests;

public record UserRegistrationRequest(
    String username,
    String password,
    String email
) {
}
