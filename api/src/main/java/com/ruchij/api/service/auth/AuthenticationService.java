package com.ruchij.api.service.auth;

import com.ruchij.api.dao.auth.models.AuthToken;
import com.ruchij.api.dao.user.models.User;
import com.ruchij.api.exception.AuthenticationException;
import com.ruchij.api.exception.ResourceNotFoundException;

public interface AuthenticationService {
    AuthToken createAuthToken(String email, String password) throws ResourceNotFoundException, AuthenticationException;

    AuthToken createAuthToken(String userId) throws ResourceNotFoundException;

    User authenticate(String token) throws AuthenticationException;

    User removeAuthToken(String token) throws AuthenticationException;
}
