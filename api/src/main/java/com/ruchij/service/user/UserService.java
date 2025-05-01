package com.ruchij.service.user;


import com.ruchij.dao.user.models.User;
import com.ruchij.exception.ResourceNotFoundException;

public interface UserService {
    User registerUser();

    User registerUser(String username, String password, String email);

    User getUserById(String userId) throws ResourceNotFoundException;
}
