package com.ruchij.api.web.responses;

import com.ruchij.api.dao.auth.models.AuthToken;
import com.ruchij.api.dao.user.models.User;

public record UserRegistrationResponse(AuthToken authToken, User user) {
}
