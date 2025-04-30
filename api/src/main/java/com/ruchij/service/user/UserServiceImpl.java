package com.ruchij.service.user;

import com.ruchij.dao.user.UserDao;
import com.ruchij.dao.user.models.User;
import com.ruchij.dao.user.models.UserCredentials;
import com.ruchij.exception.ResourceNotFoundException;
import com.ruchij.service.hashing.PasswordHashingService;
import com.ruchij.service.random.RandomGenerator;

import java.time.Clock;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final PasswordHashingService passwordHashingService;
    private final RandomGenerator randomGenerator;
    private final Clock clock;

    public UserServiceImpl(
        UserDao userDao, PasswordHashingService passwordHashingService,
        RandomGenerator randomGenerator,
        Clock clock
    ) {
        this.userDao = userDao;
        this.passwordHashingService = passwordHashingService;
        this.randomGenerator = randomGenerator;
        this.clock = clock;
    }

    @Override
    public User registerUser(Optional<String> username, Optional<String> password) {
        String userId = this.randomGenerator.uuid().toString();

        User user = new User(
            userId,
            username.orElseGet(this.randomGenerator::username),
            this.clock.instant()
        );

        String userPassword = password.orElseGet(this.randomGenerator::password);
        String hashedPassword = this.passwordHashingService.hashPassword(userPassword);
        UserCredentials userCredentials = new UserCredentials(userId, hashedPassword);

        this.userDao.insert(user);
        this.userDao.insert(userCredentials);

        return user;
    }

    @Override
    public User getUserById(String userId) throws ResourceNotFoundException {
        Optional<User> user = this.userDao.findById(userId);

        return user.orElseThrow(
                () -> new ResourceNotFoundException("Unable to find user with ID=%s".formatted(userId))
        );
    }
}
