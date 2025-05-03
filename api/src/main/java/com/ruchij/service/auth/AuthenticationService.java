package com.ruchij.service.auth;

import com.ruchij.dao.auth.models.AuthToken;
import com.ruchij.dao.user.models.User;
import com.ruchij.exception.AuthenticationException;
import com.ruchij.exception.ResourceNotFoundException;

public interface AuthenticationService {
    AuthToken createAuthToken(String email, String password) throws ResourceNotFoundException, AuthenticationException;

    AuthToken createAuthToken(String userId) throws ResourceNotFoundException;

    User authenticate(String token) throws AuthenticationException;

    User removeAuthToken(String token) throws AuthenticationException;
}
