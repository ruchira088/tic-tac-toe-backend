package com.ruchij.service.user;


import com.ruchij.dao.user.models.User;
import com.ruchij.exception.ResourceConflictException;
import com.ruchij.exception.ResourceNotFoundException;

public interface UserService {
    User registerUser() throws ResourceConflictException;

    User registerUser(String username, String password, String email) throws ResourceConflictException;

    User getUserById(String userId) throws ResourceNotFoundException;
}
