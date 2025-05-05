package com.ruchij.api.service.user;


import com.ruchij.api.dao.user.models.User;
import com.ruchij.api.exception.ResourceConflictException;
import com.ruchij.api.exception.ResourceNotFoundException;

public interface UserService {
    User registerUser() throws ResourceConflictException;

    User registerUser(String username, String password, String email) throws ResourceConflictException;

    User getUserById(String userId) throws ResourceNotFoundException;
}
