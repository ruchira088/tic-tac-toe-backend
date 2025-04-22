package com.ruchij.service.user;


import com.ruchij.dao.user.models.User;

import java.util.Optional;

public interface UserService {
    User registerUser(Optional<String> name);
}
