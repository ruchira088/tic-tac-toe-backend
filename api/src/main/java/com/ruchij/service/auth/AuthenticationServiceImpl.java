package com.ruchij.service.auth;

import com.ruchij.dao.auth.AuthTokenDao;
import com.ruchij.dao.auth.models.AuthToken;
import com.ruchij.dao.user.UserDao;
import com.ruchij.dao.user.models.User;
import com.ruchij.dao.user.models.UserCredentials;
import com.ruchij.exception.AuthenticationException;
import com.ruchij.exception.ResourceNotFoundException;
import com.ruchij.service.hashing.PasswordHashingService;
import com.ruchij.service.random.RandomGenerator;

import java.time.Clock;
import java.time.Instant;

public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserDao userDao;
    private final AuthTokenDao authTokenDao;
    private final PasswordHashingService passwordHashingService;
    private final RandomGenerator randomGenerator;
    private final Clock clock;

    public AuthenticationServiceImpl(
        UserDao userDao,
        AuthTokenDao authTokenDao,
        PasswordHashingService passwordHashingService,
        RandomGenerator randomGenerator,
        Clock clock
    ) {
        this.userDao = userDao;
        this.authTokenDao = authTokenDao;
        this.passwordHashingService = passwordHashingService;
        this.randomGenerator = randomGenerator;
        this.clock = clock;
    }

    @Override
    public AuthToken createAuthToken(String username, String password) throws ResourceNotFoundException, AuthenticationException {
        User user = this.userDao.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Unable to find user with username=%s".formatted(username)));

        UserCredentials userCredentials = this.userDao.findCredentialsById(user.id())
            .orElseThrow(() -> new IllegalStateException("User credentials not found for userId=%s".formatted(user.id())));

        boolean isPasswordMatch = this.passwordHashingService.verifyPassword(password, userCredentials.hashedPassword());

        if (isPasswordMatch) {
            return this.generateAuthToken(user.id());
        } else {
            throw new AuthenticationException("Invalid password for username=%s".formatted(username));
        }
    }

    @Override
    public AuthToken createAuthToken(String userId) throws ResourceNotFoundException {
        User user = this.userDao.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Unable to find user with userId=%s".formatted(userId)));

        return this.generateAuthToken(user.id());
    }

    private AuthToken generateAuthToken(String userId) {
        String token = this.randomGenerator.uuid().toString();
        Instant timestamp = this.clock.instant();
        AuthToken authToken = new AuthToken(token, userId, timestamp);

        this.authTokenDao.insert(authToken);

        return authToken;
    }

    @Override
    public User authenticate(String token) throws AuthenticationException {
        AuthToken authToken = this.authTokenDao.findByToken(token)
            .orElseThrow(() -> new AuthenticationException("Invalid token"));

        User user = this.userDao.findById(authToken.userId())
            .orElseThrow(() -> new IllegalStateException("User not found for userId=%s".formatted(authToken.userId())));

        return user;
    }

    @Override
    public User removeAuthToken(String token) throws AuthenticationException {
        User user = this.authenticate(token);
        this.authTokenDao.deleteByToken(token);

        return user;
    }
}
