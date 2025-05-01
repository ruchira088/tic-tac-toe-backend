package com.ruchij.web.requests;

import java.util.Optional;

public record UserRegistrationRequest(
    String username,
    String password,
    String email
) {
}
