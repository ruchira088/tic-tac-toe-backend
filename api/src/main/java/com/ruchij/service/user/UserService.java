package com.ruchij.service.user;


import com.ruchij.dao.user.models.User;
import com.ruchij.exception.ResourceNotFoundException;

import java.util.Optional;

public interface UserService {
    User registerUser(Optional<String> name);

    User getUserById(String userId) throws ResourceNotFoundException;
}
