package com.ruchij.web.responses;

import com.ruchij.dao.auth.models.AuthToken;
import com.ruchij.dao.user.models.User;

public record UserRegistrationResponse(AuthToken authToken, User user) {
}
