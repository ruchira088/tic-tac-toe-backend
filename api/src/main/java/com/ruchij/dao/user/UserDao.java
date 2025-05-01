package com.ruchij.dao.user;

import com.ruchij.dao.user.models.User;
import com.ruchij.dao.user.models.UserCredentials;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    User insert(User user);

    UserCredentials insert(UserCredentials userCredentials);

    Optional<UserCredentials> findCredentialsById(String userId);

    Optional<User> findById(String userId);

    Optional<User> findByUsername(String username);

    List<User> searchByUsername(String username);
}
