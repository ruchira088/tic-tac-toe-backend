package com.ruchij.service.auth;

import com.ruchij.dao.user.models.User;
import com.ruchij.exception.AuthenticationException;
import com.ruchij.exception.ResourceNotFoundException;
import com.ruchij.dao.auth.models.AuthToken;

public interface AuthenticationService {
    AuthToken createAuthToken(String username, String password) throws ResourceNotFoundException, AuthenticationException;

    AuthToken createAuthToken(String userId) throws ResourceNotFoundException;

    User authenticate(String token) throws AuthenticationException;
}
