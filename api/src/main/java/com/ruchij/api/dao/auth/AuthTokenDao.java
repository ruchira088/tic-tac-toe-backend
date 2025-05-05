package com.ruchij.api.dao.auth;

import com.ruchij.api.dao.auth.models.AuthToken;

import java.util.Optional;

public interface AuthTokenDao {
    AuthToken insert(AuthToken authToken);

    Optional<AuthToken> findByToken(String token);

    Optional<AuthToken> deleteByToken(String token);
}
