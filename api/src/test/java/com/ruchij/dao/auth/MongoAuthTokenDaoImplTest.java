package com.ruchij.dao.auth;

import com.github.javafaker.Faker;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.ruchij.dao.auth.models.AuthToken;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Testcontainers
class MongoAuthTokenDaoImplTest {
    private static final String MONGO_DB_NAME = "mongo_auth_token_dao_test";

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:8");
    private final Faker faker = Faker.instance();
    private MongoClient mongoClient;
    private AuthTokenDao authTokenDao;

    @BeforeAll
    static void beforeAll() {
        MONGO_DB_CONTAINER.start();
    }

    @AfterAll
    static void afterAll() {
        MONGO_DB_CONTAINER.stop();
    }

    @BeforeEach
    void setUp() {
        this.mongoClient = MongoClients.create(MONGO_DB_CONTAINER.getConnectionString());
        MongoDatabase mongoDatabase = this.mongoClient.getDatabase(MONGO_DB_NAME);
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        this.authTokenDao = new MongoAuthTokenDaoImpl(mongoDatabase, suffix);
    }

    @Test
    void insertShouldReturnAuthToken() {
        String token = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        Instant issuedAt = Instant.now();

        AuthToken authToken = new AuthToken(token, userId, issuedAt);
        AuthToken result = this.authTokenDao.insert(authToken);

        Assertions.assertEquals(token, result.token());
        Assertions.assertEquals(userId, result.userId());
        // Compare only the seconds part of the timestamps to handle precision loss
        Assertions.assertEquals(issuedAt.getEpochSecond(), result.issuedAt().getEpochSecond());
    }

    @Test
    void findByTokenShouldReturnAuthToken() {
        String token = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        Instant issuedAt = Instant.now();

        AuthToken authToken = new AuthToken(token, userId, issuedAt);
        this.authTokenDao.insert(authToken);

        Optional<AuthToken> result = this.authTokenDao.findByToken(token);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(token, result.get().token());
        Assertions.assertEquals(userId, result.get().userId());
        // Compare only the seconds part of the timestamps to handle precision loss
        Assertions.assertEquals(issuedAt.getEpochSecond(), result.get().issuedAt().getEpochSecond());
    }

    @Test
    void findByTokenShouldReturnEmptyWhenTokenDoesNotExist() {
        String nonExistentToken = UUID.randomUUID().toString();
        Optional<AuthToken> result = this.authTokenDao.findByToken(nonExistentToken);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void deleteByTokenShouldReturnDeletedAuthToken() {
        String token = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        Instant issuedAt = Instant.now();

        AuthToken authToken = new AuthToken(token, userId, issuedAt);
        this.authTokenDao.insert(authToken);

        Optional<AuthToken> result = this.authTokenDao.deleteByToken(token);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(token, result.get().token());
        Assertions.assertEquals(userId, result.get().userId());
        // Compare only the seconds part of the timestamps to handle precision loss
        Assertions.assertEquals(issuedAt.getEpochSecond(), result.get().issuedAt().getEpochSecond());

        // Verify the token is actually deleted
        Optional<AuthToken> afterDelete = this.authTokenDao.findByToken(token);
        Assertions.assertTrue(afterDelete.isEmpty());
    }

    @Test
    void deleteByTokenShouldReturnEmptyWhenTokenDoesNotExist() {
        String nonExistentToken = UUID.randomUUID().toString();
        Optional<AuthToken> result = this.authTokenDao.deleteByToken(nonExistentToken);
        Assertions.assertTrue(result.isEmpty());
    }

    @AfterEach
    void tearDown() {
        this.mongoClient.close();
    }
}