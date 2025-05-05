package com.ruchij.api.dao.game;

import com.github.javafaker.Faker;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.ruchij.api.dao.game.models.Game;
import com.ruchij.api.dao.game.models.PendingGame;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Testcontainers
class MongoGameDaoImplTest {
    private static final String MONGO_DB_NAME = "mongo_game_dao_test";

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:8");
    private final Faker faker = Faker.instance();
    private MongoClient mongoClient;
    private GameDao gameDao;

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
        this.gameDao = new MongoGameDaoImpl(mongoDatabase, suffix);
    }

    @Test
    void insertPendingGameShouldReturnPendingGame() {
        String id = UUID.randomUUID().toString();
        String title = "Game " + faker.lorem().word();
        Instant createdAt = Instant.now();
        String createdBy = UUID.randomUUID().toString();

        PendingGame pendingGame = new PendingGame(id, title, createdAt, createdBy, Optional.empty());
        PendingGame result = this.gameDao.insertPendingGame(pendingGame);

        Assertions.assertEquals(id, result.id());
        Assertions.assertEquals(title, result.title());
        // Compare only the seconds part of the timestamps to handle precision loss
        Assertions.assertEquals(createdAt.getEpochSecond(), result.createdAt().getEpochSecond());
        Assertions.assertEquals(createdBy, result.createdBy());
        Assertions.assertEquals(Optional.empty(), result.gameStartedAt());
    }

    @Test
    void findPendingGameByIdShouldReturnPendingGame() {
        String id = UUID.randomUUID().toString();
        String title = "Game " + faker.lorem().word();
        Instant createdAt = Instant.now();
        String createdBy = UUID.randomUUID().toString();

        PendingGame pendingGame = new PendingGame(id, title, createdAt, createdBy, Optional.empty());
        this.gameDao.insertPendingGame(pendingGame);

        Optional<PendingGame> result = this.gameDao.findPendingGameById(id);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(id, result.get().id());
        Assertions.assertEquals(title, result.get().title());
        // Compare only the seconds part of the timestamp to handle precision loss
        Assertions.assertEquals(createdAt.getEpochSecond(), result.get().createdAt().getEpochSecond());
        Assertions.assertEquals(createdBy, result.get().createdBy());
        Assertions.assertEquals(Optional.empty(), result.get().gameStartedAt());
    }

    @Test
    void findPendingGameByIdShouldReturnEmptyWhenGameDoesNotExist() {
        String nonExistentId = UUID.randomUUID().toString();
        Optional<PendingGame> result = this.gameDao.findPendingGameById(nonExistentId);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void updatePendingGameShouldReturnUpdatedPendingGame() {
        String id = UUID.randomUUID().toString();
        String title = "Game " + faker.lorem().word();
        Instant createdAt = Instant.now();
        String createdBy = UUID.randomUUID().toString();

        PendingGame pendingGame = new PendingGame(id, title, createdAt, createdBy, Optional.empty());
        this.gameDao.insertPendingGame(pendingGame);

        Instant gameStartedAt = Instant.now();
        PendingGame updatedPendingGame = new PendingGame(id, title, createdAt, createdBy, Optional.of(gameStartedAt));
        Optional<PendingGame> result = this.gameDao.updatePendingGame(updatedPendingGame);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(id, result.get().id());
        Assertions.assertEquals(title, result.get().title());
        // Compare only the seconds part of the timestamps to handle precision loss
        Assertions.assertEquals(createdAt.getEpochSecond(), result.get().createdAt().getEpochSecond());
        Assertions.assertEquals(createdBy, result.get().createdBy());
        Assertions.assertTrue(result.get().gameStartedAt().isPresent());
        Assertions.assertEquals(gameStartedAt.getEpochSecond(), result.get().gameStartedAt().get().getEpochSecond());
    }

    @Test
    void updatePendingGameShouldReturnEmptyWhenGameDoesNotExist() {
        String nonExistentId = UUID.randomUUID().toString();
        String name = "Game " + faker.lorem().word();
        Instant createdAt = Instant.now();
        String createdBy = UUID.randomUUID().toString();
        Instant gameStartedAt = Instant.now();

        PendingGame pendingGame = new PendingGame(nonExistentId, name, createdAt, createdBy, Optional.of(gameStartedAt));
        Optional<PendingGame> result = this.gameDao.updatePendingGame(pendingGame);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void insertGameShouldReturnGame() {
        String id = UUID.randomUUID().toString();
        String title = "Game " + faker.lorem().word();
        Instant createdAt = Instant.now();
        String createdBy = UUID.randomUUID().toString();
        Instant startedAt = Instant.now();
        String playerOneId = UUID.randomUUID().toString();
        String playerTwoId = UUID.randomUUID().toString();
        List<Game.Move> moves = new ArrayList<>();

        Game game = new Game(id, title, createdAt, createdBy, startedAt, playerOneId, playerTwoId, moves, Optional.empty());
        Game result = this.gameDao.insertGame(game);

        Assertions.assertEquals(id, result.id());
        Assertions.assertEquals(title, result.title());
        // Compare only the seconds part of the timestamps to handle precision loss
        Assertions.assertEquals(createdAt.getEpochSecond(), result.createdAt().getEpochSecond());
        Assertions.assertEquals(createdBy, result.createdBy());
        Assertions.assertEquals(startedAt.getEpochSecond(), result.startedAt().getEpochSecond());
        Assertions.assertEquals(playerOneId, result.playerOneId());
        Assertions.assertEquals(playerTwoId, result.playerTwoId());
        Assertions.assertEquals(moves, result.moves());
        Assertions.assertEquals(Optional.empty(), result.winner());
    }

    @Test
    void findGameByIdShouldReturnGame() {
        String id = UUID.randomUUID().toString();
        String title = "Game " + faker.lorem().word();
        Instant createdAt = Instant.now();
        String createdBy = UUID.randomUUID().toString();
        Instant startedAt = Instant.now();
        String playerOneId = UUID.randomUUID().toString();
        String playerTwoId = UUID.randomUUID().toString();
        List<Game.Move> moves = new ArrayList<>();

        Game game = new Game(id, title, createdAt, createdBy, startedAt, playerOneId, playerTwoId, moves, Optional.empty());
        this.gameDao.insertGame(game);

        Optional<Game> result = this.gameDao.findGameById(id);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(id, result.get().id());
        Assertions.assertEquals(title, result.get().title());
        // Compare only the seconds part of the timestamps to handle precision loss
        Assertions.assertEquals(createdAt.getEpochSecond(), result.get().createdAt().getEpochSecond());
        Assertions.assertEquals(createdBy, result.get().createdBy());
        Assertions.assertEquals(startedAt.getEpochSecond(), result.get().startedAt().getEpochSecond());
        Assertions.assertEquals(playerOneId, result.get().playerOneId());
        Assertions.assertEquals(playerTwoId, result.get().playerTwoId());
        Assertions.assertEquals(moves, result.get().moves());
        Assertions.assertEquals(Optional.empty(), result.get().winner());
    }

    @Test
    void findGameByIdShouldReturnEmptyWhenGameDoesNotExist() {
        String nonExistentId = UUID.randomUUID().toString();
        Optional<Game> result = this.gameDao.findGameById(nonExistentId);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void updateGameShouldReturnUpdatedGame() {
        String id = UUID.randomUUID().toString();
        String title = "Game " + faker.lorem().word();
        Instant createdAt = Instant.now();
        String createdBy = UUID.randomUUID().toString();
        Instant startedAt = Instant.now();
        String playerOneId = UUID.randomUUID().toString();
        String playerTwoId = UUID.randomUUID().toString();
        List<Game.Move> moves = new ArrayList<>();

        Game game = new Game(id, title, createdAt, createdBy, startedAt, playerOneId, playerTwoId, moves, Optional.empty());
        this.gameDao.insertGame(game);

        // Add a move
        Game.Coordinate coordinate = new Game.Coordinate(0, 0);
        Game.Move move = new Game.Move(playerOneId, Instant.now(), coordinate);
        List<Game.Move> updatedMoves = new ArrayList<>(moves);
        updatedMoves.add(move);

        // Add a winner
        Game.Winner winner = new Game.Winner(playerOneId, Game.WinningRule.Diagonal);

        Game updatedGame = new Game(id, title, createdAt, createdBy, startedAt, playerOneId, playerTwoId, updatedMoves, Optional.of(winner));
        Optional<Game> result = this.gameDao.updateGame(updatedGame);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(id, result.get().id());
        Assertions.assertEquals(title, result.get().title());
        // Compare only the seconds part of the timestamps to handle precision loss
        Assertions.assertEquals(createdAt.getEpochSecond(), result.get().createdAt().getEpochSecond());
        Assertions.assertEquals(createdBy, result.get().createdBy());
        Assertions.assertEquals(startedAt.getEpochSecond(), result.get().startedAt().getEpochSecond());
        Assertions.assertEquals(playerOneId, result.get().playerOneId());
        Assertions.assertEquals(playerTwoId, result.get().playerTwoId());
        Assertions.assertEquals(1, result.get().moves().size());
        Assertions.assertEquals(coordinate, result.get().moves().get(0).coordinate());
        Assertions.assertEquals(playerOneId, result.get().winner().get().playerId());
        Assertions.assertEquals(Game.WinningRule.Diagonal, result.get().winner().get().winningRule());
    }

    @Test
    void updateGameShouldReturnEmptyWhenGameDoesNotExist() {
        String nonExistentId = UUID.randomUUID().toString();
        String name = "Game " + faker.lorem().word();
        Instant createdAt = Instant.now();
        String createdBy = UUID.randomUUID().toString();
        Instant startedAt = Instant.now();
        String playerOneId = UUID.randomUUID().toString();
        String playerTwoId = UUID.randomUUID().toString();
        List<Game.Move> moves = new ArrayList<>();

        Game game = new Game(nonExistentId, name, createdAt, createdBy, startedAt, playerOneId, playerTwoId, moves, Optional.empty());
        Optional<Game> result = this.gameDao.updateGame(game);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getPendingGamesShouldReturnListOfPendingGames() {
        // Insert multiple pending games
        for (int i = 0; i < 5; i++) {
            String id = UUID.randomUUID().toString();
            String name = "Game " + faker.lorem().word();
            Instant createdAt = Instant.now();
            String createdBy = UUID.randomUUID().toString();

            PendingGame pendingGame = new PendingGame(id, name, createdAt, createdBy, Optional.empty());
            this.gameDao.insertPendingGame(pendingGame);
        }

        // Test with different limit and offset values
        List<PendingGame> result1 = this.gameDao.getPendingGames(3, 0);
        Assertions.assertEquals(3, result1.size());

        List<PendingGame> result2 = this.gameDao.getPendingGames(2, 3);
        Assertions.assertEquals(2, result2.size());

        List<PendingGame> result3 = this.gameDao.getPendingGames(10, 0);
        Assertions.assertEquals(5, result3.size());
    }

    @Test
    void getPendingGamesShouldReturnEmptyListWhenNoGamesExist() {
        List<PendingGame> result = this.gameDao.getPendingGames(10, 0);
        Assertions.assertTrue(result.isEmpty());
    }

    @AfterEach
    void tearDown() {
        this.mongoClient.close();
    }
}
