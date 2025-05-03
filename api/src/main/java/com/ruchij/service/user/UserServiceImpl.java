package com.ruchij.service.user;

import com.ruchij.dao.user.UserDao;
import com.ruchij.dao.user.models.User;
import com.ruchij.dao.user.models.UserCredentials;
import com.ruchij.exception.ResourceConflictException;
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
    public User registerUser() throws ResourceConflictException {
        return this.registerUser(
            this.randomGenerator.username(),
            this.randomGenerator.password(),
            Optional.empty()
        );
    }

    @Override
    public User registerUser(String username, String password, String email) throws ResourceConflictException {
        return this.registerUser(username, password, Optional.of(email));
    }

    private User registerUser(String username, String password, Optional<String> email) throws ResourceConflictException {
        boolean isExistingUsername = this.userDao.findByUsername(username).isPresent();

        if (isExistingUsername) {
            throw new ResourceConflictException("username=%s already exists".formatted(username));
        }

        boolean isExistingEmail = email.flatMap(this.userDao::findByEmail).isPresent();

        if (isExistingEmail) {
            throw new ResourceConflictException("email=%s already exists".formatted(email.get()));
        }

        User user = new User(
            this.randomGenerator.uuid().toString(),
            username,
            email,
            this.clock.instant()
        );

        String hashedPassword = this.passwordHashingService.hashPassword(password);
        UserCredentials userCredentials = new UserCredentials(user.id(), hashedPassword);

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
