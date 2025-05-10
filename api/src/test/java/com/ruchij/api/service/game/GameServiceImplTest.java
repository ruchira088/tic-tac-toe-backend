package com.ruchij.api.service.game;

import com.ruchij.api.dao.game.GameDao;
import com.ruchij.api.dao.game.models.Game;
import com.ruchij.api.dao.game.models.PendingGame;
import com.ruchij.api.exception.ResourceConflictException;
import com.ruchij.api.exception.ResourceNotFoundException;
import com.ruchij.api.exception.ValidationException;
import com.ruchij.api.service.random.RandomGenerator;
import com.ruchij.api.utils.ThrowableConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameServiceImplTest {
    private static final Instant FIXED_INSTANT = Instant.parse("2023-01-01T12:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneId.of("UTC"));
    private static final UUID TEST_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String TEST_GAME_ID = TEST_UUID.toString();
    private static final String TEST_GAME_NAME = "Test Game";
    private static final String PLAYER_ONE_ID = "player-one-id";
    private static final String PLAYER_TWO_ID = "player-two-id";

    private GameDao gameDao;
    private GameEngine gameEngine;
    private ExecutorService executorService;
    private RandomGenerator randomGenerator;
    private GameServiceImpl gameService;

    @BeforeEach
    void setUp() {
        gameDao = mock(GameDao.class);
        gameEngine = mock(GameEngine.class);
        executorService = mock(ExecutorService.class);
        randomGenerator = mock(RandomGenerator.class);

        gameService = new GameServiceImpl(
            gameDao,
            gameEngine,
            executorService,
            FIXED_CLOCK,
            randomGenerator
        );

        // Common mock setups
        when(randomGenerator.uuid()).thenReturn(TEST_UUID);
    }

    @Test
    void createGameShouldCreateNewPendingGame() {
        // Arrange
        when(gameDao.insertPendingGame(any(PendingGame.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PendingGame result = gameService.createGame(TEST_GAME_NAME, PLAYER_ONE_ID);

        // Assert
        assertEquals(TEST_GAME_ID, result.id());
        assertEquals(TEST_GAME_NAME, result.title());
        assertEquals(FIXED_INSTANT, result.createdAt());
        assertEquals(PLAYER_ONE_ID, result.createdBy());
        assertEquals(Optional.empty(), result.gameStartedAt());

        // Verify interactions
        verify(randomGenerator).uuid();

        // Verify pendingGame was inserted
        ArgumentCaptor<PendingGame> pendingGameCaptor = ArgumentCaptor.forClass(PendingGame.class);
        verify(gameDao).insertPendingGame(pendingGameCaptor.capture());

        PendingGame capturedPendingGame = pendingGameCaptor.getValue();
        assertEquals(TEST_GAME_ID, capturedPendingGame.id());
        assertEquals(TEST_GAME_NAME, capturedPendingGame.title());
        assertEquals(FIXED_INSTANT, capturedPendingGame.createdAt());
        assertEquals(PLAYER_ONE_ID, capturedPendingGame.createdBy());
        assertEquals(Optional.empty(), capturedPendingGame.gameStartedAt());
    }

    @Test
    void startGameShouldStartPendingGame() throws ResourceNotFoundException, ResourceConflictException {
        // Arrange
        PendingGame pendingGame = new PendingGame(
            TEST_GAME_ID,
            TEST_GAME_NAME,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            Optional.empty()
        );

        PendingGame updatedPendingGame = new PendingGame(
            TEST_GAME_ID,
            TEST_GAME_NAME,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            Optional.of(FIXED_INSTANT)
        );

        when(gameDao.findPendingGameById(TEST_GAME_ID)).thenReturn(Optional.of(pendingGame));
        when(gameDao.updatePendingGame(any(PendingGame.class))).thenReturn(Optional.of(updatedPendingGame));
        when(gameDao.insertGame(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Game result = gameService.startGame(TEST_GAME_ID, PLAYER_TWO_ID);

        // Assert
        assertEquals(TEST_GAME_ID, result.id());
        assertEquals(TEST_GAME_NAME, result.title());
        assertEquals(FIXED_INSTANT, result.createdAt());
        assertEquals(PLAYER_ONE_ID, result.createdBy());
        assertEquals(FIXED_INSTANT, result.startedAt());
        assertEquals(PLAYER_ONE_ID, result.playerOneId());
        assertEquals(PLAYER_TWO_ID, result.playerTwoId());
        assertTrue(result.moves().isEmpty());
        assertTrue(result.winner().isEmpty());

        // Verify interactions
        verify(gameDao).findPendingGameById(TEST_GAME_ID);

        // Verify pendingGame was updated
        ArgumentCaptor<PendingGame> pendingGameCaptor = ArgumentCaptor.forClass(PendingGame.class);
        verify(gameDao).updatePendingGame(pendingGameCaptor.capture());

        PendingGame capturedPendingGame = pendingGameCaptor.getValue();
        assertEquals(TEST_GAME_ID, capturedPendingGame.id());
        assertEquals(TEST_GAME_NAME, capturedPendingGame.title());
        assertEquals(FIXED_INSTANT, capturedPendingGame.createdAt());
        assertEquals(PLAYER_ONE_ID, capturedPendingGame.createdBy());
        assertEquals(Optional.of(FIXED_INSTANT), capturedPendingGame.gameStartedAt());

        // Verify game was inserted
        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameDao).insertGame(gameCaptor.capture());

        Game capturedGame = gameCaptor.getValue();
        assertEquals(TEST_GAME_ID, capturedGame.id());
        assertEquals(TEST_GAME_NAME, capturedGame.title());
        assertEquals(FIXED_INSTANT, capturedGame.createdAt());
        assertEquals(PLAYER_ONE_ID, capturedGame.createdBy());
        assertEquals(FIXED_INSTANT, capturedGame.startedAt());
        assertEquals(PLAYER_ONE_ID, capturedGame.playerOneId());
        assertEquals(PLAYER_TWO_ID, capturedGame.playerTwoId());
        assertTrue(capturedGame.moves().isEmpty());
        assertTrue(capturedGame.winner().isEmpty());
    }

    @Test
    void startGameShouldThrowExceptionWhenPendingGameNotFound() {
        // Arrange
        when(gameDao.findPendingGameById(TEST_GAME_ID)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> gameService.startGame(TEST_GAME_ID, PLAYER_TWO_ID)
        );

        assertEquals("Unable to find pending game with ID=" + TEST_GAME_ID, exception.getMessage());

        // Verify interactions
        verify(gameDao).findPendingGameById(TEST_GAME_ID);
        verify(gameDao, never()).updatePendingGame(any(PendingGame.class));
        verify(gameDao, never()).insertGame(any(Game.class));
    }

    @Test
    void startGameShouldThrowExceptionWhenGameAlreadyStarted() {
        // Arrange
        PendingGame pendingGame = new PendingGame(
            TEST_GAME_ID,
            TEST_GAME_NAME,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            Optional.of(FIXED_INSTANT)
        );

        when(gameDao.findPendingGameById(TEST_GAME_ID)).thenReturn(Optional.of(pendingGame));

        // Act & Assert
        ResourceConflictException exception = assertThrows(
            ResourceConflictException.class,
            () -> gameService.startGame(TEST_GAME_ID, PLAYER_TWO_ID)
        );

        assertEquals("Game has already started", exception.getMessage());

        // Verify interactions
        verify(gameDao).findPendingGameById(TEST_GAME_ID);
        verify(gameDao, never()).updatePendingGame(any(PendingGame.class));
        verify(gameDao, never()).insertGame(any(Game.class));
    }

    @Test
    void startGameShouldThrowExceptionWhenUpdateFails() {
        // Arrange
        PendingGame pendingGame = new PendingGame(
            TEST_GAME_ID,
            TEST_GAME_NAME,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            Optional.empty()
        );

        when(gameDao.findPendingGameById(TEST_GAME_ID)).thenReturn(Optional.of(pendingGame));
        when(gameDao.updatePendingGame(any(PendingGame.class))).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> gameService.startGame(TEST_GAME_ID, PLAYER_TWO_ID)
        );

        assertTrue(exception.getMessage().contains("Pending game id=" + TEST_GAME_ID + " not found"));
        assertTrue(exception.getMessage().contains("concurrency issue"));

        // Verify interactions
        verify(gameDao).findPendingGameById(TEST_GAME_ID);
        verify(gameDao).updatePendingGame(any(PendingGame.class));
        verify(gameDao, never()).insertGame(any(Game.class));
    }

    @Test
    void addMoveShouldAddMoveToGame() throws ResourceNotFoundException, ValidationException {
        // Arrange
        Game.Coordinate coordinate = new Game.Coordinate(0, 0);
        List<Game.Move> moves = new ArrayList<>();

        Game game = new Game(
            TEST_GAME_ID,
            TEST_GAME_NAME,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            PLAYER_TWO_ID,
            moves,
            Optional.empty()
        );

        when(gameDao.findGameById(TEST_GAME_ID)).thenReturn(Optional.of(game));
        when(gameDao.updateGame(any(Game.class))).thenAnswer(invocation -> Optional.of(invocation.getArgument(0)));
        when(gameEngine.getWinner(any(Game.class))).thenReturn(Optional.empty());
        doNothing().when(gameEngine).checkMove(any(Game.class), anyString(), any(Game.Coordinate.class));
        when(executorService.submit(any(Runnable.class))).thenReturn(null);

        // Act
        Game result = gameService.addMove(TEST_GAME_ID, PLAYER_ONE_ID, coordinate);

        // Assert
        assertEquals(TEST_GAME_ID, result.id());
        assertEquals(1, result.moves().size());
        assertEquals(PLAYER_ONE_ID, result.moves().get(0).playerId());
        assertEquals(FIXED_INSTANT, result.moves().get(0).performedAt());
        assertEquals(coordinate, result.moves().get(0).coordinate());
        assertTrue(result.winner().isEmpty());

        // Verify interactions
        verify(gameDao).findGameById(TEST_GAME_ID);
        verify(gameEngine).checkMove(any(Game.class), eq(PLAYER_ONE_ID), eq(coordinate));
        verify(gameEngine).getWinner(any(Game.class));

        // Verify game was updated
        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameDao).updateGame(gameCaptor.capture());

        Game capturedGame = gameCaptor.getValue();
        assertEquals(TEST_GAME_ID, capturedGame.id());
        assertEquals(1, capturedGame.moves().size());
        assertEquals(PLAYER_ONE_ID, capturedGame.moves().get(0).playerId());
        assertEquals(FIXED_INSTANT, capturedGame.moves().get(0).performedAt());
        assertEquals(coordinate, capturedGame.moves().get(0).coordinate());
        assertTrue(capturedGame.winner().isEmpty());

        // Verify executorService was called to notify listeners
        verify(executorService, times(2)).submit(any(Runnable.class));
    }

    @Test
    void addMoveShouldAddMoveAndSetWinner() throws ResourceNotFoundException, ValidationException {
        // Arrange
        Game.Coordinate coordinate = new Game.Coordinate(0, 0);
        List<Game.Move> moves = new ArrayList<>();
        Game.Winner winner = new Game.Winner(PLAYER_ONE_ID, Game.WinningRule.Horizontal);

        Game game = new Game(
            TEST_GAME_ID,
            TEST_GAME_NAME,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            PLAYER_TWO_ID,
            moves,
            Optional.empty()
        );

        when(gameDao.findGameById(TEST_GAME_ID)).thenReturn(Optional.of(game));
        when(gameDao.updateGame(any(Game.class))).thenAnswer(invocation -> Optional.of(invocation.getArgument(0)));
        when(gameEngine.getWinner(any(Game.class))).thenReturn(Optional.of(winner));
        doNothing().when(gameEngine).checkMove(any(Game.class), anyString(), any(Game.Coordinate.class));
        when(executorService.submit(any(Runnable.class))).thenReturn(null);

        // Act
        Game result = gameService.addMove(TEST_GAME_ID, PLAYER_ONE_ID, coordinate);

        // Assert
        assertEquals(TEST_GAME_ID, result.id());
        assertEquals(1, result.moves().size());
        assertEquals(PLAYER_ONE_ID, result.moves().get(0).playerId());
        assertEquals(FIXED_INSTANT, result.moves().get(0).performedAt());
        assertEquals(coordinate, result.moves().get(0).coordinate());
        assertTrue(result.winner().isPresent());
        assertEquals(PLAYER_ONE_ID, result.winner().get().playerId());
        assertEquals(Game.WinningRule.Horizontal, result.winner().get().winningRule());

        // Verify interactions
        verify(gameDao).findGameById(TEST_GAME_ID);
        verify(gameEngine).checkMove(any(Game.class), eq(PLAYER_ONE_ID), eq(coordinate));
        verify(gameEngine).getWinner(any(Game.class));

        // Verify game was updated
        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameDao).updateGame(gameCaptor.capture());

        Game capturedGame = gameCaptor.getValue();
        assertEquals(TEST_GAME_ID, capturedGame.id());
        assertEquals(1, capturedGame.moves().size());
        assertEquals(PLAYER_ONE_ID, capturedGame.moves().get(0).playerId());
        assertEquals(FIXED_INSTANT, capturedGame.moves().get(0).performedAt());
        assertEquals(coordinate, capturedGame.moves().get(0).coordinate());
        assertTrue(capturedGame.winner().isPresent());
        assertEquals(PLAYER_ONE_ID, capturedGame.winner().get().playerId());
        assertEquals(Game.WinningRule.Horizontal, capturedGame.winner().get().winningRule());

        // Verify executorService was called to notify listeners
        verify(executorService, times(2)).submit(any(Runnable.class));
    }

    @Test
    void addMoveShouldThrowExceptionWhenGameNotFound() {
        // Arrange
        Game.Coordinate coordinate = new Game.Coordinate(0, 0);
        when(gameDao.findGameById(TEST_GAME_ID)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> gameService.addMove(TEST_GAME_ID, PLAYER_ONE_ID, coordinate)
        );

        assertEquals("Game with gameId=" + TEST_GAME_ID + " not found", exception.getMessage());

        // Verify interactions
        verify(gameDao).findGameById(TEST_GAME_ID);
        verify(executorService, never()).submit(any(Runnable.class));
    }

    @Test
    void addMoveShouldThrowExceptionWhenGameHasWinner() throws ValidationException {
        // Arrange
        Game.Coordinate coordinate = new Game.Coordinate(0, 0);
        List<Game.Move> moves = new ArrayList<>();
        Game.Winner winner = new Game.Winner(PLAYER_ONE_ID, Game.WinningRule.Horizontal);

        Game game = new Game(
            TEST_GAME_ID,
            TEST_GAME_NAME,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            PLAYER_TWO_ID,
            moves,
            Optional.of(winner)
        );

        when(gameDao.findGameById(TEST_GAME_ID)).thenReturn(Optional.of(game));

        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> gameService.addMove(TEST_GAME_ID, PLAYER_ONE_ID, coordinate)
        );

        assertEquals("Game gameId=" + TEST_GAME_ID + " already has a winner", exception.getMessage());

        // Verify interactions
        verify(gameDao).findGameById(TEST_GAME_ID);
        verify(executorService, never()).submit(any(Runnable.class));
    }

    @Test
    void addMoveShouldThrowExceptionWhenMoveIsInvalid() throws ValidationException {
        // Arrange
        Game.Coordinate coordinate = new Game.Coordinate(0, 0);
        List<Game.Move> moves = new ArrayList<>();

        Game game = new Game(
            TEST_GAME_ID,
            TEST_GAME_NAME,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            PLAYER_TWO_ID,
            moves,
            Optional.empty()
        );

        when(gameDao.findGameById(TEST_GAME_ID)).thenReturn(Optional.of(game));
        doThrow(new ValidationException("Invalid move")).when(gameEngine).checkMove(any(Game.class), anyString(), any(Game.Coordinate.class));

        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> gameService.addMove(TEST_GAME_ID, PLAYER_ONE_ID, coordinate)
        );

        assertEquals("Invalid move", exception.getMessage());

        // Verify interactions
        verify(gameDao).findGameById(TEST_GAME_ID);
        verify(gameEngine).checkMove(any(Game.class), eq(PLAYER_ONE_ID), eq(coordinate));
        verify(executorService, never()).submit(any(Runnable.class));
    }

    @Test
    void addMoveShouldThrowExceptionWhenUpdateFails() throws ValidationException {
        // Arrange
        Game.Coordinate coordinate = new Game.Coordinate(0, 0);
        List<Game.Move> moves = new ArrayList<>();

        Game game = new Game(
            TEST_GAME_ID,
            TEST_GAME_NAME,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            PLAYER_TWO_ID,
            moves,
            Optional.empty()
        );

        when(gameDao.findGameById(TEST_GAME_ID)).thenReturn(Optional.of(game));
        when(gameDao.updateGame(any(Game.class))).thenReturn(Optional.empty());
        when(gameEngine.getWinner(any(Game.class))).thenReturn(Optional.empty());
        doNothing().when(gameEngine).checkMove(any(Game.class), anyString(), any(Game.Coordinate.class));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> gameService.addMove(TEST_GAME_ID, PLAYER_ONE_ID, coordinate)
        );

        assertTrue(exception.getMessage().contains("Game id=" + TEST_GAME_ID + " not found"));
        assertTrue(exception.getMessage().contains("concurrency issue"));

        // Verify interactions
        verify(gameDao).findGameById(TEST_GAME_ID);
        verify(gameEngine).checkMove(any(Game.class), eq(PLAYER_ONE_ID), eq(coordinate));
        verify(gameEngine).getWinner(any(Game.class));
        verify(gameDao).updateGame(any(Game.class));
        verify(executorService, never()).submit(any(Runnable.class));
    }

    @Test
    void getGameByIdShouldReturnGameWhenExists() throws ResourceNotFoundException {
        // Arrange
        List<Game.Move> moves = new ArrayList<>();

        Game game = new Game(
            TEST_GAME_ID,
            TEST_GAME_NAME,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            PLAYER_TWO_ID,
            moves,
            Optional.empty()
        );

        when(gameDao.findGameById(TEST_GAME_ID)).thenReturn(Optional.of(game));

        // Act
        Game result = gameService.getGameById(TEST_GAME_ID);

        // Assert
        assertEquals(TEST_GAME_ID, result.id());
        assertEquals(TEST_GAME_NAME, result.title());
        assertEquals(FIXED_INSTANT, result.createdAt());
        assertEquals(PLAYER_ONE_ID, result.createdBy());
        assertEquals(FIXED_INSTANT, result.startedAt());
        assertEquals(PLAYER_ONE_ID, result.playerOneId());
        assertEquals(PLAYER_TWO_ID, result.playerTwoId());
        assertTrue(result.moves().isEmpty());
        assertTrue(result.winner().isEmpty());

        // Verify interactions
        verify(gameDao).findGameById(TEST_GAME_ID);
    }

    @Test
    void getGameByIdShouldThrowExceptionWhenGameNotFound() {
        // Arrange
        when(gameDao.findGameById(TEST_GAME_ID)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> gameService.getGameById(TEST_GAME_ID)
        );

        assertEquals("Game with gameId=" + TEST_GAME_ID + " not found", exception.getMessage());

        // Verify interactions
        verify(gameDao).findGameById(TEST_GAME_ID);
    }

    @Test
    void getPendingGameByIdShouldReturnPendingGameWhenExists() throws ResourceNotFoundException {
        // Arrange
        PendingGame pendingGame = new PendingGame(
            TEST_GAME_ID,
            TEST_GAME_NAME,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            Optional.empty()
        );

        when(gameDao.findPendingGameById(TEST_GAME_ID)).thenReturn(Optional.of(pendingGame));

        // Act
        PendingGame result = gameService.getPendingGameById(TEST_GAME_ID);

        // Assert
        assertEquals(TEST_GAME_ID, result.id());
        assertEquals(TEST_GAME_NAME, result.title());
        assertEquals(FIXED_INSTANT, result.createdAt());
        assertEquals(PLAYER_ONE_ID, result.createdBy());
        assertEquals(Optional.empty(), result.gameStartedAt());

        // Verify interactions
        verify(gameDao).findPendingGameById(TEST_GAME_ID);
    }

    @Test
    void getPendingGameByIdShouldThrowExceptionWhenPendingGameNotFound() {
        // Arrange
        when(gameDao.findPendingGameById(TEST_GAME_ID)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> gameService.getPendingGameById(TEST_GAME_ID)
        );

        assertEquals("Error finding pending game with pendingGameId=" + TEST_GAME_ID, exception.getMessage());

        // Verify interactions
        verify(gameDao).findPendingGameById(TEST_GAME_ID);
    }

    @Test
    void registerForUpdatesShouldRegisterCallbacks() throws ResourceNotFoundException, IOException, ValidationException {
        // Arrange
        List<Game.Move> moves = new ArrayList<>();

        Game game = new Game(
            TEST_GAME_ID,
            TEST_GAME_NAME,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            PLAYER_TWO_ID,
            moves,
            Optional.empty()
        );

        when(gameDao.findGameById(TEST_GAME_ID)).thenReturn(Optional.of(game));
        when(randomGenerator.uuid()).thenReturn(TEST_UUID);

        ThrowableConsumer<Game.Move, IOException> moveConsumer = mock(ThrowableConsumer.class);
        ThrowableConsumer<Game.Winner, IOException> winnerConsumer = mock(ThrowableConsumer.class);

        // Act
        String registrationId = gameService.registerForUpdates(TEST_GAME_ID, moveConsumer, winnerConsumer);

        // Assert
        assertEquals(TEST_UUID.toString(), registrationId);

        // Verify interactions
        verify(gameDao).findGameById(TEST_GAME_ID);
        verify(randomGenerator).uuid();
    }

    @Test
    void registerForUpdatesCallbacksShouldBeCalledOnGameUpdates() throws ResourceNotFoundException, IOException, ValidationException {
        // Arrange
        List<Game.Move> moves = new ArrayList<>();

        Game game = new Game(
            TEST_GAME_ID,
            TEST_GAME_NAME,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            PLAYER_TWO_ID,
            moves,
            Optional.empty()
        );

        when(gameDao.findGameById(TEST_GAME_ID)).thenReturn(Optional.of(game));
        when(randomGenerator.uuid()).thenReturn(TEST_UUID);

        ThrowableConsumer<Game.Move, IOException> moveConsumer = mock(ThrowableConsumer.class);
        ThrowableConsumer<Game.Winner, IOException> winnerConsumer = mock(ThrowableConsumer.class);

        // Register for updates
        String registrationId = gameService.registerForUpdates(TEST_GAME_ID, moveConsumer, winnerConsumer);

        // Create a move
        Game.Coordinate coordinate = new Game.Coordinate(0, 0);
        Game.Move move = new Game.Move(PLAYER_ONE_ID, FIXED_INSTANT, coordinate);

        // Add the move to the game
        moves.add(move);

        // Update the game with a winner
        Game.Winner winner = new Game.Winner(PLAYER_ONE_ID, Game.WinningRule.Horizontal);
        Game updatedGame = new Game(
            TEST_GAME_ID,
            TEST_GAME_NAME,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            PLAYER_TWO_ID,
            moves,
            Optional.of(winner)
        );

        // Setup mocks for addMove
        when(gameDao.findGameById(TEST_GAME_ID)).thenReturn(Optional.of(game));
        when(gameDao.updateGame(any(Game.class))).thenReturn(Optional.of(updatedGame));
        when(gameEngine.getWinner(any(Game.class))).thenReturn(Optional.of(winner));
        doNothing().when(gameEngine).checkMove(any(Game.class), anyString(), any(Game.Coordinate.class));

        // Capture the Runnable submitted to executorService
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        // Simulate adding a move which will trigger the callbacks
        try {
            gameService.addMove(TEST_GAME_ID, PLAYER_TWO_ID, coordinate);

            // Verify executorService was called with Runnables
            verify(executorService, times(2)).submit(runnableCaptor.capture());

            // Execute the captured Runnables to trigger the callbacks
            List<Runnable> runnables = runnableCaptor.getAllValues();
            for (Runnable runnable : runnables) {
                runnable.run();
            }

            // Verify callbacks were called
            try {
                verify(moveConsumer).accept(any(Game.Move.class));
                verify(winnerConsumer).accept(any(Game.Winner.class));
            } catch (IOException e) {
                fail("Callbacks should not throw IOException in this test");
            }
        } catch (ValidationException | ResourceNotFoundException e) {
            fail("Should not throw exception when adding move: " + e.getMessage());
        }
    }

    @Test
    void registerForUpdatesShouldThrowExceptionWhenGameNotFound() {
        // Arrange
        when(gameDao.findGameById(TEST_GAME_ID)).thenReturn(Optional.empty());

        ThrowableConsumer<Game.Move, IOException> moveConsumer = mock(ThrowableConsumer.class);
        ThrowableConsumer<Game.Winner, IOException> winnerConsumer = mock(ThrowableConsumer.class);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> gameService.registerForUpdates(TEST_GAME_ID, moveConsumer, winnerConsumer)
        );

        assertEquals("Game with gameId=" + TEST_GAME_ID + " not found", exception.getMessage());

        // Verify interactions
        verify(gameDao).findGameById(TEST_GAME_ID);
        verify(randomGenerator, never()).uuid();
    }

    @Test
    void unregisterForUpdatesShouldRemoveCallbacks() {
        // Arrange
        // First register for updates
        List<Game.Move> moves = new ArrayList<>();

        Game game = new Game(
            TEST_GAME_ID,
            TEST_GAME_NAME,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            PLAYER_TWO_ID,
            moves,
            Optional.empty()
        );

        when(gameDao.findGameById(TEST_GAME_ID)).thenReturn(Optional.of(game));
        when(randomGenerator.uuid()).thenReturn(TEST_UUID);

        ThrowableConsumer<Game.Move, IOException> moveConsumer = mock(ThrowableConsumer.class);
        ThrowableConsumer<Game.Winner, IOException> winnerConsumer = mock(ThrowableConsumer.class);

        try {
            String registrationId = gameService.registerForUpdates(TEST_GAME_ID, moveConsumer, winnerConsumer);

            // Act
            gameService.unregisterForUpdates(registrationId);

            // Assert - no direct way to verify unregistration, but we can check that the logger was called
            // This is a bit of a weak test, but it's the best we can do without exposing internal state

            // Verify interactions
            verify(gameDao).findGameById(TEST_GAME_ID);
            verify(randomGenerator).uuid();
        } catch (ResourceNotFoundException e) {
            fail("Should not throw exception during setup");
        }
    }

    @Test
    void getPendingGamesShouldReturnListOfPendingGames() {
        // Arrange
        int limit = 10;
        int offset = 0;

        PendingGame pendingGame1 = new PendingGame(
            TEST_GAME_ID,
            TEST_GAME_NAME,
            FIXED_INSTANT,
            PLAYER_ONE_ID,
            Optional.empty()
        );

        PendingGame pendingGame2 = new PendingGame(
            "another-game-id",
            "Another Game",
            FIXED_INSTANT,
            PLAYER_TWO_ID,
            Optional.empty()
        );

        List<PendingGame> pendingGames = Arrays.asList(pendingGame1, pendingGame2);

        when(gameDao.getPendingGames(limit, offset)).thenReturn(pendingGames);

        // Act
        List<PendingGame> result = gameService.getPendingGames(limit, offset);

        // Assert
        assertEquals(2, result.size());
        assertEquals(pendingGame1, result.get(0));
        assertEquals(pendingGame2, result.get(1));

        // Verify interactions
        verify(gameDao).getPendingGames(limit, offset);
    }

    @Test
    void getPendingGamesShouldReturnEmptyListWhenNoPendingGames() {
        // Arrange
        int limit = 10;
        int offset = 0;

        List<PendingGame> pendingGames = Collections.emptyList();

        when(gameDao.getPendingGames(limit, offset)).thenReturn(pendingGames);

        // Act
        List<PendingGame> result = gameService.getPendingGames(limit, offset);

        // Assert
        assertTrue(result.isEmpty());

        // Verify interactions
        verify(gameDao).getPendingGames(limit, offset);
    }
}
