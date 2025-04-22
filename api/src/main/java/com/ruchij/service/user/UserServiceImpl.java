package com.ruchij.service.user;

import com.ruchij.dao.user.UserDao;
import com.ruchij.dao.user.models.User;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final RandomGenerator randomGenerator;
    private final Clock clock;

    public UserServiceImpl(
            UserDao userDao,
            RandomGenerator randomGenerator,
            Clock clock
    ) {
        this.userDao = userDao;
        this.randomGenerator = randomGenerator;
        this.clock = clock;
    }

    @Override
    public User registerUser(Optional<String> name) {
        String userId = this.randomGenerator.uuid().toString();
        String username = name.orElseGet(() -> this.randomGenerator.username());
        Instant timestamp = this.clock.instant();

        User user = new User(userId, username, timestamp);

        this.userDao.insert(user);

        return user;
    }
}
