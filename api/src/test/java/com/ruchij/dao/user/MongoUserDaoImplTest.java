package com.ruchij.dao.user;

import com.github.javafaker.Faker;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.ruchij.dao.user.models.User;
import com.ruchij.dao.user.models.UserCredentials;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Testcontainers
class MongoUserDaoImplTest {
    private static final String MONGO_DB_NAME = "mongo_user_dao_test";

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:8");

    private MongoClient mongoClient;
    private UserDao userDao;

    private final Faker faker = Faker.instance();

    @BeforeAll
    static void beforeAll() {
        MONGO_DB_CONTAINER.start();
    }

    @BeforeEach
    void setUp() {
        this.mongoClient = MongoClients.create(MONGO_DB_CONTAINER.getConnectionString());
        MongoDatabase mongoDatabase = this.mongoClient.getDatabase(MONGO_DB_NAME);
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        this.userDao = new MongoUserDaoImpl(mongoDatabase, suffix);
    }

    @Test
    void insertShouldReturnUser() {
        String id = UUID.randomUUID().toString();
        String username = this.faker.name().username();
        String email = this.faker.internet().emailAddress();
        Instant createdAt = Instant.now();

        User user = new User(id, username, Optional.of(email), createdAt);
        this.userDao.insert(user);

        User result = this.userDao.findById(id).get();

        Assertions.assertEquals(id, result.id());
        Assertions.assertEquals(username, result.username());
        Assertions.assertEquals(Optional.of(email), result.email());
        Assertions.assertEquals(createdAt.getEpochSecond(), result.createdAt().getEpochSecond());
    }

    @Test
    void insertUserCredentialsAndFindById() {
        String userId = UUID.randomUUID().toString();
        String hashedPassword = this.faker.internet().password(10, 20);

        UserCredentials userCredentials = new UserCredentials(userId, hashedPassword);
        this.userDao.insert(userCredentials);

        UserCredentials result = this.userDao.findCredentialsById(userId).get();

        Assertions.assertEquals(userId, result.userId());
        Assertions.assertEquals(hashedPassword, result.hashedPassword());
    }

    @Test
    void findByUsername() {
        String id = UUID.randomUUID().toString();
        String username = this.faker.name().username();
        String email = this.faker.internet().emailAddress();
        Instant createdAt = Instant.now();

        User user = new User(id, username, Optional.of(email), createdAt);
        this.userDao.insert(user);

        User result = this.userDao.findByUsername(username).get();

        Assertions.assertEquals(id, result.id());
        Assertions.assertEquals(username, result.username());
        Assertions.assertEquals(Optional.of(email), result.email());
        Assertions.assertEquals(createdAt.getEpochSecond(), result.createdAt().getEpochSecond());
    }

    @Test
    void searchByUsername() {
        String id1 = UUID.randomUUID().toString();
        String username1 = "testuser123";
        String email1 = this.faker.internet().emailAddress();
        Instant createdAt1 = Instant.now();

        String id2 = UUID.randomUUID().toString();
        String username2 = "testuser456";
        String email2 = this.faker.internet().emailAddress();
        Instant createdAt2 = Instant.now();

        String id3 = UUID.randomUUID().toString();
        String username3 = "otheruser789";
        String email3 = this.faker.internet().emailAddress();
        Instant createdAt3 = Instant.now();

        User user1 = new User(id1, username1, Optional.of(email1), createdAt1);
        User user2 = new User(id2, username2, Optional.of(email2), createdAt2);
        User user3 = new User(id3, username3, Optional.of(email3), createdAt3);

        this.userDao.insert(user1);
        this.userDao.insert(user2);
        this.userDao.insert(user3);

        List<User> results = this.userDao.searchByUsername("testuser");

        Assertions.assertEquals(2, results.size());
        Assertions.assertTrue(results.stream().anyMatch(user -> user.id().equals(id1)));
        Assertions.assertTrue(results.stream().anyMatch(user -> user.id().equals(id2)));
        Assertions.assertFalse(results.stream().anyMatch(user -> user.id().equals(id3)));
    }

    @Test
    void findByIdShouldReturnEmptyWhenUserDoesNotExist() {
        String nonExistentId = UUID.randomUUID().toString();
        Optional<User> result = this.userDao.findById(nonExistentId);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void findByUsernameShouldReturnEmptyWhenUserDoesNotExist() {
        String nonExistentUsername = this.faker.name().username() + UUID.randomUUID();
        Optional<User> result = this.userDao.findByUsername(nonExistentUsername);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void findCredentialsByIdShouldReturnEmptyWhenCredentialsDoNotExist() {
        String nonExistentId = UUID.randomUUID().toString();
        Optional<UserCredentials> result = this.userDao.findCredentialsById(nonExistentId);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void searchByUsernameShouldReturnEmptyListWhenNoMatches() {
        String uniqueSearchTerm = "unique" + UUID.randomUUID();
        List<User> results = this.userDao.searchByUsername(uniqueSearchTerm);
        Assertions.assertTrue(results.isEmpty());
    }

    @AfterEach
    void tearDown() {
        this.mongoClient.close();
    }

    @AfterAll
    static void afterAll() {
        MONGO_DB_CONTAINER.stop();
    }
}
