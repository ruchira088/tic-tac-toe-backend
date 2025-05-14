package com.ruchij.api.service.game;

import com.ruchij.api.dao.game.GameDao;
import com.ruchij.api.dao.game.models.Game;
import com.ruchij.api.dao.game.models.PendingGame;
import com.ruchij.api.exception.ResourceConflictException;
import com.ruchij.api.exception.ResourceNotFoundException;
import com.ruchij.api.exception.ValidationException;
import com.ruchij.api.service.random.RandomGenerator;
import com.ruchij.api.utils.ThrowableConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class GameServiceImpl implements GameService {
    private static final Logger logger = LoggerFactory.getLogger(GameServiceImpl.class);

    private final GameDao gameDao;
    private final GameEngine gameEngine;
    private final ExecutorService executorService;
    private final Clock clock;
    private final RandomGenerator randomGenerator;
    private final Map<String, Map<String, ThrowableConsumer<Game.Move, IOException>>> moveUpdates = new ConcurrentHashMap<>();
    private final Map<String, Map<String, ThrowableConsumer<Game.Winner, IOException>>> winnerUpdates = new ConcurrentHashMap<>();
    private final Map<String, String> registrationIdToGameId = new ConcurrentHashMap<>();

    public GameServiceImpl(
        GameDao gameDao,
        GameEngine gameEngine,
        ExecutorService executorService,
        Clock clock,
        RandomGenerator randomGenerator
    ) {
        this.gameDao = gameDao;
        this.gameEngine = gameEngine;
        this.executorService = executorService;
        this.clock = clock;
        this.randomGenerator = randomGenerator;
    }

    @Override
    public PendingGame createGame(String name, String playerId) {
        UUID uuid = this.randomGenerator.uuid();
        Instant instant = this.clock.instant();

        PendingGame pendingGame = new PendingGame(uuid.toString(), name, instant, playerId, Optional.empty());
        this.gameDao.insertPendingGame(pendingGame);

        return pendingGame;
    }

    @Override
    public Game startGame(String pendingGameId, String otherPlayerId)
        throws ResourceNotFoundException, ResourceConflictException {
        PendingGame pendingGame = this.gameDao.findPendingGameById(pendingGameId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Unable to find pending game with ID=%s".formatted(pendingGameId))
            );

        if (pendingGame.gameStartedAt().isPresent()) {
            throw new ResourceConflictException("Game has already started");
        }

        Instant instant = this.clock.instant();

        PendingGame updatedPendingGame = new PendingGame(
            pendingGame.id(),
            pendingGame.title(),
            pendingGame.createdAt(),
            pendingGame.createdBy(),
            Optional.of(instant)
        );

        Optional<PendingGame> updatedGame = this.gameDao.updatePendingGame(updatedPendingGame);

        if (updatedGame.isEmpty()) {
            throw new RuntimeException(
                "Pending game id=%s not found. This is most likely due to a concurrency issue"
                    .formatted(pendingGame.id())
            );
        }

        Game game = new Game(
            pendingGame.id(),
            pendingGame.title(),
            pendingGame.createdAt(),
            pendingGame.createdBy(),
            instant,
            pendingGame.createdBy(),
            otherPlayerId,
            List.of(),
            Optional.empty()
        );

        this.gameDao.insertGame(game);

        return game;
    }

    @Override
    public Game addMove(String gameId, String playerId, Game.Coordinate coordinate)
        throws ResourceNotFoundException, ValidationException {
        Game game = this.getGameById(gameId);

        if (game.winner().isPresent()) {
            throw new ValidationException("Game gameId=%s already has a winner".formatted(game.id()));
        }

        this.gameEngine.checkMove(game, playerId, coordinate);

        Instant instant = this.clock.instant();

        Game.Move move = new Game.Move(playerId, instant, coordinate);
        game.moves().add(move);

        Optional<Game.Winner> winner = this.gameEngine.getWinner(game);

        Game updatedGame =
            new Game(
                game.id(),
                game.title(),
                game.createdAt(),
                game.createdBy(),
                game.startedAt(),
                game.playerOneId(),
                game.playerTwoId(),
                game.moves(),
                winner
            );

        this.gameDao.updateGame(updatedGame)
            .orElseThrow(() ->
                new RuntimeException(
                    "Game id=%s not found. This is most likely due to a concurrency issue"
                        .formatted(game.id())
                )
            );

        this.moveUpdates.getOrDefault(gameId, new HashMap<>())
            .forEach((registrationId, moveUpdates) ->
                this.executorService.submit(() -> {
                        try {
                            moveUpdates.accept(move);
                        } catch (IOException e) {
                            this.unregisterForUpdates(registrationId);
                        } catch (Exception e) {
                            logger.error(
                                "Error notifying for move updates. gameId=%s, registrationId=%s".formatted(gameId, registrationId),
                                e
                            );
                            this.unregisterForUpdates(registrationId);
                        }
                    }
                )
            );

        winner.ifPresent(gameWinner ->
            this.winnerUpdates.getOrDefault(gameId, new HashMap<>())
                .forEach((registrationId, winnerUpdates) ->
                    this.executorService.submit(() -> {
                            try {
                                winnerUpdates.accept(gameWinner);
                            } catch (IOException e) {
                                this.unregisterForUpdates(registrationId);
                            } catch (Exception e) {
                                logger.error(
                                    "Error notifying for winner updates. gameId=%s, registrationId=%s".formatted(gameId, registrationId),
                                    e
                                );
                                this.unregisterForUpdates(registrationId);
                            }
                        }
                    )
                )
        );

        return updatedGame;
    }

    @Override
    public Game getGameById(String gameId) throws ResourceNotFoundException {
        Game game = this.gameDao.findGameById(gameId)
            .orElseThrow(() -> new ResourceNotFoundException("Game with gameId=%s not found".formatted(gameId)));

        return game;
    }

    @Override
    public PendingGame getPendingGameById(String pendingGameId) throws ResourceNotFoundException {
        PendingGame pendingGame = this.gameDao.findPendingGameById(pendingGameId)
            .orElseThrow(() -> new ResourceNotFoundException("Error finding pending game with pendingGameId=%s".formatted(pendingGameId)));

        return pendingGame;
    }

    @Override
    public String registerForUpdates(
        String gameId,
        ThrowableConsumer<Game.Move, IOException> moveUpdates,
        ThrowableConsumer<Game.Winner, IOException> winnerUpdates
    ) throws ResourceNotFoundException {
        // Check that the game exists
        this.getGameById(gameId);

        String registrationId = this.randomGenerator.uuid().toString();

        this.registrationIdToGameId.put(registrationId, gameId);

        this.moveUpdates.computeIfAbsent(gameId, __ -> new ConcurrentHashMap<>())
            .put(registrationId, moveUpdates);

        this.winnerUpdates.computeIfAbsent(gameId, __ -> new ConcurrentHashMap<>())
            .put(registrationId, winnerUpdates);

        return registrationId;
    }

    @Override
    public void unregisterForUpdates(String registrationId) {
        String gameId = this.registrationIdToGameId.remove(registrationId);

        if (gameId != null) {
            this.moveUpdates.getOrDefault(gameId, new HashMap<>()).remove(registrationId);
            this.winnerUpdates.getOrDefault(gameId, new HashMap<>()).remove(registrationId);

            logger.info("Unregistered for updates. gameId={}, registrationId={}", gameId, registrationId);
        }
    }

    @Override
    public List<PendingGame> getPendingGames(int limit, int offset) {
        return this.gameDao.getPendingGames(limit, offset);
    }
}
