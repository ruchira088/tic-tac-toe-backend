package com.ruchij.api.service.user;

import com.ruchij.api.dao.user.UserDao;
import com.ruchij.api.dao.user.models.User;
import com.ruchij.api.dao.user.models.UserCredentials;
import com.ruchij.api.exception.ResourceConflictException;
import com.ruchij.api.exception.ResourceNotFoundException;
import com.ruchij.api.service.hashing.PasswordHashingService;
import com.ruchij.api.service.random.RandomGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceImplTest {
    private static final Instant FIXED_INSTANT = Instant.parse("2023-01-01T12:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneId.of("UTC"));
    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_HASHED_PASSWORD = "hashed_password";
    private static final UUID TEST_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private UserDao userDao;
    private PasswordHashingService passwordHashingService;
    private RandomGenerator randomGenerator;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userDao = mock(UserDao.class);
        passwordHashingService = mock(PasswordHashingService.class);
        randomGenerator = mock(RandomGenerator.class);

        userService = new UserServiceImpl(
            userDao,
            passwordHashingService,
            randomGenerator,
            FIXED_CLOCK
        );

        // Common mock setups
        when(randomGenerator.uuid()).thenReturn(TEST_UUID);
        when(passwordHashingService.hashPassword(anyString())).thenReturn(TEST_HASHED_PASSWORD);
    }

    @Test
    void registerUserWithoutParametersShouldCreateUserWithRandomCredentials() throws ResourceConflictException {
        // Arrange
        String randomUsername = "random_username";
        String randomPassword = "random_password";

        when(randomGenerator.username()).thenReturn(randomUsername);
        when(randomGenerator.password()).thenReturn(randomPassword);
        when(userDao.findByUsername(randomUsername)).thenReturn(Optional.empty());

        // Act
        User result = userService.registerUser();

        // Assert
        assertEquals(TEST_UUID.toString(), result.id());
        assertEquals(randomUsername, result.username());
        assertEquals(Optional.empty(), result.email());
        assertEquals(FIXED_INSTANT, result.createdAt());

        // Verify interactions
        verify(randomGenerator).username();
        verify(randomGenerator).password();
        verify(randomGenerator).uuid();
        verify(userDao).findByUsername(randomUsername);
        verify(passwordHashingService).hashPassword(randomPassword);

        // Verify user and credentials were inserted
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<UserCredentials> credentialsCaptor = ArgumentCaptor.forClass(UserCredentials.class);

        verify(userDao).insert(userCaptor.capture());
        verify(userDao).insert(credentialsCaptor.capture());

        User capturedUser = userCaptor.getValue();
        UserCredentials capturedCredentials = credentialsCaptor.getValue();

        assertEquals(TEST_UUID.toString(), capturedUser.id());
        assertEquals(randomUsername, capturedUser.username());
        assertEquals(Optional.empty(), capturedUser.email());
        assertEquals(FIXED_INSTANT, capturedUser.createdAt());

        assertEquals(TEST_UUID.toString(), capturedCredentials.userId());
        assertEquals(TEST_HASHED_PASSWORD, capturedCredentials.hashedPassword());
    }

    @Test
    void registerUserWithParametersShouldCreateUserWithProvidedCredentials() throws ResourceConflictException {
        // Arrange
        when(userDao.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());
        when(userDao.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // Act
        User result = userService.registerUser(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL);

        // Assert
        assertEquals(TEST_UUID.toString(), result.id());
        assertEquals(TEST_USERNAME, result.username());
        assertEquals(Optional.of(TEST_EMAIL), result.email());
        assertEquals(FIXED_INSTANT, result.createdAt());

        // Verify interactions
        verify(randomGenerator).uuid();
        verify(userDao).findByUsername(TEST_USERNAME);
        verify(userDao).findByEmail(TEST_EMAIL);
        verify(passwordHashingService).hashPassword(TEST_PASSWORD);

        // Verify user and credentials were inserted
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<UserCredentials> credentialsCaptor = ArgumentCaptor.forClass(UserCredentials.class);

        verify(userDao).insert(userCaptor.capture());
        verify(userDao).insert(credentialsCaptor.capture());

        User capturedUser = userCaptor.getValue();
        UserCredentials capturedCredentials = credentialsCaptor.getValue();

        assertEquals(TEST_UUID.toString(), capturedUser.id());
        assertEquals(TEST_USERNAME, capturedUser.username());
        assertEquals(Optional.of(TEST_EMAIL), capturedUser.email());
        assertEquals(FIXED_INSTANT, capturedUser.createdAt());

        assertEquals(TEST_UUID.toString(), capturedCredentials.userId());
        assertEquals(TEST_HASHED_PASSWORD, capturedCredentials.hashedPassword());
    }

    @Test
    void registerUserShouldThrowExceptionWhenUsernameAlreadyExists() {
        // Arrange
        User existingUser = new User(
            "existing-id",
            TEST_USERNAME,
            Optional.empty(),
            Instant.now()
        );

        when(userDao.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(existingUser));

        // Act & Assert
        ResourceConflictException exception = assertThrows(
            ResourceConflictException.class,
            () -> userService.registerUser(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL)
        );

        assertEquals("username=testuser already exists", exception.getMessage());

        // Verify no inserts were made
        verify(userDao, never()).insert(any(User.class));
        verify(userDao, never()).insert(any(UserCredentials.class));
    }

    @Test
    void registerUserShouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange
        User existingUser = new User(
            "existing-id",
            "other-username",
            Optional.of(TEST_EMAIL),
            Instant.now()
        );

        when(userDao.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());
        when(userDao.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existingUser));

        // Act & Assert
        ResourceConflictException exception = assertThrows(
            ResourceConflictException.class,
            () -> userService.registerUser(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL)
        );

        assertEquals("email=test@example.com already exists", exception.getMessage());

        // Verify no inserts were made
        verify(userDao, never()).insert(any(User.class));
        verify(userDao, never()).insert(any(UserCredentials.class));
    }

    @Test
    void getUserByIdShouldReturnUserWhenExists() throws ResourceNotFoundException {
        // Arrange
        User expectedUser = new User(
            TEST_USER_ID,
            TEST_USERNAME,
            Optional.of(TEST_EMAIL),
            FIXED_INSTANT
        );

        when(userDao.findById(TEST_USER_ID)).thenReturn(Optional.of(expectedUser));

        // Act
        User result = userService.getUserById(TEST_USER_ID);

        // Assert
        assertEquals(TEST_USER_ID, result.id());
        assertEquals(TEST_USERNAME, result.username());
        assertEquals(Optional.of(TEST_EMAIL), result.email());
        assertEquals(FIXED_INSTANT, result.createdAt());

        // Verify interactions
        verify(userDao).findById(TEST_USER_ID);
    }

    @Test
    void getUserByIdShouldThrowExceptionWhenUserDoesNotExist() {
        // Arrange
        when(userDao.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userService.getUserById(TEST_USER_ID)
        );

        assertEquals("Unable to find user with ID=test-user-id", exception.getMessage());

        // Verify interactions
        verify(userDao).findById(TEST_USER_ID);
    }
}
