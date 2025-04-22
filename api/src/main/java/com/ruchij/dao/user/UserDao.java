package com.ruchij.dao.user;

import com.ruchij.dao.user.models.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    User insert(User user);

    Optional<User> findById(String userId);

    Optional<User> findByName(String name);

    List<User> searchByName(String name);
}
