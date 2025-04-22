package com.ruchij.web.requests;

import java.util.Optional;

public record UserRegistrationRequest(Optional<String> name) {
}
